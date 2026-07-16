package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.domain.model.pogues.NomenclatureUrl;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;

import java.util.List;

public interface PoguesServicePort {
    /**
     * Get DDI from pogues questionnaire id
     *
     * @param questionnaireId questionnaire id
     * @return DDI
     */
    QuestionnaireModel getQuestionnaireModel(String questionnaireId);

    /**
     * @param poguesId pogues questionnaire id
     * @return questionnaire details from pogues
     */
    Questionnaire getQuestionnaire(String poguesId);

    /**
     * Get nomenclatures with URLs for a questionnaire
     *
     * @param poguesId questionnaire pogues id
     * @return list of nomenclature id+url used by the questionnaire
     */
    List<NomenclatureUrl> getNomenclatureUrls(String poguesId);

    /**
     * Get Json Pogues variables
     *
     * @param poguesId pogues questionnaire Id
     * @return the json from pogues
     */
    List<VariableType> getQuestionnaireVariables(String poguesId);
}
