package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle synchronisation with queen
 */
@Service
@Slf4j
public class QueenUseCase {

    private final QueenServicePort queenService;

    private final SurveyUnitCsvPort surveyUnitCsvService;

    private final DDIUseCase ddiUseCase;

    public QueenUseCase(DDIUseCase ddiUseCase, QueenServicePort queenService, SurveyUnitCsvPort surveyUnitCsvService) {
        this.ddiUseCase = ddiUseCase;
        this.queenService = queenService;
        this.surveyUnitCsvService = surveyUnitCsvService;
    }

    /**
     * @param questionnaireModelId questionnaire model id
     * @return all survey units linked to the campaign
     */
    public List<SurveyUnit> getSurveyUnits(String questionnaireModelId) {
        return queenService.getSurveyUnits(questionnaireModelId);
    }

    /**
     * reset data/state data for a specific survey unit
     *
     * @param surveyUnitId   survey unit id
     * @param surveyUnitData survey units csv data
     */
    public void resetSurveyUnit(String surveyUnitId, byte[] surveyUnitData) {
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(surveyUnitId);
        SurveyUnit surveyUnit = surveyUnitCsvService.getCsvSurveyUnit(identifierHandler.getSurveyUnitIdentifier(), surveyUnitData, identifierHandler.getQuestionnaireModelId());
        queenService.updateSurveyUnit(surveyUnit);
    }

    /**
     * Create campaign in queen, with one questionnaire-model for each mode
     *
     * @param ddi           DDI for the questionnaire
     * @param questionnaire questionnaire
     */
    public void synchronizeCreate(Ddi ddi, Questionnaire questionnaire) {
        questionnaire.getQuestionnaireModes().stream()
                .filter(questionnaireMode -> questionnaireMode.getMode().isWebMode())
                .forEach(questionnaireMode -> createQueenCampaign(ddi, questionnaire, questionnaireMode));
    }

