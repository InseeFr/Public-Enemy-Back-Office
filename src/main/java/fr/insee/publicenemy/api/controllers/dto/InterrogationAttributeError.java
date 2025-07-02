package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

public record InterrogationAttributeError(String attributeKey, List<String> messages) {
}
