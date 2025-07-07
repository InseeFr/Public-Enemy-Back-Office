package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationIdentifierHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InterrogationIdentifierHandlerTest {
    @ParameterizedTest
    @ValueSource(strings = {"11-CAPI", "CAPI-1", "1111111", "11-CAPI-PLOP"})
    void onCreateIdentifierHandlerFromQueenIdentifierWhenPatternNotRecognizeThrowsException(String queenIdentifier) {
        assertThrows(IllegalArgumentException.class, () -> new InterrogationIdentifierHandler(queenIdentifier));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CAPI-1", "1111111", "11-CAPI-PLOP"})
    void onCreateIdentifierHandlerWhenPatternNotRecognizeThrowsException(String questionnaireModelId) {
        assertThrows(IllegalArgumentException.class, () -> new InterrogationIdentifierHandler(questionnaireModelId, 1));
    }

    @Test
    void onCreateIdentifierHandlerFromQueenIdentifierReturnCorrectIdentifiers() {
        InterrogationIdentifierHandler handler = new InterrogationIdentifierHandler("11-CAPI-1");
        assertEquals(11L, handler.getQuestionnaireId());
        assertEquals(1, handler.getInterrogationIdentifier());
        assertEquals("11-CAPI", handler.getQuestionnaireModelId());
        assertEquals("11-CAPI-1", handler.getQueenIdentifier());
    }

    @Test
    void onCreateIdentifierHandleReturnCorrectIdentifiers() {
        InterrogationIdentifierHandler handler = new InterrogationIdentifierHandler("11-CAPI", 1);
        assertEquals(1, handler.getInterrogationIdentifier());
        assertEquals("11-CAPI", handler.getQuestionnaireModelId());
        assertEquals("11-CAPI-1", handler.getQueenIdentifier());
        assertEquals(11L, handler.getQuestionnaireId());
    }
}
