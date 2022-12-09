package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Ddi;

public interface DdiServicePort {
    /**
     * Get DDI from questionnaireId
     * @param questionnaireId
     * @return DDI
     */
    public Ddi getDdi(String questionnaireId);
}
