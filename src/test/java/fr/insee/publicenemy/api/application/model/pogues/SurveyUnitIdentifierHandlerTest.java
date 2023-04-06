package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SurveyUnitIdentifierHandlerTest {
    @ParameterizedTest
    @ValueSource(strings = {"11-CAPI", "CAPI-1", "1111111", "11-CAPI-PLOP"})
    void onCreateIdentifierHandlerWhenPatternNotRecognizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new SurveyUnitIdentifierHandler("11-CAPI"));
    }

    @Test
    void onCreateIdentifierHandlerFromQueenIdentifierReturnCorrectIdentifiers() {
        SurveyUnitIdentifierHandler handler = new SurveyUnitIdentifierHandler("11-CAPI-1");
        assertEquals("1", handler.getSurveyUnitIdentifier());
        assertEquals("11-CAPI", handler.getQuestionnaireModelId());
        assertEquals("11-CAPI-1", handler.getQueenIdentifier());
    }

    @Test
    void onCreateIdentifierHandleReturnCorrectIdentifiers() {
        SurveyUnitIdentifierHandler handler = new SurveyUnitIdentifierHandler("11-CAPI", 1);
        assertEquals("1", handler.getSurveyUnitIdentifier());
        assertEquals("11-CAPI", handler.getQuestionnaireModelId());
        assertEquals("11-CAPI-1", handler.getQueenIdentifier());
    }
}
