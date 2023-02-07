package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;

import java.util.List;

public interface QueenServicePort {

    /**
     * Add questionnaire model in queen
     * @param questionnaireModelId questionnaire model id
     * @param ddi questionnaire DDI
     * @param jsonLunatic json lunatic for this questionnaire model
     */
    void createQuestionnaireModel(String questionnaireModelId, Ddi ddi, JsonLunatic jsonLunatic);

    /**
     * Create campaign in queen
     * @param campaignId campaign id
     * @param ddi questionnaire DDI
     * @param questionnaire model questionnaire
     */
    void createCampaign(String campaignId, Questionnaire questionnaire, Ddi ddi);

    /**
     * Delete campaign in queen
     * @param campaignId campaign id
     */
    void deleteCampaign(String campaignId);

    /**
     * Create survey units for campaign
     * @param questionnaireModelId questionnaire model id
     * @param surveyUnits survey units to save
     */
    void createSurveyUnits(String questionnaireModelId, List<SurveyUnit> surveyUnits);

    /**
     * @param campaignId campaign id
     * @return list of all survey units for a campaign
     */
    List<SurveyUnit> getSurveyUnits(String campaignId);
}