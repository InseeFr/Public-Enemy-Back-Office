package fr.insee.publicenemy.api.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;

@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireUseCase questionnaireUseCase;

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase) {
        this.questionnaireUseCase = questionnaireUseCase;
    }

    @GetMapping("")
    public List<QuestionnaireRest> getQuestionnaires() {
        return questionnaireUseCase.getQuestionnaires().stream()
                .map(QuestionnaireRest::createFromModel)
                .collect(Collectors.toList());      
    }

    @GetMapping("/{id}")
    public QuestionnaireRest getQuestionnaire(@PathVariable Long id) {        
        Questionnaire questionnaire = questionnaireUseCase.getQuestionnaire(id);
        return QuestionnaireRest.createFromModel(questionnaire);
    }

    @PostMapping(path = "/add", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public QuestionnaireRest addQuestionnaire(
            @RequestPart(name = "questionnaire", required = true) QuestionnaireAddRest questionnaireRest,
            @RequestPart(name = "surveyUnitData", required = true) MultipartFile surveyUnitData) throws IOException {        
        
        byte[] csvContent = surveyUnitData.getBytes(); 
        Questionnaire questionnaire = questionnaireUseCase.addQuestionnaire(questionnaireRest.getQuestionnaireId(), questionnaireRest.getContextId(), csvContent);
        return QuestionnaireRest.createFromModel(questionnaire);
    }

    @PostMapping(path = "/{id}/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public QuestionnaireRest saveQuestionnaire(
            @PathVariable Long id,
            @RequestPart(name = "context", required = true) ContextRest contextRest,
            @RequestPart(name = "surveyUnitData", required = true) MultipartFile surveyUnitData) throws IOException {        
        
        byte[] csvContent = surveyUnitData.getBytes(); 
        Questionnaire questionnaire = questionnaireUseCase.saveQuestionnaire(id, contextRest.getId(), csvContent);
        return QuestionnaireRest.createFromModel(questionnaire);
    }

    @DeleteMapping(path = "/{id}/delete")
    public void deleteQuestionnaire(
            @PathVariable Long id) {        
        questionnaireUseCase.deleteQuestionnaire(id);
    }
}
