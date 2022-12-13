package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;
import java.util.stream.Collectors;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionnaireRest {

    private Long id;

    private String questionnaireId;

    private String label;

    private ContextRest context;

    private List<ModeRest> modes;

    public static QuestionnaireRest createFromModel(Questionnaire questionnaire) {
        QuestionnaireRest questionnaireRest = new QuestionnaireRest();
        questionnaireRest.setId(questionnaire.getId());
        questionnaireRest.setLabel(questionnaire.getLabel());
        questionnaireRest.setQuestionnaireId(questionnaire.getQuestionnaireId());
        List<ModeRest> modes = questionnaire.getModes().stream().map(mode -> ModeRest.createFromModel(mode)).collect(Collectors.toList());
        questionnaireRest.setModes(modes);
        questionnaireRest.setContext(ContextRest.createFromModel(questionnaire.getContext()));    
        
        return questionnaireRest;
    }
}
