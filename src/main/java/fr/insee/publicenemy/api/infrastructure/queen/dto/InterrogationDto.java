package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationStateData;
@JsonSerialize(using = InterrogationSerializer.class)
public record InterrogationDto(
        String id,
        String questionnaireId,
        InterrogationData data,
        InterrogationStateData stateData){

    /**
     *
     * @param interrogation survey unit model
     * @return a new survey unit dto from the survey unit model
     */
    public static InterrogationDto fromModel(Interrogation interrogation) {
        return new InterrogationDto(interrogation.id(), interrogation.questionnaireId(), interrogation.data(), interrogation.stateData());
    }
}
