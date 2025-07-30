package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.InterrogationAttributeError;
import fr.insee.publicenemy.api.controllers.dto.InterrogationErrors;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

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

        return IntStream.range(0, interrogationDataValidationResults.size())
                .mapToObj(dataIndex -> {
                    List<InterrogationAttributeError> errors = getAttributesErrors(interrogationDataValidationResults.get(dataIndex));
                    return errors.stream().map(error -> {
                        String finalMessage =  messageService.getMessage("data.error.variable.line",
                        error.attributeKey(),
                        String.valueOf(dataIndex + 1),
                        error.message());
                        return new InterrogationErrors(dataIndex, error.attributeKey(), finalMessage);
                    }).toList();
                })
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * @param interrogationErrors object containing all errors for a interrogation
     * @return all attributes errors from a interrogation errors object
     */
    private List<InterrogationAttributeError> getAttributesErrors(InterrogationDataValidationResult interrogationErrors) {
        List<InterrogationAttributeError> attributesErrors = new ArrayList<>();
        for (InterrogationDataAttributeValidationResult attributeError : interrogationErrors.attributesValidation()) {
            attributesErrors.addAll(getAttributeErrors(attributeError));
        }
        return attributesErrors;
    }

    /**
     * @param attributeErrors object containing all errors for an attribute
     * @return all error messages for an attribute error object
     */
    private List<InterrogationAttributeError> getAttributeErrors(InterrogationDataAttributeValidationResult attributeErrors) {
        return attributeErrors.dataTypeValidationResult().errorMessages().stream()
                .map(validationMessage -> messageService.getMessage(validationMessage.getCode(), validationMessage.getArguments()))
                .map(message -> new InterrogationAttributeError(attributeErrors.attributeName(), message))
                .toList();
    }
}
