package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

public record SurveyUnitAttributeError(String attributeKey, List<String> messages) {
}
