package fr.insee.publicenemy.api.infrastructure.json;

import fr.insee.publicenemy.api.application.domain.model.interrogation.*;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.json.exceptions.InterrogationJsonNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InterrogationJsonServiceTest {

    private InterrogationJsonService service;

    @Mock
    private I18nMessagePort messageService;

    @BeforeEach
    void init() {
        this.service = new InterrogationJsonService(messageService);
    }

    @Test
    void onGetSurveyUnitsReturnCorrectCountNumber() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/interrogation-list-data.json";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());
        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        assertEquals(1, interrogations.size());
    }

    @Test
    void onGetSurveyUnitsReturnCorrectSurveyUnitsInfo() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/interrogation-list-data.json";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        Interrogation interrogation = interrogations.getFirst();
        Map<String, IInterrogationDataAttributeValue> attributes = interrogation.data().getCollectedAttributes();

        assertEquals(String.format("%s-%s", questionnaireModelId, "1"), interrogation.id());
        InterrogationDataAttributeValue<String> bonjourValue = new InterrogationDataAttributeValue<>("Bonjour");
        InterrogationDataAttributeValueList<String> prenomList = new InterrogationDataAttributeValueList<>();
        prenomList.addValue("Alice");
        prenomList.addValue("Bob");
        assertEquals(bonjourValue, attributes.get("QUESTIONTEXT"));
        assertEquals(prenomList, attributes.get("PRENOM"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void onGetCsvSurveyUnitReturnCorrectInterrogation(int surveyUnitId) {
        String questionnaireModelId = "11-CAPI";
        InterrogationIdentifierHandler identifierHandler = new InterrogationIdentifierHandler(questionnaireModelId, surveyUnitId);
        byte[] data = """
        [
            {
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value1" } } }
            },
            {
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value2" } } }
            }
        ]
        """.getBytes();
        Interrogation su = service.getJsonInterrogation(surveyUnitId, data, questionnaireModelId);
        assertEquals(su.id(), identifierHandler.getQueenIdentifier());
        assertEquals(("value" + surveyUnitId), su.data().getCollectedAttributes().get("name").getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void onGetCsvSurveyUnitWhenInterrogationIdIncorrectThrowsException(int surveyUnitId) {
        String questionnaireModelId = "11-CAPI";
        byte[] data = "[]".getBytes();
        assertThrows(InterrogationJsonNotFoundException.class, () -> service.getJsonInterrogation(surveyUnitId, data, questionnaireModelId));
    }
}
