package fr.insee.publicenemy.api.controllers;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitsRest;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class SurveyUnitController {

    private final QueenUseCase queenUseCase;

    private final SurveyUnitCsvUseCase csvUseCase;

    public SurveyUnitController(QueenUseCase queenUseCase, SurveyUnitCsvUseCase csvUseCase) {
        this.queenUseCase = queenUseCase;
        this.csvUseCase = csvUseCase;
    }

    /**
     *
     * @param questionnaireId questionnaire id
     * @param modeName insee mode
     * @return all survey units fro the questionnaire
     */
    @GetMapping("/{questionnaireId}/modes/{modeName}/survey-units")
    public SurveyUnitsRest getSurveyUnits(@PathVariable Long questionnaireId, @PathVariable String modeName) {
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaireId, Mode.valueOf(modeName));
        List<SurveyUnit> surveyUnits = queenUseCase.getSurveyUnits(questionnaireModelId);
        return SurveyUnitsRest.fromModel(surveyUnits, questionnaireModelId);
    }

    @GetMapping("/{poguesId}/csv")
    public void getCsvSchema(HttpServletResponse response, @PathVariable String poguesId) throws IOException {

        // set file name and content type
        String filename = "schema-" + poguesId + ".csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");

        // create a csv writer
        CSVWriter writer = new CSVWriter(response.getWriter(),
                ICSVWriter.DEFAULT_SEPARATOR,
                ICSVWriter.DEFAULT_QUOTE_CHARACTER,
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                ICSVWriter.DEFAULT_LINE_END);

        SurveyUnitCsvHeaderLine headersLine = csvUseCase.getHeadersLine(poguesId);
        // write all employees to csv file
        writer.writeNext(headersLine.headers().toArray(String[]::new));
        writer.close();
    }
}
