package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

public record SurveyUnitsRest(List<SurveyUnitRest> surveyUnits, String questionnaireModelId) {
}
