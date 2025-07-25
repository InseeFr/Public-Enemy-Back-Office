package fr.insee.publicenemy.api.application.domain.model.interrogation;

import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;

public record Interrogation(
        String id,
        String questionnaireModelId,
        InterrogationData data,
        InterrogationStateData stateData) {
}
