package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvHeaderLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InterrogationCsvUseCase {

    private int maxInterrogationsDataToAdd;

    private final InterrogationCsvPort interrogationCsvPort;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final PoguesUseCase poguesUseCase;

    private final I18nMessagePort messageService;

    private static final String VALIDATION_ERROR = "validation.errors";

    public InterrogationCsvUseCase(InterrogationCsvPort interrogationCsvPort, PoguesUseCase poguesUseCase,
                                   QuestionnaireUseCase questionnaireUseCase, I18nMessagePort messagePort,
                                   @Value("${application.campaign.max-interrogations}") int maxInterrogationsDataToAdd) {
        this.interrogationCsvPort = interrogationCsvPort;
        this.poguesUseCase = poguesUseCase;
        this.questionnaireUseCase = questionnaireUseCase;
        this.messageService = messagePort;
    }

    /**
     * @param poguesId pogues questionnaire id
     * @return Headers for csv file
     */
    public InterrogationCsvHeaderLine getHeadersLine(String poguesId) {
        List<VariableType> variables = poguesUseCase.getQuestionnaireVariables(poguesId);
        return interrogationCsvPort.getInterrogationsCsvHeaders(variables);
    }

    /**
     * Check data from survey units against variables type from a specific questionnaire
     *
     * @param interrogationData  survey units data
     * @param questionnaireId id questionnaire id
     * @return validation errors for all survey units in survey units data
     */
    public List<ValidationWarningMessage> validateInterrogations(byte[] interrogationData, Long questionnaireId) throws InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(questionnaireId);
        return validateInterrogations(interrogationData, questionnaire.getPoguesId());
    }

    /**
     * Check data from survey units against variables type from a specific questionnaire
     *
     * @param interrogationData survey units data
     * @param poguesId       questionnaire id from pogues
     * @return validation errors for all survey units in survey units data
     */
    public List<ValidationWarningMessage> validateInterrogations(byte[] interrogationData, String poguesId) throws InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {
        List<Interrogation> interrogations = interrogationCsvPort.initInterrogations(interrogationData, null);
        List<VariableType> variablesType = poguesUseCase.getQuestionnaireVariables(poguesId);

        if (interrogations.isEmpty()) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.survey-units.no-exist");
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        if (interrogations.size() > maxInterrogationsDataToAdd) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.survey-units.max-size", maxInterrogationsDataToAdd + "");
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        Interrogation su = interrogations.get(0);
        List<ValidationErrorMessage> missingVariablesMessages = getMissingVariablesMessages(su, variablesType);
        if (!missingVariablesMessages.isEmpty()) {
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), missingVariablesMessages);
        }

        // retrieve validation objects with attributes errors
        List<InterrogationDataValidationResult> validationResults = interrogations.stream()
                .map(interrogation -> getInterrogationErrors(interrogation, variablesType))
                .filter(interrogationDataValidationResult -> !interrogationDataValidationResult.attributesValidation().isEmpty())
                .toList();

        if (!validationResults.isEmpty()) {
            throw new InterrogationsSpecificValidationException(messageService.getMessage(VALIDATION_ERROR), validationResults);
        }

        return getAdditionalAttributesMessages(su, variablesType);
    }

    /**
     * @param interrogation    survey unit to check
     * @param variablesType list of defined variables for a questionnaire
     * @return messages about the missing variables on the survey unit
     */
    private List<ValidationErrorMessage> getMissingVariablesMessages(Interrogation interrogation, List<VariableType> variablesType) {
        Map<String, IInterrogationDataAttributeValue<?>> attributes = interrogation.data().getAttributes();
        Set<String> attributesKeys = attributes.keySet();

        // check if questionnaire variables are missing in a survey unit data
        return variablesType.stream()
                .map(VariableType::name)
                .filter(variableName -> attributesKeys.stream().noneMatch(variableName::equalsIgnoreCase))
                .map(missingVariableKey -> new ValidationErrorMessage("validation.variable.not-defined", missingVariableKey))
                .toList();
    }

    private List<ValidationWarningMessage> getAdditionalAttributesMessages(Interrogation interrogation, List<VariableType> variablesType) {
        Map<String, IInterrogationDataAttributeValue<?>> attributes = interrogation.data().getAttributes();
        Set<String> attributesKeys = attributes.keySet();
        List<String> variablesName = variablesType.stream().map(VariableType::name).toList();

        // check if non existing attributes are added to CSV schema
        return attributesKeys.stream()
                .filter(attributeName -> variablesName.stream().noneMatch(attributeName::equalsIgnoreCase))
                .map(attributeName -> new ValidationWarningMessage("validation.attribute.not-exist", attributeName))
                .toList();
    }

    /**
     * return validation errors messages a specific questionnaire
     *
     * @param interrogation    survey unit to check
     * @param variablesType list of variables from a questionnaire
     * @return validation errors messages for a specific survey unit
     */
    private InterrogationDataValidationResult getInterrogationErrors(Interrogation interrogation, List<VariableType> variablesType) {
        List<InterrogationDataAttributeValidationResult> attributesErrors = new ArrayList<>();
        Map<String, IInterrogationDataAttributeValue<?>> attributes = interrogation.data().getAttributes();

        // validate variables in survey units data
        for (Map.Entry<String, IInterrogationDataAttributeValue<?>> entry : attributes.entrySet()) {
            String attributeKey = entry.getKey();
            IInterrogationDataAttributeValue<?> attributeObjectData = entry.getValue();

            InterrogationDataAttributeValidationResult attributeValidationObject = validateAttribute(attributeKey, attributeObjectData, variablesType);

            if (!attributeValidationObject.dataTypeValidationResult().isValid()) {
                attributesErrors.add(attributeValidationObject);
            }
        }
        return new InterrogationDataValidationResult(interrogation.id(), attributesErrors);
    }

    /**
     * Validate a survey unit attribute against list of variable types
     *
     * @param attributeKey        attribute key
     * @param attributeObjectData attribute value
     * @param variablesType       list of variables from a questionnaire
     * @return a validation object containing errors message for the attribute specified
     */
    private InterrogationDataAttributeValidationResult validateAttribute(String attributeKey, IInterrogationDataAttributeValue<?> attributeObjectData, List<VariableType> variablesType) {
        return variablesType.stream()
                .filter(variable -> variable.name().equalsIgnoreCase(attributeKey))
                .findFirst()
                .map(variableType -> new InterrogationDataAttributeValidationResult(attributeKey, attributeObjectData.validate(variableType)))
                .orElseGet(() -> new InterrogationDataAttributeValidationResult(attributeKey, DataTypeValidationResult.createOkDataTypeValidation()));
    }
}
