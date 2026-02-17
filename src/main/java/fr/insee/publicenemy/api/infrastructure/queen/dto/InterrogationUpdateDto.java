package fr.insee.publicenemy.api.infrastructure.queen.dto;

import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = InterrogationUpdateSerializer.class)
public record InterrogationUpdateDto(
        InterrogationData data,
        InterrogationStateData stateData) {

    /**
     * @param interrogation survey unit model
     * @return a new survey unit dto from the survey unit model
     */
    public static InterrogationUpdateDto fromModel(Interrogation interrogation) {
        return new InterrogationUpdateDto(interrogation.data(), interrogation.stateData());
    }
}
