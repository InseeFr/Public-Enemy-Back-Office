package fr.insee.publicenemy.api.application.ports;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;

import java.util.List;

public interface DdiServicePort {
    /**
     * Get DDI from pogues questionnaire id
     *
     * @param questionnaireId questionnaire id
     * @return DDI
     */
    Ddi getDdi(String questionnaireId);

    /**
     * @param poguesId pogues questionnaire id
     * @return questionnaire details from pogues
     */
    Questionnaire getQuestionnaire(String poguesId);

    /**
     * Get nomenclature
     *
     * @param poguesId questionnaire pogues id
     * @return the nomenclature of questionnaire
     */
    JsonNode getNomenclaturesByQuestionnaire(String poguesId);

    /**
     * Get Json Pogues variables
     *
     * @param poguesId pogues questionnaire Id
     * @return the json from pogues
     */
    List<VariableType> getQuestionnaireVariables(String poguesId);
}
