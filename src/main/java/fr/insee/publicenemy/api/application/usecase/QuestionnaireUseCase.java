package fr.insee.publicenemy.api.application.usecase;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param contextId
     * @param csvContent
     * @return the saved questionnaire
     */
    public Questionnaire addQuestionnaire(String questionnaireId, Long contextId, byte[] csvContent) {
        return questionnairePort.addQuestionnaire(getQuestionnaire(questionnaireId, contextId, csvContent));
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
     * @param questionnaireId
     * @return the questionnaire list
     */
    public List<Questionnaire> getQuestionnaires() {
        return questionnairePort.getQuestionnaires();
    }

    /**
     * get modes
     * @return all modes
     */
    public List<Mode> getModes() {
        return questionnairePort.getModes();
    }

    /**
     * Get contexts
     * @return all contexts
     */
    public List<Context> getContexts() {
        return questionnairePort.getContexts();
    }

    /**
     * delete questionnaire
     * @param questionnaireId
     */
    public void deleteQuestionnaire(Long id) {
        questionnairePort.deleteQuestionnaire(id);
    }

        /**
     * Save questionnaire
     * @param questionnaireId
     * @param contextId
     * @param surveyUnitData
     * @return the saved questionnaire
     */
    public Questionnaire saveQuestionnaire(Long id, Long contextId, byte[] surveyUnitData) {
        Questionnaire questionnaire = new Questionnaire(id, contextId, surveyUnitData);
        return questionnairePort.updateQuestionnaire(questionnaire);
    }

    /**
     * Get questionnaire model
     * @param questionnaireId
     * @param contextId
     * @param csvContent
     * @return the questionnaire model
     */
    private Questionnaire getQuestionnaire(String questionnaireId, Long contextId, byte[] csvContent) {
        Context context = questionnairePort.getContext(contextId);        
        Ddi ddi = ddiUseCase.getDdi(questionnaireId);
        List<String> modesString = ddi.getModes();

        List<Mode> modes = modesString.stream()
                .map(questionnairePort::getModeByName)
                .collect(Collectors.toList());  
        return new Questionnaire(questionnaireId, ddi.getLabel(), context, modes, csvContent);
    }
}
