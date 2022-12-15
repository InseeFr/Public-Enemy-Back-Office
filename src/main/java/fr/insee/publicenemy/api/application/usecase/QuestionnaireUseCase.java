package fr.insee.publicenemy.api.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Mode;
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
     * @param questionnaireId
     * @param context
     * @param csvContent
     * @return the saved questionnaire
     */
    public Questionnaire addQuestionnaire(String questionnaireId, Context context, byte[] csvContent) {
        return questionnairePort.addQuestionnaire(getQuestionnaire(questionnaireId, context, csvContent));
    }

    /**
     * Get questionnaire
     * @param questionnaireId
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(Long questionnaireId) {
        return questionnairePort.getQuestionnaire(questionnaireId);
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
     * @param id
     */
    public void deleteQuestionnaire(Long id) {
        questionnairePort.deleteQuestionnaire(id);
    }

    /**
     * Save questionnaire
     * @param id
     * @param context
     * @param surveyUnitData
     * @return the saved questionnaire
     */
    public Questionnaire saveQuestionnaire(Long id, Context context, byte[] surveyUnitData) {
        Questionnaire questionnaire = new Questionnaire(id, context, surveyUnitData);
        return questionnairePort.updateQuestionnaire(questionnaire);
    }

    /**
     * Get questionnaire model
     * @param questionnaireId
     * @param context
     * @param csvContent
     * @return the questionnaire model
     */
    private Questionnaire getQuestionnaire(String questionnaireId, Context context, byte[] csvContent) {
        Ddi ddi = ddiUseCase.getDdi(questionnaireId);
        List<String> modesString = ddi.getModes();

        List<Mode> modes = modesString.stream()
                .map(Mode::valueOf).toList();  
        return new Questionnaire(questionnaireId, ddi.getLabel(), context, modes, csvContent);
    }
}
