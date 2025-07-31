package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;

import java.util.List;

public interface QuestionnairePort {
    /**
     * Add questionnaire
     *
     * @param questionnaire questionnaire to save
     * @return saved questionnaire
     */
    Questionnaire addQuestionnaire(Questionnaire questionnaire);

    /**
     * Get questionnaire
     *
     * @param questionnaireId questionnaire id
     * @return questionnaire
     */
    Questionnaire getQuestionnaire(Long questionnaireId);

    /**
     * Get questionnaire
     *
     * @param poguesId questionnaire pogues id
     * @return the questionnaire
     */
    Questionnaire getQuestionnaire(String poguesId);

    /**
     * Get all questionnaires
     *
     * @return all questionnaires
     */
    List<Questionnaire> getQuestionnaires();

    /**
     * update questionnaire
     *
     * @param questionnaire questionnaire id
     * @return saved questionnaire
     */
    Questionnaire updateQuestionnaire(Questionnaire questionnaire);

    /**
     * delete questionnaire
     *
     * @param id questionnaire id to delete
     */
    void deleteQuestionnaire(Long id);
    /**
     * delete questionnaire
     *
     * @param poguesId id to delete
     */
    void deleteQuestionnaire(String poguesId);

    /**
     * update questionnaire synchronisation state
     *
     * @param questionnaire questionnaire that needs state update
     */
    Questionnaire updateQuestionnaireState(Questionnaire questionnaire);

    /**
     * @param poguesId questionnaire pogues id
     * @return true if questionnaire exist, false otherwise
     */
    boolean hasQuestionnaire(String poguesId);

    /**
     * Get questionnaire
     *
     * @param questionnaireId questionnaire id
     * @return survey units csv data
     */
    byte[] getInterrogationData(Long questionnaireId);

    /**
     * Get questionnaire
     *
     * @param poguesId questionnaire id
     * @return survey units csv data
     */
    byte[] getInterrogationData(String poguesId);
}