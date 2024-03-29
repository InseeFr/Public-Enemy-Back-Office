package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.exceptions.SurveyUnitCsvNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
@Slf4j
public class SurveyUnitCsvService implements SurveyUnitCsvPort {
    private final Integer iterationHeaderCount;

    private final I18nMessagePort messageService;

    public SurveyUnitCsvService(@Value("${application.csv.iteration-header-count}") Integer iterationHeaderCount, I18nMessagePort messagePort) {
        this.messageService = messagePort;
        this.iterationHeaderCount = iterationHeaderCount;
    }

    @Override
    public List<SurveyUnit> initSurveyUnits(byte[] surveyUnitData, String questionnaireModelId) {
        List<SurveyUnitCsvLine> surveyUnitsCsvModel = getSurveyUnitsCsvLines(surveyUnitData);

        List<SurveyUnit> surveyUnits = new ArrayList<>();
        for (int id = 1; id <= surveyUnitsCsvModel.size(); id++) {
            SurveyUnitCsvLine surveyUnitCsvLine = surveyUnitsCsvModel.get(id - 1);
            surveyUnits.add(initSurveyUnit(id, surveyUnitCsvLine, questionnaireModelId));
        }
        return surveyUnits;
    }

    @Override
    public SurveyUnit getCsvSurveyUnit(int surveyUnitId, byte[] surveyUnitData, String questionnaireModelId) {
        List<SurveyUnitCsvLine> surveyUnitsCsvModel = getSurveyUnitsCsvLines(surveyUnitData);

        if (surveyUnitId <= 0 || surveyUnitId > surveyUnitsCsvModel.size()) {
            throw new SurveyUnitCsvNotFoundException(messageService.getMessage("surveyunit.csv.not-found"));
        }

        SurveyUnitCsvLine surveyUnitCsvLine = surveyUnitsCsvModel.get(surveyUnitId - 1);
        return initSurveyUnit(surveyUnitId, surveyUnitCsvLine, questionnaireModelId);
    }

    /**
     * @param surveyUnitId         survey unit id
     * @param surveyUnitCsvLine    csv line containing a survey unit
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit from a line in the csv file
     */
    private SurveyUnit initSurveyUnit(int surveyUnitId, @NonNull SurveyUnitCsvLine surveyUnitCsvLine, String questionnaireModelId) {
        String queenIdentifier = surveyUnitId + "";
        if (questionnaireModelId != null && !questionnaireModelId.isEmpty()) {
            SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(questionnaireModelId, surveyUnitId);
            queenIdentifier = identifierHandler.getQueenIdentifier();
        }

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        if (surveyUnitCsvLine.getFields() != null) {
            csvFields = surveyUnitCsvLine.getFields().entries()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
        }

        SurveyUnitData surveyUnitData = new SurveyUnitData(csvFields);
        return new SurveyUnit(queenIdentifier, questionnaireModelId, surveyUnitData, SurveyUnitStateData.createInitialStateData());
    }

    @Override
    public SurveyUnitCsvHeaderLine getSurveyUnitsCsvHeaders(List<VariableType> variablesType) {
        Set<String> csvHeaders = new LinkedHashSet<>();
        variablesType.forEach(
                variableType -> csvHeaders.addAll(getCsvHeaders(variableType)));
        return new SurveyUnitCsvHeaderLine(csvHeaders);
    }

    /**
     * Get CSV Headers for a specific variable type
     *
     * @param variableType variable type
     */
    private Set<String> getCsvHeaders(VariableType variableType) {
        Set<String> csvHeaders = new LinkedHashSet<>();
        if (!variableType.hasMultipleValues()) {
            csvHeaders.add(variableType.name());
            return csvHeaders;
        }

        // if variable type is an iteration, generate a specific number of headers
        for (int index = 1; index <= iterationHeaderCount; index++) {
            csvHeaders.add(variableType.name() + "_" + index);
        }
        return csvHeaders;
    }

    /**
     * get a list of survey units from survey unit csv data
     *
     * @param surveyUnitData survey units csv data
     * @return list of survey units from survey unit csv data
     */
    private List<SurveyUnitCsvLine> getSurveyUnitsCsvLines(byte[] surveyUnitData) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(surveyUnitData));

        return new CsvToBeanBuilder<SurveyUnitCsvLine>(reader)
                .withSkipLines(0)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withStrictQuotes(true)
                .withIgnoreQuotations(false)
                .withType(SurveyUnitCsvLine.class)
                .build().parse();
    }
}
