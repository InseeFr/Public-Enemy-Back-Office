package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;

public interface DdiServicePort {
    /**
     * Get DDI as XML format from Pogues questionnaireId
     * @param questionnaireId questionnaire id
     * @return DDI
     */
    Ddi getDdi(String questionnaireId);

    /**
     * 
     * @param poguesId pogues questionnaire id
     * @return questionnaire details from pogues
     */
    Questionnaire getQuestionnaire(String poguesId);
}
