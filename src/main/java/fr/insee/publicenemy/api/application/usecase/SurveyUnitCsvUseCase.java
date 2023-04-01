package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.ISurveyUnitObjectData;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitAttributeValidation;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitValidation;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SurveyUnitCsvUseCase {

    private final SurveyUnitCsvPort surveyUnitCsvService;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final DDIUseCase ddiUseCase;

    private final I18nMessagePort messageService;

    private static final String VALIDATION_ERROR = "validation.errors";

    public SurveyUnitCsvUseCase(SurveyUnitCsvPort surveyUnitCsvService, DDIUseCase ddiUseCase,
                                QuestionnaireUseCase questionnaireUseCase, I18nMessagePort messagePort) {
        this.surveyUnitCsvService = surveyUnitCsvService;
        this.ddiUseCase = ddiUseCase;
        this.questionnaireUseCase = questionnaireUseCase;
        this.messageService = messagePort;
    }

    /**
     *
     * @param poguesId pogues questionnaire id
     * @return Headers for csv file
     */
    public SurveyUnitCsvHeaderLine getHeadersLine(String poguesId) {
        List<VariableType> variables = ddiUseCase.getQuestionnaireVariables(poguesId);
        return surveyUnitCsvService.getSurveyUnitsCsvHeaders(variables);
    }

    /**
     * Check data from survey units against variables type from a specific questionnaire
     * @param surveyUnitData survey units data
     * @param questionnaireId id questionnaire id
     * @return validation errors for all survey units in survey units data
     */
    public List<ValidationWarningMessage> validateSurveyUnits(byte[] surveyUnitData, Long questionnaireId) throws SurveyUnitsGlobalValidationException, SurveyUnitsValidationException {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(questionnaireId);
        return validateSurveyUnits(surveyUnitData, questionnaire.getPoguesId());
    }

    /**
     * Check data from survey units against variables type from a specific questionnaire
     * @param surveyUnitData survey units data
     * @param poguesId questionnaire id from pogues
     * @return validation errors for all survey units in survey units data
     */
    public List<ValidationWarningMessage> validateSurveyUnits(byte[] surveyUnitData, String poguesId) throws SurveyUnitsGlobalValidationException, SurveyUnitsValidationException {
        List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(surveyUnitData, poguesId);
        List<VariableType> variablesType = ddiUseCase.getQuestionnaireVariables(poguesId);

        if(surveyUnits.isEmpty()) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.survey-units.no-exist");
            throw new SurveyUnitsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        int maxSurveyUnitsToAdd = 10;
        if(surveyUnits.size() > maxSurveyUnitsToAdd) {
            ValidationErrorMessage errorMessage = new ValidationErrorMessage("validation.survey-units.max-size", maxSurveyUnitsToAdd+"");
            throw new SurveyUnitsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), errorMessage);
        }

        SurveyUnit su = surveyUnits.get(0);
        List<ValidationErrorMessage> missingVariablesMessages = getMissingVariablesMessages(su, variablesType);
        if(!missingVariablesMessages.isEmpty()) {
            throw new SurveyUnitsGlobalValidationException(messageService.getMessage(VALIDATION_ERROR), missingVariablesMessages);
        }

        // retrieve validation objects with attributes errors
        List<SurveyUnitValidation> surveyUnitsErrors = surveyUnits.stream()
                .map(surveyUnit -> getSurveyUnitErrors(surveyUnit, variablesType))
                .filter(surveyUnitValidation -> !surveyUnitValidation.attributesValidation().isEmpty())
                .toList();

        if(!surveyUnitsErrors.isEmpty()) {
            throw new SurveyUnitsValidationException(messageService.getMessage(VALIDATION_ERROR), surveyUnitsErrors);
        }

        return getAdditionalAttributesMessages(su, variablesType);
    }

    /**
     * @param surveyUnit survey unit to check
     * @param variablesType list of defined variables for a questionnaire
     * @return messages about the missing variables on the survey unit
     */
    private List<ValidationErrorMessage> getMissingVariablesMessages(SurveyUnit surveyUnit, List<VariableType> variablesType) {
        Map<String, ISurveyUnitObjectData<?>> attributes = surveyUnit.data().getAttributes();
        Set<String> attributesKeys = attributes.keySet();

        // check if questionnaire variables are missing in a survey unit data
        return variablesType.stream()
                .map(VariableType::name)
                .filter(variableName -> attributesKeys.stream().noneMatch(variableName::equalsIgnoreCase))
                .map(missingVariableKey -> new ValidationErrorMessage("validation.variable.not-defined", missingVariableKey))
                .toList();
    }

    private List<ValidationWarningMessage> getAdditionalAttributesMessages(SurveyUnit surveyUnit, List<VariableType> variablesType) {
        Map<String, ISurveyUnitObjectData<?>> attributes = surveyUnit.data().getAttributes();
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
     * @param surveyUnit survey unit to check
     * @param variablesType list of variables from a questionnaire
     * @return validation errors messages for a specific survey unit
     */
    private SurveyUnitValidation getSurveyUnitErrors(SurveyUnit surveyUnit, List<VariableType> variablesType) {
        List<SurveyUnitAttributeValidation> attributesErrors = new ArrayList<>();
        Map<String, ISurveyUnitObjectData<?>> attributes = surveyUnit.data().getAttributes();

        // validate variables in survey units data
        for (Map.Entry<String, ISurveyUnitObjectData<?>> entry : attributes.entrySet()) {
            String attributeKey = entry.getKey();
            ISurveyUnitObjectData<?> attributeObjectData = entry.getValue();

            SurveyUnitAttributeValidation attributeValidationObject = validateAttribute(attributeKey, attributeObjectData, variablesType);

            if(!attributeValidationObject.dataTypeValidation().isValid()) {
                attributesErrors.add(attributeValidationObject);
            }
        }
        return new SurveyUnitValidation(surveyUnit.id(), attributesErrors);
    }

    /**
     * Validate a survey unit attribute against list of variable types
     * @param attributeKey attribute key
     * @param attributeObjectData attribute value
     * @param variablesType list of variables from a questionnaire
     * @return a validation object containing errors message for the attribute specified
     */
    private SurveyUnitAttributeValidation validateAttribute(String attributeKey, ISurveyUnitObjectData<?> attributeObjectData, List<VariableType> variablesType) {
        return variablesType.stream()
                .filter(variable -> variable.name().equalsIgnoreCase(attributeKey))
                .findFirst()
                .map(variableType -> new SurveyUnitAttributeValidation(attributeKey, attributeObjectData.validate(variableType)))
                .orElseGet(() -> new SurveyUnitAttributeValidation(attributeKey, DataTypeValidation.createOkDataTypeValidation()));
    }
}
