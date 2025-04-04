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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private final PoguesUseCase poguesUseCase;

    private final boolean onlyCAWIMode;

    public QueenUseCase(PoguesUseCase poguesUseCase,
                        QueenServicePort queenService,
                        SurveyUnitCsvPort surveyUnitCsvService,
                        @Value("${application.mode.handle-only-cawi}") boolean onlyCAWIMode) {
        this.poguesUseCase = poguesUseCase;
        this.queenService = queenService;
        this.surveyUnitCsvService = surveyUnitCsvService;
        this.onlyCAWIMode = onlyCAWIMode;
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
        queenService.deteteSurveyUnit(surveyUnit);
        createSurveyUnit(surveyUnit.questionnaireId(), surveyUnit);
    }

    /**
     * Create campaign in queen, with one questionnaire-model for each mode
     *
     * @param questionnaireModel           DDI for the questionnaire
     * @param questionnaire questionnaire
     */
    public void synchronizeCreate(QuestionnaireModel questionnaireModel, Questionnaire questionnaire) {
        questionnaire.getQuestionnaireModes().stream()
                .filter(questionnaireMode -> questionnaireMode.getMode().isWebMode())
                // /!\ filter to process only CAWI in stromae api at this moment, as CAPI/CATI are not integrated
                .filter(questionnaireMode -> isModeAllowed(questionnaireMode.getMode()))
                .forEach(questionnaireMode -> {
                    try {
                        createQueenCampaign(questionnaireModel, questionnaire, questionnaireMode);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Update questionnaire in queen
     * This update is used for
     * - simply updating a questionnaire (context/survey unit data)
     * - for updating a not well synchronized questionnaire (problems during previous sync, questionnaire changed in pogues, ...)
     *
     * @param questionnaireModel           DDI for the questionnaire
     * @param questionnaire questionnaire
     */
    public void synchronizeUpdate(QuestionnaireModel questionnaireModel, Questionnaire questionnaire) {

        List<Mode> modes = questionnaireModel.modes();
        List<QuestionnaireMode> questionnaireModes = new ArrayList<>(questionnaire.getQuestionnaireModes());

        log.info(String.format("%s is synchronized: %b", questionnaire.getPoguesId(), questionnaire.isSynchronized()));

        // retrieve questionnaire modes not in DDI (these modes need to be deleted) and delete them
        questionnaire.getQuestionnaireModes().stream()
                .filter(questionnaireMode -> !modes.contains(questionnaireMode.getMode()))
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
        modes.stream()
                .filter(mode -> !modesFromQuestionnaire.contains(mode))
                // /!\ filter to process only CAWI in stromae api at this moment, as CAPI/CATI are not integrated
                .filter(mode -> isModeAllowed(Mode.CAWI))
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
                // /!\ filter to process only CAWI in stromae api at this moment, as CAPI/CATI are not integrated
                .filter(questionnaireMode -> isModeAllowed(questionnaireMode.getMode()))
                .forEach(questionnaireMode -> {
                    log.info(String.format("%s: mode to update: %s", questionnaire.getPoguesId(), questionnaireMode.getMode().name()));
                    try {
                        updateQueenCampaign(questionnaireModel, questionnaire, questionnaireMode);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
                // /!\ filter to process only CAWI in stromae api at this moment, as CAPI/CATI are not integrated
                .filter(this::isModeAllowed)
                .forEach(mode ->
                        deleteQueenCampaign(IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode)));
    }

    /**
     * Create campaign in queen (questionnaire model, campaign, survey units) and update synchronisation state for questionnaire mode
     *
     * @param questionnaireModel               questionnaireModel
     * @param questionnaire     questionnaire
     * @param questionnaireMode questionnaire mode
     * @throws CampaignNotFoundException exception thrown if the campaign was not found
     */
    private void createQueenCampaign(QuestionnaireModel questionnaireModel, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) throws CampaignNotFoundException, IOException {
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), questionnaireMode.getMode());
        List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(questionnaire.getSurveyUnitData(), questionnaireModelId);
        createQuestionnaireModel(questionnaireModelId, questionnaireModel, questionnaire.getContext(), questionnaireMode);
        createCampaign(questionnaireModelId, questionnaireModel, questionnaire, questionnaireMode);
        createSurveyUnits(questionnaireModelId, surveyUnits, questionnaireMode);
        questionnaireMode.setSynchronisationState(SynchronisationState.OK.name());
    }

    /**
     * Update queen campaign, resolve synchronisation problems and update synchronisation state for questionnaire mode
     * The update recreates a complete campaign (delete then create) for campaign, questionnaire model and survey units
     *
     * @param questionnaireModel               questionnaireModel
     * @param questionnaire     questionnaire
     * @param questionnaireMode questionnaire mode
     */
    private void updateQueenCampaign(QuestionnaireModel questionnaireModel, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) throws IOException {
        Mode mode = questionnaireMode.getMode();
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode);
        List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(questionnaire.getSurveyUnitData(), questionnaireModelId);
        // try to delete campaign if exists
        try {
            log.info(String.format("%s: delete campaign %s", questionnaire.getPoguesId(), questionnaireModelId));
            surveyUnits.forEach(queenService::deteteSurveyUnit);
            queenService.deleteCampaign(questionnaireModelId);
            questionnaireMode.setSynchronisationState(null);
        } catch (CampaignNotFoundException ex) {
            // campaign does not exist, we will create it afterwards, no need to throw this exception
            log.debug(ex.getMessage());
        }

        createQueenCampaign(questionnaireModel, questionnaire, questionnaireMode);
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
     * @param questionnaireModel                  questionnaireModel
     * @param context              context
     * @param questionnaireMode    questionnaire mode
     */
    private void createQuestionnaireModel(String questionnaireModelId, QuestionnaireModel questionnaireModel, Context context, QuestionnaireMode questionnaireMode) throws IOException {
        log.info(String.format("create questionnaire model %s", questionnaireModelId));
        JsonLunatic jsonLunatic = poguesUseCase.getJsonLunatic(questionnaireModel, context, questionnaireMode.getMode());
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_QUESTIONNAIRE.name());
        queenService.createQuestionnaireModel(questionnaireModelId, questionnaireModel, jsonLunatic);
    }

    /**
     * Create campaign in queen and update synchronisation state for questionnaire mode
     *
     * @param questionnaireModelId questionnaire model id
     * @param questionnaireModel                  questionnaireModel
     * @param questionnaire        questionnaire
     * @param questionnaireMode    questionnaire mode
     */
    private void createCampaign(String questionnaireModelId, QuestionnaireModel questionnaireModel, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) {
        log.info(String.format("create campaign %s", questionnaireModelId));
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_CAMPAIGN.name());
        queenService.createCampaign(questionnaireModelId, questionnaire, questionnaireModel);
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

    private void createSurveyUnit(String campaignId, SurveyUnit surveyUnit) {
        log.info(String.format("create survey unit %s for campaign %s", surveyUnit.id(), campaignId));
        queenService.createSurveyUnit(campaignId, surveyUnit);
    }

    private boolean isModeAllowed(Mode mode) {
        if(!onlyCAWIMode) {
            return true;
        }
        if(mode.equals(Mode.CAWI)) {
            return true;
        }
        return false;
    }
}
