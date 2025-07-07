package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

public record InterrogationErrors(String interrogationId, List<InterrogationAttributeError> attributesErrors) {
}
