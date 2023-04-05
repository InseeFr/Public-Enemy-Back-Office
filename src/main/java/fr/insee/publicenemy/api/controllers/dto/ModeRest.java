package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;

import java.util.Comparator;
import java.util.List;

public record ModeRest(String name, boolean isWebMode) {
    public static List<ModeRest> fromModesModel(List<Mode> modes) {
        return modes.stream()
                .sorted(Comparator.comparing(Mode::ordinal))
                .map(mode -> new ModeRest(mode.name(), mode.isWebMode()))
                .toList();
    }

    public static List<ModeRest> fromQuestionnaireModesModel(List<QuestionnaireMode> questionnaireModes) {
        List<Mode> modes = questionnaireModes.stream().map(QuestionnaireMode::getMode).toList();
        return fromModesModel(modes);
    }
}
