package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Ddi;

public interface DdiServicePort {
    /**
     * Get DDI as XML format from Pogues questionnaireId
     * @param questionnaireId
     * @return DDI
     */
    Ddi getDdi(String questionnaireId);
}
