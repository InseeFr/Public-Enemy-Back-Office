package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

public record SurveyUnitErrors(String surveyUnitId, List<SurveyUnitAttributeError> attributesErrors) {
}
