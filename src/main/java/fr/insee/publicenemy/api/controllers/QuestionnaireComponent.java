package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.ModeRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionnaireComponent {
    private final I18nMessagePort messageService;

    public QuestionnaireComponent(I18nMessagePort messageService) {
        this.messageService = messageService;
    }

    /**
     * @param questionnaire rest questionnaire
     * @return the model for this rest questionnaire
     */
    public QuestionnaireRest createFromModel(@NonNull Questionnaire questionnaire) {
        List<ModeRest> modesRest = ModeRest.fromQuestionnaireModesModel(questionnaire.getQuestionnaireModes());

        ContextRest contextRest = null;
        if (questionnaire.getContext() != null) {
            String contextName = questionnaire.getContext().name();
            contextRest = new ContextRest(contextName, messageService.getMessage("context." + contextName.toLowerCase()));
        }

        return new QuestionnaireRest(questionnaire.getId(), questionnaire.getPoguesId(), questionnaire.getLabel(),
                contextRest, modesRest, questionnaire.isSynchronized());
    }
}
