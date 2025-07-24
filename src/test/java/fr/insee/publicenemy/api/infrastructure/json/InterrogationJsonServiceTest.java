package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
    @ValueSource(strings = {"random-uuid-1", "random-uuid-2"})
    void onGetCsvSurveyUnitReturnCorrectInterrogation(String surveyUnitId) {
        byte[] data = """
        [
            {
                "id": "random-uuid-1",
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value random-uuid-1" } } }
            },
            {
                "id": "random-uuid-2",
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value random-uuid-2" } } }
            }
        ]
        """.getBytes();
        Interrogation su = service.getJsonInterrogation(surveyUnitId, data);
        assertEquals(surveyUnitId, su.id());
        assertEquals(("value " + surveyUnitId), su.data().getCollectedAttributes().get("name").getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"random-0", "random-10"})
    void onGetCsvSurveyUnitWhenInterrogationIdIncorrectThrowsException(String surveyUnitId) {
        byte[] data = "[]".getBytes();
        assertThrows(InterrogationJsonNotFoundException.class, () -> service.getJsonInterrogation(surveyUnitId, data));
    }

    @Test
    void whenValidJsonAndInterrogations_thenAddIdToOnlyEmptyId() throws Exception {
        // Given

        byte[] data = """
        [
            {
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value random-uuid-1" } } }
            },
            {
                "id": "",
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value random-uuid-1" } } }
            },
            {
                "id": "existing-random-uuid-3",
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value random-uuid-2" } } }
            }
        ]
        """.getBytes();

        List<Interrogation> interrogations = List.of(
                new Interrogation("random-uuid-1", null, null, null),
                new Interrogation("random-uuid-2", null, null, null),
                new Interrogation("random-uuid-3", null, null, null)
        );

        // When
        byte[] resultBytes = service.addInterrogationIdToData(data, interrogations);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode result = objectMapper.readTree(resultBytes);

        // Then
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        assertEquals("random-uuid-1", result.get(0).get("id").asText());
        assertEquals("random-uuid-2", result.get(1).get("id").asText());
        assertEquals("existing-random-uuid-3", result.get(2).get("id").asText());
    }

}
