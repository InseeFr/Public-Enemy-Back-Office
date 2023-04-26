package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;

public record SurveyUnitRest(String id, int displayableId) {
    /**
     * @param surveyUnit survey unit model
     * @return a new survey unit rest from the survey unit model
     */
    public static SurveyUnitRest fromModel(SurveyUnit surveyUnit) {
        String queenIdentifier = surveyUnit.id();
        // split the id to get rid of the questionnaire id part for frontend
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(queenIdentifier);
        return new SurveyUnitRest(queenIdentifier, identifierHandler.getSurveyUnitIdentifier());
    }
}
