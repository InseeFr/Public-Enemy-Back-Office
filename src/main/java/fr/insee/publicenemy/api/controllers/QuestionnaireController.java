package fr.insee.publicenemy.api.controllers;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvMultilineLimitBrokenException;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.PoguesUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.InterrogationUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
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
import java.util.List;

import static fr.insee.publicenemy.api.configuration.auth.AuthorityRole.HAS_ANY_ROLE;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class QuestionnaireController {

    private final QuestionnaireUseCase questionnaireUseCase;

    private final InterrogationUseCase csvUseCase;
    private final PoguesUseCase poguesUseCase;

    private final ApiExceptionComponent errorComponent;

    private final I18nMessagePort messageService;

    private final QuestionnaireComponent questionnaireComponent;

    private static final String VALIDATION_ERROR = "validation.errors";

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase, PoguesUseCase poguesUseCase,
                                   InterrogationUseCase csvUseCase,
                                   QuestionnaireComponent questionnaireComponent, I18nMessagePort messagePort,
                                   ApiExceptionComponent errorComponent) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.poguesUseCase = poguesUseCase;
        this.csvUseCase = csvUseCase;
        this.questionnaireComponent = questionnaireComponent;
        this.messageService = messagePort;
        this.errorComponent = errorComponent;
    }

    /**
     * @return all questionnaires
     */
    @GetMapping("")
    @PreAuthorize(HAS_ANY_ROLE)
    public List<QuestionnaireRest> getQuestionnaires() {
        return questionnaireUseCase.getQuestionnaires().stream()
                .map(questionnaireComponent::createFromModel)
                .toList();
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
     * @param poguesId questionnaire pogues id
     * @return questionnaire
     */
    @GetMapping("/{poguesId}/db")
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest getQuestionnaire(@PathVariable String poguesId) {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(poguesId);
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
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
                    .body(interrogationsData);
        }
        return null;
    }

    /**
     * @param poguesId pogues questionnaire id
     * @return questionnaire informations from ddi
     */
    @GetMapping("/pogues/{poguesId}")
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest getQuestionnaireFromPogues(@PathVariable String poguesId) {
        Questionnaire questionnaire = poguesUseCase.getQuestionnaire(poguesId);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param questionnaireRest questionnaire form
     * @param interrogationData    csv content of survey units
     * @return the saved questionnaire
     */
    @PostMapping(path = "/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest addQuestionnaire(
            @RequestPart(name = "questionnaire") QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "interrogationData") MultipartFile interrogationData) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {

        byte[] csvContent = interrogationData.getBytes();

        csvUseCase.validateInterrogations(csvContent, questionnaireRest.poguesId());

        Questionnaire questionnaire = questionnaireUseCase.addQuestionnaire(questionnaireRest.poguesId(), ContextRest.toModel(questionnaireRest.context()), csvContent);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param questionnaireId questionnaire id
     * @param context         insee context
     * @param interrogationData  csv content of survey units
     * @return the updated questionnaire
     */
    @PostMapping(path = "/{questionnaireId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize(HAS_ANY_ROLE)
    public QuestionnaireRest saveQuestionnaire(
            @PathVariable Long questionnaireId,
            @RequestPart(name = "context") ContextRest context,
            @RequestPart(name = "interrogationData", required = false) MultipartFile interrogationData) throws IOException, InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {

        byte[] csvContent = null;
        if (interrogationData != null) {
            csvContent = interrogationData.getBytes();
            csvUseCase.validateInterrogations(csvContent, questionnaireId);
        } else {
            csvContent = questionnaireUseCase.getInterrogationData(questionnaireId);
            csvUseCase.validateInterrogations(csvContent, questionnaireId);
        }
        Questionnaire questionnaire = questionnaireUseCase.updateQuestionnaire(questionnaireId, ContextRest.toModel(context), csvContent);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * Delete questionnaire
     *
     * @param id questionnaire id to delete
     */
    @DeleteMapping(path = "/{id}/delete")
    @PreAuthorize(HAS_ANY_ROLE)
    public String deleteQuestionnaire(
            @PathVariable Long id) {
        questionnaireUseCase.deleteQuestionnaire(id);
        return "{}";
    }


    /**
     * @return generic errors when csv parsing errors
     */
    @ExceptionHandler({InterrogationsGlobalValidationException.class,
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
