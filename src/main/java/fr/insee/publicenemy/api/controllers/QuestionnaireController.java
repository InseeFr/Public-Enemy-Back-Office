package fr.insee.publicenemy.api.controllers;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvMultilineLimitBrokenException;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.insee.publicenemy.api.application.domain.model.PreparedQuestionnaire;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.InterrogationUseCase;
import fr.insee.publicenemy.api.application.usecase.PoguesUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithInterrogations;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithMessages;
import fr.insee.publicenemy.api.controllers.exceptions.dto.InterrogationError;
import fr.insee.publicenemy.api.infrastructure.questionnaire.RepositoryEntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static fr.insee.publicenemy.api.configuration.auth.AuthorityRole.HAS_ANY_ROLE;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class QuestionnaireController {

    private final QuestionnaireUseCase questionnaireUseCase;

    private final InterrogationUseCase interroUseCase;
    private final PoguesUseCase poguesUseCase;

    private final ApiExceptionComponent errorComponent;

    private final I18nMessagePort messageService;

    private final QuestionnaireComponent questionnaireComponent;
    private final InterrogationMessagesComponent messageComponent;
    private final boolean webSocketEnabled;

    private static final String VALIDATION_ERROR = "validation.errors";

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase, PoguesUseCase poguesUseCase,
                                   InterrogationUseCase interroUseCase,
                                   QuestionnaireComponent questionnaireComponent, I18nMessagePort messagePort,
                                   ApiExceptionComponent errorComponent,
                                   InterrogationMessagesComponent messageComponent,
                                   @Value("${feature.webSocket.enabled}") boolean webSocketEnabled) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.poguesUseCase = poguesUseCase;
        this.interroUseCase = interroUseCase;
        this.questionnaireComponent = questionnaireComponent;
        this.messageService = messagePort;
        this.errorComponent = errorComponent;
        this.messageComponent = messageComponent;
        this.webSocketEnabled = webSocketEnabled;
    }

    /**
     * @param poguesId questionnaire id
     * @return questionnaire
     */
    @GetMapping("/{poguesId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest getQuestionnaire(@PathVariable String poguesId) {
        try {
            Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(poguesId);
            return questionnaireComponent.createFromModel(questionnaire);
        } catch (RepositoryEntityNotFoundException e) {
            Questionnaire questionnaire = poguesUseCase.getQuestionnaire(poguesId);
            return questionnaireComponent.createFromModel(questionnaire);
        }
    }



    /**
     * @param poguesId questionnaire id
     * @return questionnaire
     */
    @GetMapping(value = "/{poguesId}/data")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<byte[]> getInterrogationData(@PathVariable String poguesId) {

        byte[] interrogationsData = questionnaireUseCase.getInterrogationData(poguesId);
        InterrogationData.FormatType dataFormat = InterrogationData.getDataFormat(interrogationsData);
        if (dataFormat != null) {
            String filename = String.format("questionnaire-%s-data.%s", poguesId, dataFormat.name().toLowerCase());
            String contentType =  InterrogationData.FormatType.JSON.equals(dataFormat) ? MediaType.APPLICATION_JSON_VALUE : "text/csv";
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(interrogationsData);
        }
        return null;
    }

    /**
     * @param questionnaireRest questionnaire form
     * @param interrogationData    csv content of survey units
     * @return the saved questionnaire
     */
    @PostMapping(path = "", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<QuestionnaireRest> addQuestionnaire(
            @RequestPart(name = "questionnaire") QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "interrogationData") MultipartFile interrogationData) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {

        byte[] dataContent = interrogationData.getBytes();

        interroUseCase.validateInterrogations(dataContent, questionnaireRest.poguesId());

        PreparedQuestionnaire prepareQuestionnaire = questionnaireUseCase.prepareQuestionnaire(
                questionnaireRest.poguesId(),
                ContextRest.toModel(questionnaireRest.context()),
                dataContent);

        if(webSocketEnabled){
            // Async method
            questionnaireUseCase.addQuestionnaireAsync(prepareQuestionnaire);
        } else {
            questionnaireUseCase.addQuestionnaire(prepareQuestionnaire);
        }
        return ResponseEntity.accepted().body(questionnaireComponent.createFromModel(prepareQuestionnaire.getQuestionnaire()));
    }


    /**
     * @param interrogationData  csv/json content of survey units
     * @return the updated questionnaire
     */
    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<QuestionnaireRest> updateQuestionnaire(
            @RequestPart(name = "questionnaire") QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "interrogationData", required = false) MultipartFile interrogationData) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {

        byte[] dataContent;
        if (interrogationData != null) {
            dataContent = interrogationData.getBytes();
        } else {
            dataContent = questionnaireUseCase.getInterrogationData(questionnaireRest.poguesId());
        }
        interroUseCase.validateInterrogations(dataContent, questionnaireRest.poguesId());

        PreparedQuestionnaire prepareQuestionnaire = questionnaireUseCase.prepareUpdateQuestionnaire(
                questionnaireRest.poguesId(),
                ContextRest.toModel(questionnaireRest.context()),
                dataContent);

        if(webSocketEnabled){
            // Async method
            questionnaireUseCase.updateQuestionnaireAsync(prepareQuestionnaire);
        } else {
            questionnaireUseCase.updateQuestionnaire(prepareQuestionnaire);
        }

        return ResponseEntity.accepted().body(questionnaireComponent.createFromModel(prepareQuestionnaire.getQuestionnaire()));
    }

    /**
     * Delete questionnaire
     *
     * @param poguesId questionnaire id to delete
     */
    @DeleteMapping(path = "/{poguesId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public String deleteQuestionnaire(@PathVariable String poguesId) {
        questionnaireUseCase.deleteQuestionnaire(poguesId);
        return "{}";
    }


    /**
     * @return generic errors when csv parsing errors
     */
    @ExceptionHandler({
            CsvException.class,
            CsvRuntimeException.class,
            CsvMultilineLimitBrokenException.class,
            CsvMalformedLineException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCsvValidationException(WebRequest request) {
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST,
                messageService.getMessage(VALIDATION_ERROR));
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
     * @param validationException specific interrogation validation exception
     * @return list of interrogations specific errors
     */
    @ExceptionHandler(InterrogationsSpecificValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorWithInterrogations handleInterrogationsValidationException(
            InterrogationsSpecificValidationException validationException, WebRequest request) {
        List<InterrogationError> errors = messageComponent.getErrors(validationException.getInterrogationsErrors());
        return errorComponent.buildApiErrorWithInterrogations(request, validationException.getCode().value(),
                validationException.getMessage(), errors);
    }
}
