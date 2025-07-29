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
import fr.insee.publicenemy.api.infrastructure.questionnaire.RepositoryEntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    private static final String VALIDATION_ERROR = "validation.errors";

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase, PoguesUseCase poguesUseCase,
                                   InterrogationUseCase interroUseCase,
                                   QuestionnaireComponent questionnaireComponent, I18nMessagePort messagePort,
                                   ApiExceptionComponent errorComponent) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.poguesUseCase = poguesUseCase;
        this.interroUseCase = interroUseCase;
        this.questionnaireComponent = questionnaireComponent;
        this.messageService = messagePort;
        this.errorComponent = errorComponent;
    }


    /**
     * @return questionnaire perso by poguesId
     */
    @GetMapping("")
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest getQuestionnairesByPoguesId(@RequestParam String poguesId) {
        try {
            Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(poguesId);
            return questionnaireComponent.createFromModel(questionnaire);
        } catch (RepositoryEntityNotFoundException e) {
            Questionnaire questionnaire = poguesUseCase.getQuestionnaire(poguesId);
            return questionnaireComponent.createFromModel(questionnaire);
        }

    }

    /**
     * @param id questionnaire id
     * @return questionnaire
     */
    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest getQuestionnaire(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(id);
        return questionnaireComponent.createFromModel(questionnaire);
    }



    /**
     * @param id questionnaire id
     * @return questionnaire
     */
    @GetMapping(value = "/{id}/data")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<byte[]> getInterrogationData(@PathVariable Long id) {

        byte[] interrogationsData = questionnaireUseCase.getInterrogationData(id);
        InterrogationData.FormatType dataFormat = InterrogationData.getDataFormat(interrogationsData);
        if (dataFormat != null) {
            String filename = String.format("questionnaire-%s-data.%s", id, dataFormat.name().toLowerCase());
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

        // Async method
        questionnaireUseCase.addQuestionnaire(prepareQuestionnaire);
        return ResponseEntity.accepted().body(questionnaireComponent.createFromModel(prepareQuestionnaire.getQuestionnaire()));
    }


    /**
     * @param questionnaireId questionnaire id
     * @param interrogationData  csv/json content of survey units
     * @return the updated questionnaire
     */
    @PutMapping(path = "/{questionnaireId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<QuestionnaireRest> updateQuestionnaire(
            @PathVariable Long questionnaireId,
            @RequestPart(name = "questionnaire") QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "interrogationData", required = false) MultipartFile interrogationData) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {

        byte[] dataContent;
        if (interrogationData != null) {
            dataContent = interrogationData.getBytes();
        } else {
            dataContent = questionnaireUseCase.getInterrogationData(questionnaireId);
        }
        interroUseCase.validateInterrogations(dataContent, questionnaireId);

        PreparedQuestionnaire prepareQuestionnaire = questionnaireUseCase.prepareUpdateQuestionnaire(
                questionnaireId,
                ContextRest.toModel(questionnaireRest.context()),
                dataContent);

        // Async method
        questionnaireUseCase.updateQuestionnaire(prepareQuestionnaire);
        return ResponseEntity.accepted().body(questionnaireComponent.createFromModel(prepareQuestionnaire.getQuestionnaire()));
    }

    /**
     * Delete questionnaire
     *
     * @param id questionnaire id to delete
     */
    @DeleteMapping(path = "/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public String deleteQuestionnaire(@PathVariable Long id) {
        questionnaireUseCase.deleteQuestionnaire(id);
        return "{}";
    }


    /**
     * @return generic errors when csv parsing errors
     */
    @ExceptionHandler({
            InterrogationsGlobalValidationException.class,
            CsvException.class,
            CsvRuntimeException.class,
            InterrogationsSpecificValidationException.class,
            CsvMultilineLimitBrokenException.class,
            CsvMalformedLineException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCsvValidationException(WebRequest request) {
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST,
                messageService.getMessage(VALIDATION_ERROR));
    }
}
