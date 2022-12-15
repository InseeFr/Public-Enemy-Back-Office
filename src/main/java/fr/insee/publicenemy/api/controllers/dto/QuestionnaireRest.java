package fr.insee.publicenemy.api.controllers.dto;

import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import lombok.Data;

@Data
public class QuestionnaireRest {

    private Long id;

    private String questionnaireId;

    private String label;

    private Context context;

    private List<Mode> modes;

    public static QuestionnaireRest createFromModel(Questionnaire questionnaire) {
        QuestionnaireRest questionnaireRest = new QuestionnaireRest();
        questionnaireRest.setId(questionnaire.getId());
        questionnaireRest.setLabel(questionnaire.getLabel());
        questionnaireRest.setQuestionnaireId(questionnaire.getQuestionnaireId());
        questionnaireRest.setModes(questionnaire.getModes());
        questionnaireRest.setContext(questionnaire.getContext());    
        
        return questionnaireRest;
    }
}
