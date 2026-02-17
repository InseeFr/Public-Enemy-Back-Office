package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.PersonalizationState;

import java.util.List;

public record QuestionnaireRest(Long id, String poguesId, String label, ContextRest context, List<ModeRest> modes, PersonalizationState state, Boolean isOutdated){
}
