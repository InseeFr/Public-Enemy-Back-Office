package fr.insee.publicenemy.api.controllers;


import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.exceptions.dto.InterrogationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterrogationMessagesComponentTest {

    @Mock
    private I18nMessagePort messagePort;

    private InterrogationMessagesComponent component;

    @BeforeEach
    void init() {
        component = new InterrogationMessagesComponent(messagePort);
    }

    /**
     * Check that transformation to dto is correct
     */
    @Test
    void onGetErrorsReturnSpecificErrors() {

        List<String> codes = List.of("error.code1", "error.code2");
        List<String> atts = List.of("att1", "att2");
        List<String> surveyUnitsIds = List.of("11-CAPI-1", "11-CAPI-2");

        when(messagePort.getMessage(eq(codes.get(0)), any())).thenReturn(codes.get(0));
        when(messagePort.getMessage(eq(codes.get(1)), any())).thenReturn(codes.get(1));
        when(messagePort.getMessage(eq("data.error.variable.line"), any(),any(),any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return String.format("%s - %s. %s %s", args);
        });

        List<InterrogationDataValidationResult> interrogationDataValidationResults = new ArrayList<>();
        List<InterrogationDataAttributeValidationResult> attributesValidations = new ArrayList<>();
        DataTypeValidationMessage message1 = DataTypeValidationMessage.createMessage(codes.get(0));
        DataTypeValidationMessage message2 = DataTypeValidationMessage.createMessage(codes.get(1), "plop");
        List<DataTypeValidationMessage> messages = List.of(message1, message2);

        DataTypeValidationResult typeValidation = new DataTypeValidationResult(false, messages);
        attributesValidations.add(new InterrogationDataAttributeValidationResult(atts.get(0), typeValidation));
        attributesValidations.add(new InterrogationDataAttributeValidationResult(atts.get(1), typeValidation));
        interrogationDataValidationResults.add(new InterrogationDataValidationResult(surveyUnitsIds.get(0), attributesValidations));
        interrogationDataValidationResults.add(new InterrogationDataValidationResult(surveyUnitsIds.get(1), attributesValidations));
        List<InterrogationError> surveyUnitsErrors = component.getErrors(interrogationDataValidationResults);


        assertEquals(8, surveyUnitsErrors.size());
        assertEquals(4, surveyUnitsErrors.stream()
                .filter(error -> error.message().contains(codes.getFirst()))
                .count());
        assertEquals(4, surveyUnitsErrors.stream()
                .filter(error -> error.message().contains(codes.getLast()))
                .count());


    }
}
