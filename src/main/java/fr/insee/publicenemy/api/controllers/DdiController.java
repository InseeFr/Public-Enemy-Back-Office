package fr.insee.publicenemy.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.usecase.DDIUseCase;

@RestController
@RequestMapping("/api/ddi")
public class DdiController {

    @Autowired
    DDIUseCase ddiUseCase;

    @GetMapping("/{questionnaireId}")
    public Ddi getDdi(@PathVariable String questionnaireId) {
        return ddiUseCase.getDdi(questionnaireId);
    }
}
