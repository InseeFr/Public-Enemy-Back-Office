package fr.insee.publicenemy.api.controllers;

import com.opencsv.CSVWriter;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitErrors;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitsRest;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class SurveyUnitController {

    private final QueenUseCase queenUseCase;

    private final SurveyUnitCsvUseCase surveyUnitUseCase;

    private final I18nMessagePort messageService;

    private final SurveyUnitMessagesComponent messageComponent;

    public SurveyUnitController(QueenUseCase queenUseCase, SurveyUnitCsvUseCase surveyUnitUseCase, I18nMessagePort messageService, SurveyUnitMessagesComponent messageComponent) {
        this.queenUseCase = queenUseCase;
        this.surveyUnitUseCase = surveyUnitUseCase;
        this.messageService = messageService;
        this.messageComponent = messageComponent;
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

    /**
     * @param response http servlet response object
     * @param poguesId questionnaire pogues identifier
     * @throws IOException IO Exception
     */
    @GetMapping("/csv/{poguesId}")
    public void getCsvSchema(HttpServletResponse response, @PathVariable String poguesId) throws IOException {

        // set file name and content type
        String filename = "schema-" + poguesId + ".csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");

        // create a csv writer
        CSVWriter writer = new CSVWriter(response.getWriter());

        SurveyUnitCsvHeaderLine attributes = surveyUnitUseCase.getHeadersLine(poguesId);
        // write all employees to csv file
        writer.writeNext(attributes.headers().toArray(String[]::new));
        writer.close();
    }


    /**
     * Check Data from survey unit csv data
     * @param poguesId questionnaire pogues id
     * @param surveyUnitData survey unit data
     * @return result of checking csv file
     * @throws IOException IO Exception
     * @throws SurveyUnitsGlobalValidationException  global exceptions occurred when validating survey unit data csv file
     * @throws SurveyUnitsValidationException specific exceptions occurred when validating survey unit data csv file
     */
    @PostMapping(path = "/csv/{poguesId}/checkdata", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public List<String> checkSurveyUnitsData(
            @PathVariable String poguesId,
            @RequestPart(name = "surveyUnitData") MultipartFile surveyUnitData) throws IOException, SurveyUnitsGlobalValidationException, SurveyUnitsValidationException {
        byte[] csvContent = surveyUnitData.getBytes();
        List<ValidationWarningMessage> validationMessages = surveyUnitUseCase.validateSurveyUnits(csvContent, poguesId);

        return validationMessages.stream()
                .map(message -> messageService.getMessage(message.getCode(), message.getArguments()))
                .toList();
    }

    /**
     * Handle global survey units errors when checking csv data
     * @param validationException global validation exception
     * @return list of error messages
     */
    @ExceptionHandler(SurveyUnitsGlobalValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<String> handleSurveyUnitsGlobalValidationException(
            SurveyUnitsGlobalValidationException validationException) {
        return validationException.getGlobalErrorMessages().stream()
                .map(message -> messageService.getMessage(message.getCode(), message.getArguments()))
                .toList();
    }

    /**
     *
     * @param surveyUnitsValidationException specific survey unit validation exception
     * @return list of survey units specific errors
     */
    @ExceptionHandler(SurveyUnitsValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<SurveyUnitErrors> handleSurveyUnitsValidationException(
            SurveyUnitsValidationException surveyUnitsValidationException) {
        return messageComponent.getErrors(surveyUnitsValidationException.getSurveyUnitsErrors());
    }
}
