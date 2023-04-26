package fr.insee.publicenemy.api.controllers;

import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvMultilineLimitBrokenException;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitErrors;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitsRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithMessages;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithSurveyUnits;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class SurveyUnitController {

    private final QueenUseCase queenUseCase;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final SurveyUnitCsvUseCase surveyUnitUseCase;

    private final I18nMessagePort messageService;

    private final SurveyUnitMessagesComponent messageComponent;

    private final ApiExceptionComponent errorComponent;

    private static final String CSV_ERROR_MESSAGE = "CSV Error: ";

    public SurveyUnitController(QuestionnaireUseCase questionnaireUseCase, QueenUseCase queenUseCase, SurveyUnitCsvUseCase surveyUnitUseCase,
                                I18nMessagePort messageService, SurveyUnitMessagesComponent messageComponent,
                                ApiExceptionComponent errorComponent) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.queenUseCase = queenUseCase;
        this.surveyUnitUseCase = surveyUnitUseCase;
        this.messageService = messageService;
        this.messageComponent = messageComponent;
        this.errorComponent = errorComponent;
    }

    /**
     * @param questionnaireId questionnaire id
     * @param modeName        insee mode
     * @return all survey units fro the questionnaire
     */
    @GetMapping("/questionnaires/{questionnaireId}/modes/{modeName}/survey-units")
    public SurveyUnitsRest getSurveyUnits(@PathVariable Long questionnaireId, @PathVariable String modeName) {
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaireId, Mode.valueOf(modeName));
        List<SurveyUnit> surveyUnits = queenUseCase.getSurveyUnits(questionnaireModelId);
        return SurveyUnitsRest.fromModel(surveyUnits, questionnaireModelId);
    }

    /**
     * reset survey unit data/state data
     *
     * @param surveyUnitId survey unit id
     */
    @PutMapping("/survey-units/{surveyUnitId}/reset")
    public String resetSurveyUnit(@PathVariable String surveyUnitId) {
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(surveyUnitId);
        byte[] surveyUnitsCsvData = questionnaireUseCase.getSurveyUnitData(identifierHandler.getQuestionnaireId());
        queenUseCase.resetSurveyUnit(surveyUnitId, surveyUnitsCsvData);
        return "{}";
    }

    /**
     * @param response http servlet response object
     * @param poguesId questionnaire pogues identifier
     * @throws IOException IO Exception
     */
    @GetMapping("/questionnaires/{poguesId}/csv")
    public void getCsvSchema(HttpServletResponse response, @PathVariable String poguesId) throws IOException {

        // set file name and content type
        String filename = String.format("schema-%s.csv", poguesId);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=\"%s\"", filename));

        CSVWriter writer = new CSVWriter(response.getWriter());
        SurveyUnitCsvHeaderLine attributes = surveyUnitUseCase.getHeadersLine(poguesId);
        writer.writeNext(attributes.headers().toArray(String[]::new));
        writer.close();
    }


    /**
     * Check Data from survey unit csv data
     *
     * @param poguesId       questionnaire pogues id
     * @param surveyUnitData survey unit data
     * @return result of checking csv file
     * @throws IOException                            IO Exception
     * @throws SurveyUnitsGlobalValidationException   global exceptions occurred when validating survey unit data csv file
     * @throws SurveyUnitsSpecificValidationException specific exceptions occurred when validating survey unit data csv file
     */
    @PostMapping(path = "/questionnaires/{poguesId}/checkdata", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public List<String> checkSurveyUnitsData(
            @PathVariable String poguesId,
            @RequestPart(name = "surveyUnitData") @NonNull MultipartFile surveyUnitData) throws IOException, SurveyUnitsGlobalValidationException, SurveyUnitsSpecificValidationException {
        byte[] csvContent = surveyUnitData.getBytes();
        List<ValidationWarningMessage> validationMessages = surveyUnitUseCase.validateSurveyUnits(csvContent, poguesId);

        return validationMessages.stream()
                .map(message -> messageService.getMessage(message.getCode(), message.getArguments()))
                .toList();
    }

    /**
     * Handle global survey units errors when checking csv data
     *
     * @param validationException global validation exception
     * @return list of error messages
     */
    @ExceptionHandler(SurveyUnitsGlobalValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorWithMessages handleSurveyUnitsGlobalValidationException(
            SurveyUnitsGlobalValidationException validationException, WebRequest request) {
        List<String> errors = validationException.getGlobalErrorMessages().stream()
                .map(message -> messageService.getMessage(message.getCode(), message.getArguments()))
                .toList();
        return errorComponent.buildApiErrorWithMessages(request, validationException.getCode().value(),
                validationException.getMessage(), errors);
    }

    /**
     * @param csvException csv exceptions
     * @return csv parsing errors
     */
    @ExceptionHandler(CsvException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSurveyUnitsGlobalCSVValidationException(
            CsvException csvException, WebRequest request) {
        String line = String.join(",", csvException.getLine());
        String message = messageService.getMessage("validation.csv.error.message", csvException.getMessage(),
                csvException.getLineNumber() + "", line);
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(CsvRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSurveyUnitsGlobalCSVValidationException(
            CsvRuntimeException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, csvException.getMessage());
    }

    @ExceptionHandler(CsvMultilineLimitBrokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSurveyUnitsGlobalCSVValidationException(
            CsvMultilineLimitBrokenException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, csvException.getMessage());
    }

    @ExceptionHandler(CsvMalformedLineException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleSurveyUnitsGlobalCSVValidationException(
            CsvMalformedLineException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        String message = messageService.getMessage("validation.csv.malform.error", csvException.getLineNumber() + "", csvException.getMessage());
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, message);
    }

    /**
     * @param validationException specific survey unit validation exception
     * @return list of survey units specific errors
     */
    @ExceptionHandler(SurveyUnitsSpecificValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorWithSurveyUnits handleSurveyUnitsValidationException(
            SurveyUnitsSpecificValidationException validationException, WebRequest request) {
        List<SurveyUnitErrors> errors = messageComponent.getErrors(validationException.getSurveyUnitsErrors());
        return errorComponent.buildApiErrorWithSurveyUnits(request, validationException.getCode().value(),
                validationException.getMessage(), errors);
    }
}
