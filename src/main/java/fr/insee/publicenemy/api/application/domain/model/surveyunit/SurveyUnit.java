package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;

public record SurveyUnit(String id, String questionnaireId, SurveyUnitData data, SurveyUnitStateData stateData) {
}
