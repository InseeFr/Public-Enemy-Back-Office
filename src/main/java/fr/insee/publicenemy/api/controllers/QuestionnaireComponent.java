package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.ModeRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class QuestionnaireComponent {
    private final I18nMessagePort messageService;

    public QuestionnaireComponent(I18nMessagePort messageService) {
        this.messageService = messageService;
    }

    public QuestionnaireRest createFromModel(@NonNull Questionnaire questionnaire) {
        List<ModeRest> modesRest = questionnaire.getQuestionnaireModes().stream()
                .map(QuestionnaireMode::getMode)
                .sorted(Comparator.comparing(Mode::ordinal))
                .map(mode -> new ModeRest(mode.name(), mode.isWebMode()))
                .toList();

        ContextRest contextRest = null;
        if(questionnaire.getContext() != null) {
            String contextName = questionnaire.getContext().name().toLowerCase();
            contextRest = new ContextRest(contextName, messageService.getMessage("context." + contextName));
        }

        return new QuestionnaireRest(questionnaire.getId(), questionnaire.getPoguesId(), questionnaire.getLabel(),
                contextRest, modesRest, questionnaire.isSynchronized());
    }
}
