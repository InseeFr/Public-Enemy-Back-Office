package fr.insee.publicenemy.api.controllers;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddDto;

@RestController
@RequestMapping("/api/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireUseCase questionnaireUseCase;

    public QuestionnaireController(QuestionnaireUseCase questionnaireUseCase) {
        this.questionnaireUseCase = questionnaireUseCase;
    }

    @PostMapping(path = "/add", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Questionnaire addQuestionnaire(
            @RequestPart(name = "questionnaire", required = true) QuestionnaireAddDto questionnaire,
            @RequestPart(name = "surveyUnitData", required = true) MultipartFile surveyUnitData) throws IOException {        
        
        byte[] csvContent = surveyUnitData.getBytes(); 
        return questionnaireUseCase.addQuestionnaire(questionnaire.getQuestionnaireId(), questionnaire.getContextId(), csvContent);
    }
}
