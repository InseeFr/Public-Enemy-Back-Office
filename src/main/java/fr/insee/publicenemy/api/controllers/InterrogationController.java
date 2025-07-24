package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvMultilineLimitBrokenException;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationIdentifierHandler;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.*;
import fr.insee.publicenemy.api.controllers.dto.InterrogationErrors;
import fr.insee.publicenemy.api.controllers.dto.InterrogationsRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithMessages;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithInterrogations;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvHeaderLine;
import fr.insee.publicenemy.api.infrastructure.queen.dto.InterrogationDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static fr.insee.publicenemy.api.configuration.auth.AuthorityRole.HAS_ANY_ROLE;

@RestController
@RequestMapping("/api")
@Slf4j
public class InterrogationController {

    private final QueenUseCase queenUseCase;

    private final PoguesUseCase poguesUseCase;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final InterrogationUseCase interrogationUseCase;

    private final InterrogationUseCaseUtils interrogationUtils;

    private final I18nMessagePort messageService;

    private final InterrogationMessagesComponent messageComponent;

    private final ApiExceptionComponent errorComponent;

    private static final String CSV_ERROR_MESSAGE = "CSV Error: ";

    public InterrogationController(QuestionnaireUseCase questionnaireUseCase, QueenUseCase queenUseCase, InterrogationUseCase interrogationUseCase,
                                   I18nMessagePort messageService, InterrogationMessagesComponent messageComponent,
                                   ApiExceptionComponent errorComponent, PoguesUseCase poguesUseCase, InterrogationUseCaseUtils interrogationUtils) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.queenUseCase = queenUseCase;
        this.interrogationUseCase = interrogationUseCase;
        this.interrogationUtils = interrogationUtils;
        this.messageService = messageService;
        this.messageComponent = messageComponent;
        this.errorComponent = errorComponent;
        this.poguesUseCase = poguesUseCase;
    }

    /**
     * @param questionnaireId questionnaire id
     * @param modeName        insee mode
     * @return all interrogations fro the questionnaire
     */
    @GetMapping("/questionnaires/{questionnaireId}/modes/{modeName}/interrogations")
    @PreAuthorize(HAS_ANY_ROLE)
    public InterrogationsRest getInterrogations(@PathVariable Long questionnaireId, @PathVariable String modeName) {

        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(questionnaireId);
        JsonNode nomenclatures = poguesUseCase.getNomenclatureOfQuestionnaire(questionnaire.getPoguesId());
        String campaignId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaireId, Mode.valueOf(modeName));
        List<InterrogationDto> interrogations = queenUseCase.getInterrogations(campaignId);

        return new InterrogationsRest(
                interrogations.stream().map(interrogation -> {
                    try {
                        return interrogationUtils.buildInterrogationRest(
                                interrogation,
                                Mode.valueOf(modeName),
                                nomenclatures);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }).toList());
    }

    /**
     * reset interrogation data/state data
     *
     * @param interrogationId interrogation id
     */
    @PutMapping("/questionnaires/{questionnaireId}/interrogations/{interrogationId}/reset")
    @PreAuthorize(HAS_ANY_ROLE)
    public String resetInterrogation(@PathVariable Long questionnaireId, @PathVariable String interrogationId) {
        byte[] interrogationData = questionnaireUseCase.getInterrogationData(questionnaireId);
        queenUseCase.resetInterrogation(interrogationId, interrogationData);
        return "{}";
    }

    /**
     * @param response http servlet response object
     * @param poguesId questionnaire pogues identifier
     * @throws IOException IO Exception
     */
    @GetMapping("/questionnaires/{poguesId}/csv")
    @PreAuthorize(HAS_ANY_ROLE)
    public void getCsvSchema(HttpServletResponse response, @PathVariable String poguesId) throws IOException {

        // set file name and content type
        String filename = String.format("schema-%s.csv", poguesId);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=\"%s\"", filename));

        CSVWriter writer = new CSVWriter(response.getWriter());
        InterrogationCsvHeaderLine attributes = interrogationUseCase.getHeadersLine(poguesId);
        writer.writeNext(attributes.headers().toArray(String[]::new));
        writer.close();
    }

    /**
     * Check Data from interrogation csv data
     *
     * @param poguesId       questionnaire pogues id
     * @param interrogation interrogation data
     * @return result of checking csv file
     * @throws IOException                            IO Exception
     * @throws InterrogationsGlobalValidationException   global exceptions occurred when validating interrogation data csv file
     * @throws InterrogationsSpecificValidationException specific exceptions occurred when validating interrogation data csv file
     */
    @PostMapping(path = "/questionnaires/{poguesId}/checkdata", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public List<String> checkInterrogationsData(
            @PathVariable String poguesId,
            @RequestPart(name = "interrogationData") @NonNull MultipartFile interrogation) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {
        byte[] csvContent = interrogation.getBytes();
        List<ValidationWarningMessage> validationMessages = interrogationUseCase.validateInterrogations(csvContent, poguesId);

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
    @ExceptionHandler(InterrogationsGlobalValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorWithMessages handleInterrogationsGlobalValidationException(
            InterrogationsGlobalValidationException validationException, WebRequest request) {
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
    public ApiError handleInterrogationsGlobalCSVValidationException(
            CsvException csvException, WebRequest request) {
        String line = String.join(",", csvException.getLine());
        String message = messageService.getMessage("validation.csv.error.message", csvException.getMessage(),
                csvException.getLineNumber() + "", line);
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(CsvRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInterrogationsGlobalCSVValidationException(
            CsvRuntimeException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, csvException.getMessage());
    }

    @ExceptionHandler(CsvMultilineLimitBrokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInterrogationsGlobalCSVValidationException(
            CsvMultilineLimitBrokenException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, csvException.getMessage());
    }

    @ExceptionHandler(CsvMalformedLineException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInterrogationsGlobalCSVValidationException(
            CsvMalformedLineException csvException, WebRequest request) {
        log.warn(CSV_ERROR_MESSAGE, csvException);
        String message = messageService.getMessage("validation.csv.malform.error", csvException.getLineNumber() + "", csvException.getMessage());
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST, message);
    }

    /**
     * @param validationException specific interrogation validation exception
     * @return list of interrogations specific errors
     */
    @ExceptionHandler(InterrogationsSpecificValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorWithInterrogations handleInterrogationsValidationException(
            InterrogationsSpecificValidationException validationException, WebRequest request) {
        List<InterrogationErrors> errors = messageComponent.getErrors(validationException.getInterrogationsErrors());
        return errorComponent.buildApiErrorWithInterrogations(request, validationException.getCode().value(),
                validationException.getMessage(), errors);
    }
}
