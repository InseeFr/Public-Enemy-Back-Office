package fr.insee.publicenemy.api.controllers;


import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataValidationResult;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitAttributeError;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitMessagesComponentTest {

    @Mock
    private I18nMessagePort messagePort;

    private SurveyUnitMessagesComponent component;

    @BeforeEach
    void init() {
        component = new SurveyUnitMessagesComponent(messagePort);
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

        List<SurveyUnitDataValidationResult> surveyUnitDataValidationResults = new ArrayList<>();
        List<SurveyUnitDataAttributeValidationResult> attributesValidations = new ArrayList<>();
        DataTypeValidationMessage message1 = DataTypeValidationMessage.createMessage(codes.get(0));
        DataTypeValidationMessage message2 = DataTypeValidationMessage.createMessage(codes.get(1), "plop");
        List<DataTypeValidationMessage> messages = List.of(message1, message2);

        DataTypeValidationResult typeValidation = new DataTypeValidationResult(false, messages);
        attributesValidations.add(new SurveyUnitDataAttributeValidationResult(atts.get(0), typeValidation));
        attributesValidations.add(new SurveyUnitDataAttributeValidationResult(atts.get(1), typeValidation));
        surveyUnitDataValidationResults.add(new SurveyUnitDataValidationResult(surveyUnitsIds.get(0), attributesValidations));
        surveyUnitDataValidationResults.add(new SurveyUnitDataValidationResult(surveyUnitsIds.get(1), attributesValidations));
        List<SurveyUnitErrors> surveyUnitsErrors = component.getErrors(surveyUnitDataValidationResults);

        for (int index = 0; index < surveyUnitsErrors.size(); index++) {
            SurveyUnitErrors surveyUnitErrors = surveyUnitsErrors.get(index);
            List<SurveyUnitAttributeError> attributesErrors = surveyUnitErrors.attributesErrors();
            assertEquals(surveyUnitsIds.get(index), surveyUnitErrors.surveyUnitId());
            for (int indexAttributes = 0; indexAttributes < attributesErrors.size(); indexAttributes++) {
                SurveyUnitAttributeError attributeErrors = attributesErrors.get(indexAttributes);
                assertEquals(atts.get(indexAttributes), attributeErrors.attributeKey());
                List<String> attributeMessages = attributeErrors.messages();
                assertTrue(attributeMessages.contains(codes.get(0)));
                assertTrue(attributeMessages.contains(codes.get(1)));
            }
        }
    }
}
