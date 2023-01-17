package fr.insee.publicenemy.api.application.usecase;

import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import org.springframework.stereotype.Service;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.infrastructure.questionnaire.QuestionnaireRepository;

@Service
public class QuestionnaireUseCase {
    private final QuestionnaireRepository questionnairePort;
    private final DDIUseCase ddiUseCase;

    public QuestionnaireUseCase(QuestionnaireRepository questionnairePort, DDIUseCase ddiUseCase) {
        this.questionnairePort = questionnairePort;
        this.ddiUseCase = ddiUseCase;
    }

    /**
     * Add questionnaire
     * @param poguesId pogues questionnaire id
     * @param context insee context
     * @param csvContent survey unit data file (csv)
     * @return the saved questionnaire
     */
    public Questionnaire addQuestionnaire(String poguesId, Context context, byte[] csvContent) {
        return questionnairePort.addQuestionnaire(getQuestionnaire(poguesId, context, csvContent));
    }

    /**
     * Get questionnaire
     * @param id questionnaire id
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(Long id) {
        return questionnairePort.getQuestionnaire(id);
    }

    /**
     * Get questionnaire list
     * @return the questionnaire list
     */
    public List<Questionnaire> getQuestionnaires() {
        return questionnairePort.getQuestionnaires();
    }

    /**
     * delete questionnaire
     * @param id questionnaire id
     */
    public void deleteQuestionnaire(Long id) {
        questionnairePort.deleteQuestionnaire(id);
    }

    /**
     * Save questionnaire
     * @param id questionnaire id
     * @param context insee context
     * @param surveyUnitData survey unit data file in csv format
     * @return the saved questionnaire
     */
    public Questionnaire saveQuestionnaire(Long id, Context context, byte[] surveyUnitData) {
        Questionnaire questionnaire = new Questionnaire(id, context, surveyUnitData);
        return questionnairePort.updateQuestionnaire(questionnaire);
    }

    /**
     * Get questionnaire model
     * @param poguesId pogues questionnaire id
     * @param context insee context
     * @param csvContent survey unit data file in csv format
     * @return the questionnaire model
     */
    private Questionnaire getQuestionnaire(String poguesId, Context context, byte[] csvContent) {
        Ddi ddi = ddiUseCase.getDdi(poguesId);
        return new Questionnaire(poguesId, ddi.label(), context, ddi.modes(), csvContent);
    }
}