    /**
     * Update questionnaire in queen
     * This update is used for
     * - simply updating a questionnaire (context/survey unit data)
     * - for updating a not well synchronized questionnaire (problems during previous sync, questionnaire changed in pogues, ...)
     *
     * @param ddi           DDI for the questionnaire
     * @param questionnaire questionnaire
     */
    public void synchronizeUpdate(Ddi ddi, Questionnaire questionnaire) {

        List<Mode> ddiModes = ddi.modes();
        List<QuestionnaireMode> questionnaireModes = new ArrayList<>(questionnaire.getQuestionnaireModes());

        log.info(String.format("%s is synchronized: %b", questionnaire.getPoguesId(), questionnaire.isSynchronized()));

        // retrieve questionnaire modes not in DDI (these modes need to be deleted) and delete them
        questionnaire.getQuestionnaireModes().stream()
                .filter(questionnaireMode -> !ddiModes.contains(questionnaireMode.getMode()))
                .forEach(questionnaireModeToDelete -> {
                    log.info(String.format("%s: mode to delete: %s", questionnaire.getPoguesId(), questionnaireModeToDelete.getMode().name()));
                    questionnaireModes.remove(questionnaireModeToDelete);
                    if (questionnaireModeToDelete.getMode().isWebMode()) {
                        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), questionnaireModeToDelete.getMode());
                        deleteQueenCampaign(questionnaireModelId);
                    }
                });

        List<Mode> modesFromQuestionnaire = questionnaireModes.stream()
                .map(QuestionnaireMode::getMode)
                .toList();

        // get modes that exist in DDI but not in questionnaire (these modes need to be added)
        ddiModes.stream()
                .filter(mode -> !modesFromQuestionnaire.contains(mode))
                .forEach(mode -> {
                    log.info(String.format("%s: mode to add: %s", questionnaire.getPoguesId(), mode.name()));
                    questionnaireModes.add(
                            new QuestionnaireMode(questionnaire.getId(), mode, SynchronisationState.INIT_QUESTIONNAIRE.name()));
                });

        // synchronize created/updated questionnaire web modes
        // created modes will be processed like updated questionnaire modes, as synchronisation with API can cause unexpected errors
        // Often it will cause unnecessary checks for created modes, but synchronisation is safer this way
        questionnaireModes.stream()
                .filter(questionnaireMode -> questionnaireMode.getMode().isWebMode())
                .forEach(questionnaireMode -> {
                    log.info(String.format("%s: mode to update: %s", questionnaire.getPoguesId(), questionnaireMode.getMode().name()));
                    updateQueenCampaign(ddi, questionnaire, questionnaireMode);
                });
        questionnaire.setQuestionnaireModes(questionnaireModes);
    }

    /**
     * Delete questionnaire in queen
     *
     * @param questionnaire questionnaire to delete
     */
    public void synchronizeDelete(Questionnaire questionnaire) {
        // Delete questionnaire in queen for the different modes
        questionnaire.getQuestionnaireModes().stream()
                .map(QuestionnaireMode::getMode)
                .filter(Mode::isWebMode)
                .forEach(mode ->
                        deleteQueenCampaign(IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode)));
    }

    /**
     * Create campaign in queen (questionnaire model, campaign, survey units) and update synchronisation state for questionnaire mode
     *
     * @param ddi               ddi
     * @param questionnaire     questionnaire
     * @param questionnaireMode questionnaire mode
     * @throws CampaignNotFoundException exception thrown if the campaign was not found
     */
    private void createQueenCampaign(Ddi ddi, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) throws CampaignNotFoundException {
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), questionnaireMode.getMode());
        List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(questionnaire.getSurveyUnitData(), questionnaireModelId);
        createQuestionnaireModel(questionnaireModelId, ddi, questionnaire.getContext(), questionnaireMode);
        createCampaign(questionnaireModelId, ddi, questionnaire, questionnaireMode);
        createSurveyUnits(questionnaireModelId, surveyUnits, questionnaireMode);
        questionnaireMode.setSynchronisationState(SynchronisationState.OK.name());
    }

    /**
     * Update queen campaign, resolve synchronisation problems and update synchronisation state for questionnaire mode
     * The update recreates a complete campaign (delete then create) for campaign, questionnaire model and survey units
     *
     * @param ddi               ddi
     * @param questionnaire     questionnaire
     * @param questionnaireMode questionnaire mode
     */
    private void updateQueenCampaign(Ddi ddi, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) {
        Mode mode = questionnaireMode.getMode();
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode);
        List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(questionnaire.getSurveyUnitData(), questionnaireModelId);
        // try to delete campaign if exists
        try {
            log.info(String.format("%s: delete campaign %s", questionnaire.getPoguesId(), questionnaireModelId));
            queenService.deleteCampaign(questionnaireModelId);
            questionnaireMode.setSynchronisationState(null);
        } catch (CampaignNotFoundException ex) {
            // campaign does not exist, we will create it afterwards, no need to throw this exception
            log.debug(ex.getMessage());
        }

        if (!queenService.hasQuestionnaireModel(questionnaireModelId)) {
            log.info(String.format("%s: questionnaire model %s does not exist", questionnaire.getPoguesId(), questionnaireModelId));
            createQuestionnaireModel(questionnaireModelId, ddi, questionnaire.getContext(), questionnaireMode);
        }

        createCampaign(questionnaireModelId, ddi, questionnaire, questionnaireMode);
        createSurveyUnits(questionnaireModelId, surveyUnits, questionnaireMode);
        questionnaireMode.setSynchronisationState(SynchronisationState.OK.name());
    }

    /**
     * Delete completely a campaign (campaign, associated questionnaire model and survey units)
     *
     * @param questionnaireModelId questionnaire model id
     */
    private void deleteQueenCampaign(String questionnaireModelId) {
        log.info(String.format("delete campaign %s", questionnaireModelId));
        try {
            queenService.deleteCampaign(questionnaireModelId);
        } catch (ServiceException | CampaignNotFoundException ex) {
            //!\\ fail silently in case of errors.
            // we admit "orphan" campaigns can appear in queen backoffice
            log.error(ex.toString());
        }
    }

    /**
     * Create a questionnaire model in orchestrator backoffice and update synchronisation state for questionnaire mode
     *
     * @param questionnaireModelId questionnaire model id
     * @param ddi                  ddi
     * @param context              context
     * @param questionnaireMode    questionnaire mode
     */
    private void createQuestionnaireModel(String questionnaireModelId, Ddi ddi, Context context, QuestionnaireMode questionnaireMode) {
        log.info(String.format("create questionnaire model %s", questionnaireModelId));
        JsonLunatic jsonLunatic = ddiUseCase.getJsonLunatic(ddi, context, questionnaireMode.getMode());
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_QUESTIONNAIRE.name());
        queenService.createQuestionnaireModel(questionnaireModelId, ddi, jsonLunatic);
    }

    /**
     * Create campaign in queen and update synchronisation state for questionnaire mode
     *
     * @param questionnaireModelId questionnaire model id
     * @param ddi                  ddi
     * @param questionnaire        questionnaire
     * @param questionnaireMode    questionnaire mode
     */
    private void createCampaign(String questionnaireModelId, Ddi ddi, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) {
        log.info(String.format("create campaign %s", questionnaireModelId));
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_CAMPAIGN.name());
        queenService.createCampaign(questionnaireModelId, questionnaire, ddi);
    }

    /**
     * Create survey units in queen and update synchronisation state for questionnaire mode
     *
     * @param campaignId        campaign id
     * @param surveyUnits       survey units list
     * @param questionnaireMode questionnaire mode
     */
    private void createSurveyUnits(String campaignId, List<SurveyUnit> surveyUnits, QuestionnaireMode questionnaireMode) {
        log.info(String.format("create survey units for campaign %s", campaignId));
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_SURVEY_UNIT.name());
        queenService.createSurveyUnits(campaignId, surveyUnits);
    }
}
