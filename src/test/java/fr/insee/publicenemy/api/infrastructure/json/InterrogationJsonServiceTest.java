package fr.insee.publicenemy.api.infrastructure.json;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.interrogation.*;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.json.exceptions.InterrogationJsonNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static fr.insee.publicenemy.api.infrastructure.json.InterrogationJsonService.simplifyMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterrogationJsonServiceTest {

    private InterrogationJsonService service;

    @Mock
    private I18nMessagePort messageService;

    @BeforeEach
    void init() {
        this.service = new InterrogationJsonService(messageService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"interrogation-list-data", "interrogation-data"})
    void onGetSurveyUnitsReturnCorrectCountNumber(String dataPath) throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = String.format("src/test/resources/%s.json", dataPath);
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());
        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        assertEquals(1, interrogations.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid"})
    void onGetInvalidJsonShouldThrowTechnicalError(String dataPath) throws IOException {
        String questionnaireModelId = "dummy";

        String resourcePath = String.format("src/test/resources/%s.json", dataPath);
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        when(messageService.getMessage(eq("validation.json.malform.error"), any())).thenAnswer(
                invocation -> simplifyMessage(invocation.getArgument(1)));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.initInterrogations(surveyUnitData, questionnaireModelId));
        assertEquals("Unexpected character (']' (code 93)): was expecting double-quote to start field name (line: 4, column: 3)", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"interrogation-list-data", "interrogation-data"})
    void onGetSurveyUnitsReturnCorrectSurveyUnitsInfo(String dataPath) throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = String.format("src/test/resources/%s.json", dataPath);
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        Interrogation interrogation = interrogations.getFirst();
        Map<String, IInterrogationDataAttributeValue> attributes = interrogation.data().getCollectedAttributes();

        InterrogationDataAttributeValue<String> bonjourValue = new InterrogationDataAttributeValue<>("Bonjour");
        InterrogationDataAttributeValueList<String> prenomList = new InterrogationDataAttributeValueList<>();
        prenomList.addValue("Alice");
        prenomList.addValue("Bob");
        assertEquals(bonjourValue, attributes.get("QUESTIONTEXT"));
        assertEquals(prenomList, attributes.get("PRENOM"));

        assertEquals("INIT", interrogation.stateData().state());
        assertEquals("5", interrogation.stateData().currentPage());
        assertEquals(1752053082781L, interrogation.stateData().date());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void onGetCsvSurveyUnitReturnCorrectInterrogation(int dataIndex) {
        PersonalizationMapping mapping = new PersonalizationMapping("11-CAPI-1", 11L, Mode.CAPI, dataIndex);
        byte[] data = """
        [
            {
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value 0" } } }
            },
            {
                "data": { "COLLECTED": { "name": { "COLLECTED" : "value 1" } } }
            }
        ]
        """.getBytes();
        Interrogation su = service.getJsonInterrogation(mapping, data);
        assertEquals(("value " + dataIndex), su.data().getCollectedAttributes().get("name").getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 10})
    void onGetCsvSurveyUnitWhenInterrogationIdIncorrectThrowsException(int dataIndex) {
        PersonalizationMapping mapping = new PersonalizationMapping("11-CAPI-1", 11L, Mode.CAPI, dataIndex);
        byte[] data = "[]".getBytes();
        assertThrows(InterrogationJsonNotFoundException.class, () -> service.getJsonInterrogation(mapping, data));
    }

}
