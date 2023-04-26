package fr.insee.publicenemy.api.controllers;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import com.opencsv.exceptions.CsvMultilineLimitBrokenException;
import com.opencsv.exceptions.CsvRuntimeException;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.DDIUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class QuestionnaireController {

    private final QuestionnaireUseCase questionnaireUseCase;

    private final SurveyUnitCsvUseCase csvUseCase;
    private final DDIUseCase ddiUseCase;

    private final ApiExceptionComponent errorComponent;

    private final I18nMessagePort messageService;

    private final QuestionnaireComponent questionnaireComponent;

    private static final String VALIDATION_ERROR = "validation.errors";

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase, DDIUseCase ddiUseCase,
                                   SurveyUnitCsvUseCase csvUseCase,
                                   QuestionnaireComponent questionnaireComponent, I18nMessagePort messagePort,
                                   ApiExceptionComponent errorComponent) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.ddiUseCase = ddiUseCase;
        this.csvUseCase = csvUseCase;
        this.questionnaireComponent = questionnaireComponent;
        this.messageService = messagePort;
        this.errorComponent = errorComponent;
    }

    /**
     * @return all questionnaires
     */
    @GetMapping("")
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
    public QuestionnaireRest getQuestionnaire(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(id);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param poguesId questionnaire pogues id
     * @return questionnaire
     */
    @GetMapping("/{poguesId}/db")
    public QuestionnaireRest getQuestionnaire(@PathVariable String poguesId) {
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(poguesId);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param id questionnaire id
     * @return questionnaire
     */
    @GetMapping(value = "/{id}/data", produces = "text/csv")
    public ResponseEntity<byte[]> getSurveyUnitData(@PathVariable Long id) {
        String filename = String.format("questionnaire-%s-data.csv", id);

        byte[] surveyUnitsData = questionnaireUseCase.getSurveyUnitData(id);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
                .body(surveyUnitsData);
    }

    /**
     * @param poguesId pogues questionnaire id
     * @return questionnaire informations from ddi
     */
    @GetMapping("/pogues/{poguesId}")
    public QuestionnaireRest getQuestionnaireFromPogues(@PathVariable String poguesId) {
        Questionnaire questionnaire = ddiUseCase.getQuestionnaire(poguesId);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param questionnaireRest questionnaire form
     * @param surveyUnitData    csv content of survey units
     * @return the saved questionnaire
     */
    @PostMapping(path = "/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public QuestionnaireRest addQuestionnaire(
            @RequestPart(name = "questionnaire") QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "surveyUnitData") MultipartFile surveyUnitData) throws IOException, SurveyUnitsGlobalValidationException, SurveyUnitsSpecificValidationException {

        byte[] csvContent = surveyUnitData.getBytes();

        csvUseCase.validateSurveyUnits(csvContent, questionnaireRest.poguesId());

        Questionnaire questionnaire = questionnaireUseCase.addQuestionnaire(questionnaireRest.poguesId(), ContextRest.toModel(questionnaireRest.context()), csvContent);
        return questionnaireComponent.createFromModel(questionnaire);
    }

    /**
     * @param questionnaireId questionnaire id
     * @param context         insee context
     * @param surveyUnitData  csv content of survey units
     * @return the updated questionnaire
     */
    @PostMapping(path = "/{questionnaireId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public QuestionnaireRest saveQuestionnaire(
            @PathVariable Long questionnaireId,
            @RequestPart(name = "context") ContextRest context,
            @RequestPart(name = "surveyUnitData", required = false) MultipartFile surveyUnitData) throws IOException, SurveyUnitsGlobalValidationException, SurveyUnitsSpecificValidationException {

        byte[] csvContent = null;
        if (surveyUnitData != null) {
            csvContent = surveyUnitData.getBytes();
            csvUseCase.validateSurveyUnits(csvContent, questionnaireId);
        } else {
            csvContent = questionnaireUseCase.getSurveyUnitData(questionnaireId);
            csvUseCase.validateSurveyUnits(csvContent, questionnaireId);
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
    public String deleteQuestionnaire(
            @PathVariable Long id) {
        questionnaireUseCase.deleteQuestionnaire(id);
        return "{}";
    }


    /**
     * @return generic errors when csv parsing errors
     */
    @ExceptionHandler({SurveyUnitsGlobalValidationException.class,
            CsvException.class,
            CsvRuntimeException.class,
            SurveyUnitsSpecificValidationException.class,
            CsvMultilineLimitBrokenException.class,
            CsvMalformedLineException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCsvValidationException(WebRequest request) {
        return errorComponent.buildApiErrorObject(request, HttpStatus.BAD_REQUEST,
                messageService.getMessage(VALIDATION_ERROR));
    }
}
