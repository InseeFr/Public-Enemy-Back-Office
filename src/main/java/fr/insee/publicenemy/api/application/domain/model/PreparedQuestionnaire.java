package fr.insee.publicenemy.api.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreparedQuestionnaire {
    private Questionnaire questionnaire;
    private QuestionnaireModel questionnaireModel;
}
