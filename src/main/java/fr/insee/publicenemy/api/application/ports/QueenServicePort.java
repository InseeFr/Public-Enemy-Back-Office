package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.pogues.NomenclatureUrl;
import fr.insee.publicenemy.api.infrastructure.queen.dto.SimpleInterrogationDto;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
import tools.jackson.databind.JsonNode;

import java.util.List;

public interface QueenServicePort {

    /**
     * Add questionnaire model in queen
     *
     * @param questionnaireModelId questionnaire model id
     * @param questionnaireModel   questionnaire DDI
     * @param jsonLunatic          json lunatic for this questionnaire model
     * @param nomenclatureIds      list of nomenclature ids required by this questionnaire
     */
    void createQuestionnaireModel(String questionnaireModelId, QuestionnaireModel questionnaireModel, JsonLunatic jsonLunatic, List<String> nomenclatureIds);

    /**
     * Create a nomenclature in queen. Fetches content from the provided URL.
     * If the nomenclature already exists, it is skipped.
     *
     * @param nomenclatureUrl nomenclature id and URL to fetch content from
     */
    void createNomenclature(NomenclatureUrl nomenclatureUrl);

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
     * Create interrogations for campaign
     *
     * @param questionnaireModelId questionnaire model id
     * @param interrogations interrogations to save
     */
    void createInterrogations(String questionnaireModelId, List<Interrogation> interrogations);

    /**
     * @param campaignId campaign id
     * @return list of all interrogations for a campaign
     */
    List<SimpleInterrogationDto> getInterrogations(String campaignId);


    /**
     * @param interrogationId interrogation id
     * @return interrogation according interrogation id
     */
    SimpleInterrogationDto getInterrogation(String interrogationId);

    /**
     * @param questionnaireModelId questionnaire model id
     * @return true if questionnaire model exists, false otherwise
     */
    boolean hasQuestionnaireModel(String questionnaireModelId);

    /**
     * Update a interrogation
     *
     * @param interrogation interrogation to update
     */
    void updateInterrogation(Interrogation interrogation);

    void deteteInterrogation(Interrogation interrogation);

    void createInterrogation(String questionnaireId, Interrogation interrogation);

    JsonNode getQuestionnaireModel(String questionnaireModelId);
}