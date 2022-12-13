package fr.insee.publicenemy.api.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.ModeRest;

@RestController
@RequestMapping("/api/modes")
public class ModeController {

    private final QuestionnaireUseCase questionnaireUseCase;

    public ModeController(QuestionnaireUseCase questionnaireUseCase) {
        this.questionnaireUseCase = questionnaireUseCase;
    }

    @GetMapping("")
    public List<ModeRest> getModes() {
        return questionnaireUseCase.getModes().stream()
                .map(ModeRest::createFromModel)
                .collect(Collectors.toList());      
    }
}
