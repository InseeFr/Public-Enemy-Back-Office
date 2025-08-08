package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.*;
import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.application.ports.InterrogationJsonPort;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvHeaderLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InterrogationUseCase {

    private final int maxInterrogationsDataToAdd;

    private final InterrogationCsvPort interrogationService;
    private final InterrogationJsonPort interrogationJsonService;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final PoguesUseCase poguesUseCase;

    private final I18nMessagePort messageService;

    private static final String VALIDATION_ERROR = "validation.errors";

    public InterrogationUseCase(InterrogationCsvPort interrogationService, InterrogationJsonPort interrogationJsonService,
                                PoguesUseCase poguesUseCase,
                                QuestionnaireUseCase questionnaireUseCase, I18nMessagePort messagePort,
                                @Value("${application.campaign.max-interrogations}") int maxInterrogationsDataToAdd) {
        this.interrogationService = interrogationService;
        this.interrogationJsonService = interrogationJsonService;
        this.poguesUseCase = poguesUseCase;
        this.questionnaireUseCase = questionnaireUseCase;
        this.messageService = messagePort;
        this.maxInterrogationsDataToAdd = maxInterrogationsDataToAdd;
    }

    /**
     * @param poguesId pogues questionnaire id
     * @return Headers for csv file
     */
    public InterrogationCsvHeaderLine getHeadersLine(String poguesId) {
        List<VariableType> variables = poguesUseCase.getQuestionnaireVariables(poguesId);
        return interrogationService.getInterrogationsCsvHeaders(variables);
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

        List<Interrogation> interrogations;
        InterrogationData.FormatType dataFormat = InterrogationData.getDataFormat(interrogationData);
        if(InterrogationData.FormatType.CSV.equals(dataFormat)){
            interrogations = interrogationService.initInterrogations(interrogationData, null);
        } else if(InterrogationData.FormatType.JSON.equals(dataFormat)) {
            interrogations = interrogationJsonService.initInterrogations(interrogationData, null);
        } else {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, "Invalid format of data");
        }
        List<VariableType> variablesType = poguesUseCase.getQuestionnaireVariables(poguesId);

        if (interrogations.isEmpty()) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.interrogations.no-exist");
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        if (interrogations.size() > maxInterrogationsDataToAdd) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.interrogations.max-size", maxInterrogationsDataToAdd + "");
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        Interrogation su = interrogations.get(0);
        List<ValidationErrorMessage> missingVariablesMessages = getMissingExternalVariablesMessages(su, variablesType);
        if (!missingVariablesMessages.isEmpty()) {
            throw new InterrogationsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), missingVariablesMessages);
        }

        // retrieve validation objects with attributes errors
        List<InterrogationDataValidationResult> validationResults = interrogations.stream()
                .map(interrogation -> getInterrogationErrors(interrogation, variablesType))
                .toList();

        if (!validationResults.stream().filter(validationResult -> !validationResult.attributesValidation().isEmpty()).toList().isEmpty()) {
            throw new InterrogationsSpecificValidationException(messageService.getMessage(VALIDATION_ERROR), validationResults);
        }

        return getAdditionalAttributesMessages(su, variablesType);
    }

    /**
     * @param interrogation    survey unit to check
     * @param variablesType list of defined variables for a questionnaire
     * @return messages about the missing variables on the survey unit
     */
    private List<ValidationErrorMessage> getMissingExternalVariablesMessages(Interrogation interrogation, List<VariableType> variablesType) {
        Map<String, IInterrogationDataAttributeValue> attributes = interrogation.data().getExternalAttributes();
        Set<String> attributesKeys = attributes.keySet();

        // check if EXTERNAL questionnaire variables are missing in a survey unit data
        return variablesType.stream()
                .filter(variable -> VariableTypeEnum.EXTERNAL.equals(variable.type()))
                .map(VariableType::name)
                .filter(variableName -> attributesKeys.stream().noneMatch(variableName::equalsIgnoreCase))
                .map(missingVariableKey -> new ValidationErrorMessage("validation.variable.not-defined", missingVariableKey))
                .toList();
    }

    private List<ValidationWarningMessage> getAdditionalAttributesMessages(Interrogation interrogation, List<VariableType> variablesType) {
        Map<String, IInterrogationDataAttributeValue> externalAttributes = interrogation.data().getExternalAttributes();
        Map<String, IInterrogationDataAttributeValue> collectedAttributes = interrogation.data().getCollectedAttributes();
        List<String> variablesName = variablesType.stream().map(VariableType::name).toList();

        List<ValidationWarningMessage> warningMessagesValidation = new ArrayList<>();

        // check if non existing attributes are added to CSV schema
        List<ValidationWarningMessage> externalVariablesWarning = externalAttributes.keySet().stream()
                .filter(attributeName -> variablesName.stream().noneMatch(attributeName::equalsIgnoreCase))
                .map(attributeName -> new ValidationWarningMessage("validation.attribute.not-exist", attributeName))
                .toList();
        List<ValidationWarningMessage> collectedVariablesWarning = collectedAttributes.keySet().stream()
                .filter(attributeName -> variablesName.stream().noneMatch(attributeName::equalsIgnoreCase))
                .map(attributeName -> new ValidationWarningMessage("validation.attribute.not-exist", attributeName))
                .toList();

        warningMessagesValidation.addAll(externalVariablesWarning);
        warningMessagesValidation.addAll(collectedVariablesWarning);
        return warningMessagesValidation;
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
        Map<String, IInterrogationDataAttributeValue> externalAttributes = interrogation.data().getExternalAttributes();
        Map<String, IInterrogationDataAttributeValue> collectedAttributes = interrogation.data().getCollectedAttributes();

        // validate external variables in survey units data
        for (Map.Entry<String, IInterrogationDataAttributeValue> entry : externalAttributes.entrySet()) {
            String attributeKey = entry.getKey();
            IInterrogationDataAttributeValue attributeObjectData = entry.getValue();

            InterrogationDataAttributeValidationResult attributeValidationObject = validateAttribute(attributeKey, attributeObjectData, variablesType);

            if (!attributeValidationObject.dataTypeValidationResult().isValid()) {
                attributesErrors.add(attributeValidationObject);
            }
        }

        // validate collected variables in survey units data
        for (Map.Entry<String, IInterrogationDataAttributeValue> entry : collectedAttributes.entrySet()) {
            String attributeKey = entry.getKey();
            IInterrogationDataAttributeValue attributeObjectData = entry.getValue();

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
    private InterrogationDataAttributeValidationResult validateAttribute(String attributeKey, IInterrogationDataAttributeValue attributeObjectData, List<VariableType> variablesType) {
        return variablesType.stream()
                .filter(variable -> variable.name().equalsIgnoreCase(attributeKey))
                .findFirst()
                .map(variableType -> new InterrogationDataAttributeValidationResult(attributeKey, attributeObjectData.validate(variableType)))
                .orElseGet(() -> new InterrogationDataAttributeValidationResult(attributeKey, DataTypeValidationResult.createOkDataTypeValidation()));
    }
}
