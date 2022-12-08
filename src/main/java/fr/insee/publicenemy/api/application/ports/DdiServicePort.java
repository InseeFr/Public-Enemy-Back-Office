package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Ddi;

public interface DdiServicePort {
    public Ddi getDdi(String questionnaireId);
}
