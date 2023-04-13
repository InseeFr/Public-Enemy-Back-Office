package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuestionnaireUseCase {
    private final QuestionnairePort questionnairePort;

    private final QueenUseCase queenUseCase;

    private final DDIUseCase ddiUseCase;

    private final I18nMessagePort messageService;

    public QuestionnaireUseCase(QuestionnairePort questionnairePort, DDIUseCase ddiUseCase, QueenUseCase queenUseCase, I18nMessagePort messageService) {
        this.questionnairePort = questionnairePort;
        this.ddiUseCase = ddiUseCase;
        this.queenUseCase = queenUseCase;
        this.messageService = messageService;
    }

    /**
     * Add questionnaire
     *
     * @param poguesId   pogues questionnaire id
     * @param context    insee context
     * @param csvContent survey unit data file (csv)
     * @return the saved questionnaire
     */
    public Questionnaire addQuestionnaire(String poguesId, Context context, byte[] csvContent) {

        if (questionnairePort.hasQuestionnaire(poguesId)) {
            throw new ServiceException(HttpStatus.CONFLICT, messageService.getMessage("questionnaire.exists", poguesId));
        }
        Ddi ddi = ddiUseCase.getDdi(poguesId);

        Questionnaire questionnaire = new Questionnaire(ddi, context, csvContent);
        questionnaire = questionnairePort.addQuestionnaire(questionnaire);

        queenUseCase.synchronizeCreate(ddi, questionnaire);
        questionnaire.setSynchronized(true);
        // update questionnaire to save the synchronisation state (unsuccessful in case of throwed ServiceException)
        questionnairePort.updateQuestionnaireState(questionnaire);
        return questionnaire;
    }

    /**
     * Get questionnaire
     *
     * @param id questionnaire id
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(Long id) {
        return questionnairePort.getQuestionnaire(id);
    }

    /**
     * Get questionnaire
     *
     * @param poguesId questionnaire pogues id
     * @return a questionnaire based on its pogues id
     */
    public Questionnaire getQuestionnaire(String poguesId) {
        return questionnairePort.getQuestionnaire(poguesId);
    }

    /**
     * Get survey units data from questionnaire
     *
     * @param id questionnaire id
     * @return csv of survey units
     */
    public byte[] getSurveyUnitData(Long id) {
        return questionnairePort.getSurveyUnitData(id);
    }

    /**
     * Get questionnaire list
     *
     * @return the questionnaire list
     */
    public List<Questionnaire> getQuestionnaires() {
        return questionnairePort.getQuestionnaires();
    }

    /**
     * delete questionnaire
     *
     * @param id questionnaire id
     */
    public void deleteQuestionnaire(Long id) {
        Questionnaire questionnaire = questionnairePort.getQuestionnaire(id);
        questionnairePort.deleteQuestionnaire(id);
        queenUseCase.synchronizeDelete(questionnaire);
    }

    /**
     * Update questionnaire
     *
     * @param id             questionnaire id
     * @param context        insee context
     * @param surveyUnitData survey unit data file in csv format
     * @return the saved questionnaire
     */
    public Questionnaire updateQuestionnaire(Long id, Context context, byte[] surveyUnitData) {
        Questionnaire questionnaire = getQuestionnaire(id);
        Ddi ddi = ddiUseCase.getDdi(questionnaire.getPoguesId());
        questionnaire.setContext(context);
        questionnaire.setSurveyUnitData(surveyUnitData);
        questionnaire.setLabel(ddi.label());
        queenUseCase.synchronizeUpdate(ddi, questionnaire);
        questionnaire.setSynchronized(true);
        return questionnairePort.updateQuestionnaire(questionnaire);
    }
}
