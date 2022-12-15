package fr.insee.publicenemy.api.application.ports;

import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;

public interface QuestionnairePort {
    /**
     * Add questionnaire
     * @param questionnaire
     * @return saved questionnaire
     */
    Questionnaire addQuestionnaire(Questionnaire questionnaire);

     /**
     * Get questionnaire
     * @param questionnaireId
     * @return  questionnaire
     */
    Questionnaire getQuestionnaire(Long questionnaireId);

    /**
     * Get all questionnaires
     * @return  all questionnaires
     */
    List<Questionnaire> getQuestionnaires();

    /**
     * update questionnaire
     * @param questionnaire
     * @return saved questionnaire
     */
    Questionnaire updateQuestionnaire(Questionnaire questionnaire);

    /**
     * delete questionnaire
     * @param id
     */
    void deleteQuestionnaire(Long id);
}