package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitAttributeValidation;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitValidation;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitAttributeError;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitErrors;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SurveyUnitMessagesComponent {
    private final I18nMessagePort messageService;

    public SurveyUnitMessagesComponent(I18nMessagePort messageService) {
        this.messageService = messageService;
    }

    /**
     * This method transforms survey unit validation objects to errors ready to be used as API response
     * @param surveyUnitsValidation list of survey unit validation objects
     * @return list of errors to be displayed for client
     */
    public List<SurveyUnitErrors> getErrors(@NonNull List<SurveyUnitValidation> surveyUnitsValidation) {
        List<SurveyUnitErrors> surveyUnitsErrors = new ArrayList<>();

        for(SurveyUnitValidation surveyUnitErrors : surveyUnitsValidation) {
            List<SurveyUnitAttributeError> attributeErrors = getAttributesErrors(surveyUnitErrors);
            // split the id to get rid of the questionnaire id part for frontend
            String surveyUnitId = IdentifierGenerationUtils.generateSurveyUnitIdentifierFromQueen(surveyUnitErrors.surveyUnitId());
            surveyUnitsErrors.add(new SurveyUnitErrors(surveyUnitId,attributeErrors));
        }
        return surveyUnitsErrors;
    }

    /**
     * @param surveyUnitErrors object containing all errors for a survey unit
     * @return all attributes errors from a survey unit errors object
     */
    private List<SurveyUnitAttributeError> getAttributesErrors(SurveyUnitValidation surveyUnitErrors) {
        List<SurveyUnitAttributeError> attributesErrors = new ArrayList<>();
        for(SurveyUnitAttributeValidation attributeError : surveyUnitErrors.attributesValidation()) {
            attributesErrors.add(getAttributeErrors(attributeError));
        }
        return attributesErrors;
    }

    /**
     *
     * @param attributeErrors object containing all errors for an attribute
     * @return all error messages for an attribute error object
     */
    private SurveyUnitAttributeError getAttributeErrors(SurveyUnitAttributeValidation attributeErrors) {
        List<String> messages = attributeErrors.dataTypeValidation().errorMessages().stream()
                .map(validationMessage -> messageService.getMessage(validationMessage.getCode(), validationMessage.getArguments()))
                .toList();

        return new SurveyUnitAttributeError(attributeErrors.attributeName(), messages);
    }
}
