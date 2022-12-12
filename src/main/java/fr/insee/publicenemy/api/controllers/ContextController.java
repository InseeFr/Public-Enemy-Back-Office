package fr.insee.publicenemy.api.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;

@RestController
@RequestMapping("/api/contexts")
public class ContextController {

    private final QuestionnaireUseCase questionnaireUseCase;

    public ContextController(QuestionnaireUseCase questionnaireUseCase) {
        this.questionnaireUseCase = questionnaireUseCase;
    }

    @GetMapping("")
    public List<ContextRest> getContexts() {
        return questionnaireUseCase.getContexts().stream()
                .map(ContextRest::createFromModel)
                .collect(Collectors.toList());      
    }
}
