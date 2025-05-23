package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;

import java.util.List;

public interface QueenServicePort {

    /**
     * Add questionnaire model in queen
     *
     * @param questionnaireModelId questionnaire model id
     * @param questionnaireModel                  questionnaire DDI
     * @param jsonLunatic          json lunatic for this questionnaire model
     */
    void createQuestionnaireModel(String questionnaireModelId, QuestionnaireModel questionnaireModel, JsonLunatic jsonLunatic);

    /**
     * Create campaign in queen
     *
     * @param campaignId    campaign id
     * @param questionnaireModel           questionnaire DDI
     * @param questionnaire model questionnaire
     */
    void createCampaign(String campaignId, Questionnaire questionnaire, QuestionnaireModel questionnaireModel);

    /**
     * Delete campaign in queen
     *
     * @param campaignId campaign id
     */
    void deleteCampaign(String campaignId) throws CampaignNotFoundException;

    /**
     * Create survey units for campaign
     *
     * @param questionnaireModelId questionnaire model id
     * @param surveyUnits          survey units to save
     */
    void createSurveyUnits(String questionnaireModelId, List<SurveyUnit> surveyUnits);

    /**
     * @param campaignId campaign id
     * @return list of all survey units for a campaign
     */
    List<SurveyUnit> getSurveyUnits(String campaignId);

    /**
     * @param questionnaireModelId questionnaire model id
     * @return true if questionnaire model exists, false otherwise
     */
    boolean hasQuestionnaireModel(String questionnaireModelId);

    /**
     * Update a survey unit
     *
     * @param surveyUnit survey unit to update
     */
    void updateSurveyUnit(SurveyUnit surveyUnit);

    void deteteSurveyUnit(SurveyUnit surveyUnit);

    void createSurveyUnit(String questionnaireId, SurveyUnit surveyUnit);
}