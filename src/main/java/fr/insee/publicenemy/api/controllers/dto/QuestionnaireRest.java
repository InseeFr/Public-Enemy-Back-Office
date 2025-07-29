package fr.insee.publicenemy.api.controllers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationState;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record QuestionnaireRest(Long id, String poguesId, String label, ContextRest context, List<ModeRest> modes, boolean isSynchronized, PersonalizationState state, boolean isOutdated){
}
