package fr.insee.publicenemy.api.infrastructure.csv;

import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationIdentifierHandler;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.csv.exceptions.InterrogationCsvNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InterrogationCsvServiceTest {

    private InterrogationCsvService service;

    @Mock
    private I18nMessagePort messageService;

    @BeforeEach
    void init() {
        this.service = new InterrogationCsvService(2, messageService);
    }

    @Test
    void onGetSurveyUnitsReturnCorrectCountNumber() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());
        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        assertEquals(10, interrogations.size());
    }

    @Test
    void onGetSurveyUnitsReturnCorrectSurveyUnitsInfo() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        List<Interrogation> interrogations = service.initInterrogations(surveyUnitData, questionnaireModelId);

        Interrogation interrogation = interrogations.get(0);
        Map<String, IInterrogationDataAttributeValue> attributes = interrogation.data().getExternalAttributes();

        InterrogationDataAttributeValue numfa = new InterrogationDataAttributeValue("1");
        InterrogationDataAttributeValue complement = new InterrogationDataAttributeValue("CS 70058");
        assertEquals(numfa, attributes.get("Numfa"));
        assertEquals(complement, attributes.get("ComplementAdresse"));
    }

    @Test
    void onGetSurveyUnitsWhenQuestionnaireNullThrowsNullPointerException() {
        String questionnaireModelId = "13-CAPI";
        assertThrows(NullPointerException.class, () -> service.initInterrogations(null, questionnaireModelId));
    }

    @Test
    void onGetSurveyUnitsWhenQuestionnaireModelIdNullThrowsNullPointerException() {
        byte[] surveyUnitData = null;
        assertThrows(NullPointerException.class, () -> service.initInterrogations(surveyUnitData, null));
    }

    @Test
    void onGetSurveyUnitsCsvHeaderWhenSimpleDatatypeReturnCorrectCsvHeader() {
        List<VariableType> variablesType = new ArrayList<>();
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "TEXT-TEST", null, null));
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "NUMERIC-TEST", null, null));

        InterrogationCsvHeaderLine headersLine = service.getInterrogationsCsvHeaders(variablesType);
        Set<String> csvHeaders = headersLine.headers();
        Iterator<String> iterator = csvHeaders.iterator();
        assertEquals(2, csvHeaders.size());
        assertEquals("TEXT-TEST", iterator.next());
        assertEquals("NUMERIC-TEST", iterator.next());
    }

    @Test
    void onGetSurveyUnitsCsvHeaderWhenIterationDatatypeReturnCorrectCsvHeader() {
        List<VariableType> variablesType = new ArrayList<>();
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "TEXT-TEST", "iteration", null));
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "NUMERIC-TEST", null, null));
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "DATE-TEST", "iteration", null));

        InterrogationCsvHeaderLine headersLine = service.getInterrogationsCsvHeaders(variablesType);
        Set<String> csvHeaders = headersLine.headers();
        Iterator<String> iterator = csvHeaders.iterator();
        assertEquals(5, csvHeaders.size());
        assertEquals("TEXT-TEST_1", iterator.next());
        assertEquals("TEXT-TEST_2", iterator.next());
        assertEquals("NUMERIC-TEST", iterator.next());
        assertEquals("DATE-TEST_1", iterator.next());
        assertEquals("DATE-TEST_2", iterator.next());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void onGetCsvSurveyUnitReturnCorrectInterrogation(int surveyUnitId) {
        byte[] data = "\"name\",\"ID_INTERROGATION\"\n\"value1\",\"UUID1\"\n\"value2\",\"UUID2\"".getBytes();
        Interrogation su = service.getCsvInterrogation("UUID"+surveyUnitId, data);
        assertEquals("UUID"+surveyUnitId, su.id());
        assertEquals(("value" + surveyUnitId), su.data().getExternalAttributes().get("name").getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void onGetCsvSurveyUnitWhenInterrogationIdIncorrectThrowsException(int surveyUnitId) {
        byte[] data = "\"name\"\n\"value1\"\n\"value2\"".getBytes();
        assertThrows(InterrogationCsvNotFoundException.class, () -> service.getCsvInterrogation(""+surveyUnitId, data));
    }

    @Test
    void givenCsvAndIdList_whenAddIdColumn_thenCsvContainsNewColumn() throws IOException {
        // Given
        String csvContent = """
            "name","age"
            "Alice","30"
            "Bob","25"
            """;
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        List<Interrogation> interrogations = List.of(
                new Interrogation("UUID1",null, null, null),
                new Interrogation("UUID2",null, null, null)
        );

        // When
        byte[] modifiedCsvBytes = service.addInterrogationIdToData(csvBytes, interrogations);
        String result = new String(modifiedCsvBytes, StandardCharsets.UTF_8);

        // Then
        String expected = """
            "name","age","ID_INTERROGATION"
            "Alice","30","UUID1"
            "Bob","25","UUID2"
            """;

        assertEquals(expected, result);
    }


}
