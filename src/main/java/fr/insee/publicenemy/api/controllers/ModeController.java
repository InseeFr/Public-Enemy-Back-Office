package fr.insee.publicenemy.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.publicenemy.api.application.domain.model.Mode;

@RestController
@RequestMapping("/api/modes")
public class ModeController {
    /**
     * 
     * @return all modes
     */
    @GetMapping("")
    public Mode[] getModes() {
        return Mode.values();     
    }
}
