package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;

import java.util.List;

public record SurveyUnitsRest(List<SurveyUnitRest> surveyUnits, String questionnaireModelId) {
    /**
     *
     * @param surveyUnits List of survey units model
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit list with questionnaire model id
     */
    public static SurveyUnitsRest fromModel(List<SurveyUnit> surveyUnits, String questionnaireModelId) {
        List<SurveyUnitRest> surveyUnitRests = surveyUnits.stream().map(SurveyUnitRest::fromModel).toList();
        return new SurveyUnitsRest(surveyUnitRests, questionnaireModelId);
    }
}
