package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnitData;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SurveyUnitCsvService implements SurveyUnitCsvPort {
    @Override
    public List<SurveyUnit> initSurveyUnits(@NonNull Questionnaire questionnaire, @NonNull String questionnaireModelId) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(questionnaire.getSurveyUnitData()));

        List<SurveyUnitCsvLine> surveyUnitsCsvModel = new CsvToBeanBuilder<SurveyUnitCsvLine>(reader)
                .withSkipLines(0)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withStrictQuotes(true)
                .withIgnoreQuotations(false)
                .withType(SurveyUnitCsvLine.class)
                .build().parse();

        List<SurveyUnit> surveyUnits = new ArrayList<>();
        for(SurveyUnitCsvLine surveyUnitCsvLine : surveyUnitsCsvModel) {
            surveyUnits.add(getSurveyUnit(surveyUnitCsvLine, questionnaireModelId));
        }
        return surveyUnits;
    }

    /**
     *
     * @param surveyUnitCsvLine csv line containing a survey unit
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit from a line in the csv file
     */
    private SurveyUnit getSurveyUnit(@NonNull SurveyUnitCsvLine surveyUnitCsvLine, String questionnaireModelId) {

        String surveyUnitId = String.format("%s-%s", questionnaireModelId, surveyUnitCsvLine.getIdUnit());
        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        if(surveyUnitCsvLine.getFields() != null) {
            csvFields = surveyUnitCsvLine.getFields().entries()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
        }

        SurveyUnitData surveyUnitData = new SurveyUnitData(csvFields);
        surveyUnitData.addAttribute("IdUe", surveyUnitId);
        return new SurveyUnit(surveyUnitId, questionnaireModelId, surveyUnitData, SurveyUnitStateData.createInitialStateData());
    }
}
