package fr.insee.publicenemy.api.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;

@RestController
@RequestMapping("/api/contexts")
public class ContextController {

    private final I18nMessagePort i18nMessageService;

    public ContextController(I18nMessagePort i18nMessageService) {
        this.i18nMessageService = i18nMessageService;
    }

    /**
     * 
     * @return all contexts
     */
    @GetMapping("")
    public List<ContextRest> getContexts() {
        return Arrays.stream(Context.values())
                .map(context -> 
                        new ContextRest(context.name(), i18nMessageService.getMessage("context."+context.name().toLowerCase())))
                .toList();
    }
}
