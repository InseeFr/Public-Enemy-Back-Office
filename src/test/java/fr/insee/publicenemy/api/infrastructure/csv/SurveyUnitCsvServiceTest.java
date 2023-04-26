package fr.insee.publicenemy.api.infrastructure.csv;

import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.ISurveyUnitDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.csv.exceptions.SurveyUnitCsvNotFoundException;
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
class SurveyUnitCsvServiceTest {

    private SurveyUnitCsvService service;

    @Mock
    private I18nMessagePort messageService;

    @BeforeEach
    void init() {
        this.service = new SurveyUnitCsvService(2, messageService);
    }

    @Test
    void onGetSurveyUnitsReturnCorrectCountNumber() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());
        List<SurveyUnit> surveyUnits = service.initSurveyUnits(surveyUnitData, questionnaireModelId);

        assertEquals(10, surveyUnits.size());
    }

    @Test
    void onGetSurveyUnitsReturnCorrectSurveyUnitsInfo() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        List<SurveyUnit> surveyUnits = service.initSurveyUnits(surveyUnitData, questionnaireModelId);

        SurveyUnit surveyUnit = surveyUnits.get(0);
        Map<String, ISurveyUnitDataAttributeValue<?>> attributes = surveyUnit.data().getAttributes();

        assertEquals(String.format("%s-%s", questionnaireModelId, "1"), surveyUnit.id());
        SurveyUnitDataAttributeValue numfa = new SurveyUnitDataAttributeValue("1");
        SurveyUnitDataAttributeValue complement = new SurveyUnitDataAttributeValue("CS 70058");
        assertEquals(numfa, attributes.get("Numfa"));
        assertEquals(complement, attributes.get("ComplementAdresse"));
    }

    @Test
    void onGetSurveyUnitsWhenQuestionnaireNullThrowsNullPointerException() {
        String questionnaireModelId = "13-CAPI";
        assertThrows(NullPointerException.class, () -> service.initSurveyUnits(null, questionnaireModelId));
    }

    @Test
    void onGetSurveyUnitsWhenQuestionnaireModelIdNullThrowsNullPointerException() {
        byte[] surveyUnitData = null;
        assertThrows(NullPointerException.class, () -> service.initSurveyUnits(surveyUnitData, null));
    }

    @Test
    void onGetSurveyUnitsCsvHeaderWhenSimpleDatatypeReturnCorrectCsvHeader() {
        List<VariableType> variablesType = new ArrayList<>();
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "TEXT-TEST", null, null));
        variablesType.add(new VariableType(VariableTypeEnum.EXTERNAL, "NUMERIC-TEST", null, null));

        SurveyUnitCsvHeaderLine headersLine = service.getSurveyUnitsCsvHeaders(variablesType);
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

        SurveyUnitCsvHeaderLine headersLine = service.getSurveyUnitsCsvHeaders(variablesType);
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
    void onGetCsvSurveyUnitReturnCorrectSurveyUnit(int surveyUnitId) {
        String questionnaireModelId = "11-CAPI";
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(questionnaireModelId, surveyUnitId);
        byte[] data = "\"name\"\n\"value1\"\n\"value2\"".getBytes();
        SurveyUnit su = service.getCsvSurveyUnit(surveyUnitId, data, questionnaireModelId);
        assertEquals(su.id(), identifierHandler.getQueenIdentifier());
        assertEquals(("value" + surveyUnitId), su.data().getAttributes().get("name").getValue());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void onGetCsvSurveyUnitWhenSurveyUnitIdIncorrectThrowsException(int surveyUnitId) {
        String questionnaireModelId = "11-CAPI";
        byte[] data = "\"name\"\n\"value1\"\n\"value2\"".getBytes();
        assertThrows(SurveyUnitCsvNotFoundException.class, () -> service.getCsvSurveyUnit(surveyUnitId, data, questionnaireModelId));
    }
}
