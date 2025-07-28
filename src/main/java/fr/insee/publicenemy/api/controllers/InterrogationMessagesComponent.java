package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.InterrogationAttributeError;
import fr.insee.publicenemy.api.controllers.dto.InterrogationErrors;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InterrogationMessagesComponent {
    private final I18nMessagePort messageService;

    public InterrogationMessagesComponent(I18nMessagePort messageService) {
        this.messageService = messageService;
    }

    /**
     * This method transforms survey unit validation objects to errors ready to be used as API response
     *
     * @param interrogationDataValidationResults list of survey unit validation error objects
     * @return list of errors to be displayed for client
     */
    public List<InterrogationErrors> getErrors(@NonNull List<InterrogationDataValidationResult> interrogationDataValidationResults) {
        List<InterrogationErrors> interrogationsErrors = new ArrayList<>();

        for (InterrogationDataValidationResult interrogationErrors : interrogationDataValidationResults) {
            List<InterrogationAttributeError> attributeErrors = getAttributesErrors(interrogationErrors);
            String interrogationId = interrogationErrors.InterrogationId();
            interrogationsErrors.add(new InterrogationErrors(interrogationId, attributeErrors));
        }
        return interrogationsErrors;
    }

    /**
     * @param interrogationErrors object containing all errors for a interrogation
     * @return all attributes errors from a interrogation errors object
     */
    private List<InterrogationAttributeError> getAttributesErrors(InterrogationDataValidationResult interrogationErrors) {
        List<InterrogationAttributeError> attributesErrors = new ArrayList<>();
        for (InterrogationDataAttributeValidationResult attributeError : interrogationErrors.attributesValidation()) {
            attributesErrors.add(getAttributeErrors(attributeError));
        }
        return attributesErrors;
    }

    /**
     * @param attributeErrors object containing all errors for an attribute
     * @return all error messages for an attribute error object
     */
    private InterrogationAttributeError getAttributeErrors(InterrogationDataAttributeValidationResult attributeErrors) {
        List<String> messages = attributeErrors.dataTypeValidationResult().errorMessages().stream()
                .map(validationMessage -> messageService.getMessage(validationMessage.getCode(), validationMessage.getArguments()))
                .toList();

        return new InterrogationAttributeError(attributeErrors.attributeName(), messages);
    }
}
