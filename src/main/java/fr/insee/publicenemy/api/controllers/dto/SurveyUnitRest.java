package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;

public record SurveyUnitRest(String id, String displayableId) {
    /**
     *
     * @param surveyUnit survey unit model
     * @return a new survey unit rest from the survey unit model
     */
    public static SurveyUnitRest fromModel(SurveyUnit surveyUnit) {
        String surveyUnitId= surveyUnit.id();
        // split the id to get rid of the questionnaire id part for frontend
        String surveyUnitIdWithoutQuestionnaireId = IdentifierGenerationUtils.generateSurveyUnitIdentifierFromQueen(surveyUnitId);
        return new SurveyUnitRest(surveyUnitId, surveyUnitIdWithoutQuestionnaireId);
    }
}
