package fr.insee.publicenemy.api.infrastructure.csv;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyUnitCsvServiceTest {

    private final SurveyUnitCsvService service = new SurveyUnitCsvService();

    @Mock
    private Questionnaire questionnaire;

    @Test
    void onGetSurveyUnitsReturnCorrectCountNumber() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        when(questionnaire.getSurveyUnitData()).thenReturn(surveyUnitData);

        List<SurveyUnit> surveyUnits = service.initSurveyUnits(questionnaire, questionnaireModelId);

        assertEquals(10, surveyUnits.size());
    }

    @Test
    void onGetSurveyUnitsReturnCorrectSurveyUnitsInfo() throws IOException {
        String questionnaireModelId = "13-CAPI";

        String resourcePath = "src/test/resources/survey-unit-data.csv";
        File file = new File(resourcePath);
        byte[] surveyUnitData = Files.readAllBytes(file.toPath());

        when(questionnaire.getSurveyUnitData()).thenReturn(surveyUnitData);

        List<SurveyUnit> surveyUnits = service.initSurveyUnits(questionnaire, questionnaireModelId);

        SurveyUnit surveyUnit = surveyUnits.get(0);
        Map<String, Object> attributes = surveyUnit.data().getAttributes();

        assertEquals(String.format("%s-%s", questionnaireModelId, "EF000051"), surveyUnit.id());
        assertEquals("1", attributes.get("Numfa"));
        assertEquals("CS 70058", attributes.get("ComplementAdresse"));
    }
}
