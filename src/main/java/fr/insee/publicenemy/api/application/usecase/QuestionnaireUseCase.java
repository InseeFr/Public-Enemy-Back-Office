package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuestionnaireUseCase {
    private final QuestionnairePort questionnairePort;

    private final QueenUseCase queenUseCase;

    private final PoguesUseCase poguesUseCase;

    private final I18nMessagePort messageService;

    public QuestionnaireUseCase(QuestionnairePort questionnairePort, PoguesUseCase poguesUseCase, QueenUseCase queenUseCase, I18nMessagePort messageService) {
        this.questionnairePort = questionnairePort;
        this.poguesUseCase = poguesUseCase;
        this.queenUseCase = queenUseCase;
        this.messageService = messageService;
    }

    public boolean hasQuestionnaire(String poguesId){
        return questionnairePort.hasQuestionnaire(poguesId);
    }

    /**
     * Add questionnaire
     *
     * @param poguesId   pogues questionnaire id
     * @param context    insee context
     * @param interrogationData survey unit data file (csv)
     * @return the saved questionnaire
     */

    public PreparedQuestionnaire prepareQuestionnaire(String poguesId, Context context, byte[] interrogationData){
        if (hasQuestionnaire(poguesId)) {
            throw new ServiceException(HttpStatus.CONFLICT, messageService.getMessage("questionnaire.exists", poguesId));
        }
        QuestionnaireModel questionnaireModel = poguesUseCase.getQuestionnaireModel(poguesId);
        Questionnaire questionnaire = questionnairePort.addQuestionnaire(new Questionnaire(questionnaireModel, context, interrogationData));
        return new PreparedQuestionnaire(questionnaire, questionnaireModel);
    }

    public PreparedQuestionnaire prepareUpdateQuestionnaire(Long id, Context updatedContext, byte[] updatedInterrogationData){
        Questionnaire questionnaire = getQuestionnaire(id);
        QuestionnaireModel latestQuestionnaireModel = poguesUseCase.getQuestionnaireModel(questionnaire.getPoguesId());
        questionnaire.setContext(updatedContext);
        questionnaire.setInterrogationData(updatedInterrogationData);
        questionnaire.setLabel(latestQuestionnaireModel.label());
        questionnaire.setPersonalizationState(PersonalizationState.STARTED);
        questionnaire.setSynchronized(false);
        questionnairePort.updateQuestionnaireState(questionnaire);
        return new PreparedQuestionnaire(questionnaire, latestQuestionnaireModel);
    }

    @Async
    public void addQuestionnaire(PreparedQuestionnaire preparedQuestionnaire) {
        log.info(String.format("%s: create questionnaire", preparedQuestionnaire.getQuestionnaire().getId()));
        queenUseCase.synchronizeCreateAsync(preparedQuestionnaire.getQuestionnaireModel(), preparedQuestionnaire.getQuestionnaire()).exceptionally(ex -> {
            log.error("Error during creation of personalization");
            preparedQuestionnaire.getQuestionnaire().setPersonalizationState(PersonalizationState.ERROR);
            return null;
        }).thenRun(() -> questionnairePort.updateQuestionnaireState(preparedQuestionnaire.getQuestionnaire()));
    }

    /**
     * Update questionnaire
     *
     * @param preparedQuestionnaire questionnaire id
     * @return the saved questionnaire
     */
    @Async
    public void updateQuestionnaire(PreparedQuestionnaire preparedQuestionnaire) {
        Questionnaire questionnaire = preparedQuestionnaire.getQuestionnaire();
        log.info(String.format("%s: update questionnaire", questionnaire.getPoguesId()));

        queenUseCase.synchronizeUpdateAsync(preparedQuestionnaire.getQuestionnaireModel(), preparedQuestionnaire.getQuestionnaire()).exceptionally(ex -> {
            log.error("Error during updating of personalization");
            preparedQuestionnaire.getQuestionnaire().setPersonalizationState(PersonalizationState.ERROR);
            return null;
        }).thenRun(() -> questionnairePort.updateQuestionnaireState(preparedQuestionnaire.getQuestionnaire()));

    }

    /**
     * Get questionnaire
     *
     * @param id questionnaire id
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(Long id) {
        Questionnaire questionnaire =  questionnairePort.getQuestionnaire(id);
        computeOutdatedAttribute(questionnaire);
        return questionnaire;
    }

    /**
     * Get questionnaire
     *
     * @param poguesId questionnaire pogues id
     * @return a questionnaire based on its pogues id
     */
    public Questionnaire getQuestionnaire(String poguesId) {
        Questionnaire questionnaire = questionnairePort.getQuestionnaire(poguesId);
        computeOutdatedAttribute(questionnaire);
        return questionnaire;
    }

    private void computeOutdatedAttribute(Questionnaire questionnaire){
        QuestionnaireModel latestQuestionnaireModel = poguesUseCase.getQuestionnaireModel(questionnaire.getPoguesId());
        questionnaire.setOutdated(!latestQuestionnaireModel.versionId().equals(questionnaire.getVersionId()));
    }

    /**
     * Get interrogation data from questionnaire
     *
     * @param questionnaireId questionnaire id
     * @return csv of interrogation
     */
    public byte[] getInterrogationData(Long questionnaireId) {
        return questionnairePort.getInterrogationData(questionnaireId);
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
        log.info(String.format("%s: delete questionnaire", questionnaire.getPoguesId()));
        questionnairePort.deleteQuestionnaire(id);
        queenUseCase.synchronizeDelete(questionnaire);
    }


}
