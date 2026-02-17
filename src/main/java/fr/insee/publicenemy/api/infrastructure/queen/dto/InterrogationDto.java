package fr.insee.publicenemy.api.infrastructure.queen.dto;

import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = InterrogationSerializer.class)
public record InterrogationDto(
        String id,
        String surveyUnitId,
        String questionnaireId,
        InterrogationData data,
        InterrogationStateData stateData){

    /**
     *
     * @param interrogation survey unit model
     * @return a new survey unit dto from the survey unit model
     */
    public static InterrogationDto fromModel(Interrogation interrogation) {
        return new InterrogationDto(interrogation.id(), String.format("su-%s",interrogation.id()), interrogation.questionnaireModelId(), interrogation.data(), interrogation.stateData());
    }
}
