package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.Context;

public record ContextRest(String name, String value) {

    public static Context toModel(ContextRest contextRest) {
        return Context.valueOf(contextRest.name());
    }
}
