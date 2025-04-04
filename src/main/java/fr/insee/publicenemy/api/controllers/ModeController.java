package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static fr.insee.publicenemy.api.configuration.auth.AuthorityRole.HAS_ANY_ROLE;

@RestController
@RequestMapping("/api/modes")
@PreAuthorize(HAS_ANY_ROLE)
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
