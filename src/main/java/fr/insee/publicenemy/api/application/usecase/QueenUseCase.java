package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.application.ports.InterrogationJsonPort;
import fr.insee.publicenemy.api.application.ports.PersonalizationPort;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.infrastructure.queen.dto.InterrogationDto;
import fr.insee.publicenemy.api.infrastructure.queen.dto.InterrogationSurveyUnitDto;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Handle synchronisation with queen
 */
@Service
@Slf4j
public class QueenUseCase {

    private final QueenServicePort queenService;

    private final InterrogationCsvPort interrogationCsvService;
    private final InterrogationJsonPort interrogationJsonService;
    private final PersonalizationPort personalizationService;

    private final PoguesUseCase poguesUseCase;

    public QueenUseCase(PoguesUseCase poguesUseCase,
                        QueenServicePort queenService,
                        InterrogationCsvPort interrogationCsvService,
                        InterrogationJsonPort interrogationJsonService,
                        PersonalizationPort personalizationService) {
        this.poguesUseCase = poguesUseCase;
        this.queenService = queenService;
        this.interrogationCsvService = interrogationCsvService;
        this.interrogationJsonService = interrogationJsonService;
        this.personalizationService = personalizationService;
    }

    /**
     * @param campaignId questionnaire model id
     * @return all interrogations linked to the campaign
     */
    public List<InterrogationDto> getInterrogations(String campaignId) {
        return queenService.getInterrogations(campaignId);
    }

    /**
     *
     * @param interrogationId id of interrogation
     * @return interrogation object according interrogationId
     */
    public InterrogationDto getInterrogation(String interrogationId){
        return queenService.getInterrogation(interrogationId);
    }

    /**
     * reset data/state data for a specific interrogation
     *
     * @param personalizationMapping
     * @param interrogationData interrogations csv data
     */
    public void resetInterrogation(PersonalizationMapping personalizationMapping, byte[] interrogationData) {
        Interrogation interrogation;
        InterrogationData.FormatType dataFormat = InterrogationData.getDataFormat(interrogationData);
        if(InterrogationData.FormatType.CSV.equals(dataFormat)){
            interrogation = interrogationCsvService.getCsvInterrogation(
                    personalizationMapping,
                    interrogationData);
        } else if(InterrogationData.FormatType.JSON.equals(dataFormat)) {
            interrogation = interrogationJsonService.getJsonInterrogation(
                    personalizationMapping,
                    interrogationData);
        } else {
            throw new ServiceException(HttpStatus.NOT_ACCEPTABLE, "Invalid format of data");
        }

        String campaignId = IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(
                personalizationMapping.questionnaireId(),
                personalizationMapping.mode());
        queenService.deteteInterrogation(interrogation);
        createInterrogation(campaignId, interrogation);
    }

    /**
     * Create campaign in queen, with one questionnaire-model for each mode
     *
     * @param questionnaireModel           DDI for the questionnaire
     * @param questionnaire questionnaire
     */

    public void synchronizeCreate(QuestionnaireModel questionnaireModel, Questionnaire questionnaire) {
        questionnaire.getQuestionnaireModes().stream()
                .filter(qm -> qm.getMode().isWebMode())
                .forEach(qm -> createQueenCampaign(questionnaireModel, questionnaire, qm));
        questionnaire.setPersonalizationState(PersonalizationState.COMPLETED);
    }

    public CompletableFuture<Void> synchronizeCreateAsync(QuestionnaireModel questionnaireModel, Questionnaire questionnaire) {
        return CompletableFuture.runAsync(() -> synchronizeCreate(questionnaireModel, questionnaire));
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

        // retrieve questionnaire modes not in DDI (these modes need to be deleted) and delete them
        questionnaire.getQuestionnaireModes().stream()
                .filter(questionnaireMode -> !modes.contains(questionnaireMode.getMode()))
                .forEach(questionnaireModeToDelete -> {
                    log.info(String.format("%s: mode to delete: %s", questionnaire.getPoguesId(), questionnaireModeToDelete.getMode().name()));
                    questionnaireModes.remove(questionnaireModeToDelete);
                    if (questionnaireModeToDelete.getMode().isWebMode()) {
                        String questionnaireModelId = IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(questionnaire.getId(), questionnaireModeToDelete.getMode());
                        deleteQueenCampaign(questionnaireModelId);
                    }
                });

        List<Mode> modesFromQuestionnaire = questionnaireModes.stream()
                .map(QuestionnaireMode::getMode)
                .toList();

        // get modes that exist in DDI but not in questionnaire (these modes need to be added)
        modes.stream()
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
                    updateQueenCampaign(questionnaireModel, questionnaire, questionnaireMode);
                });
        questionnaire.setQuestionnaireModes(questionnaireModes);
        questionnaire.setPersonalizationState(PersonalizationState.COMPLETED);
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
    public CompletableFuture<Void> synchronizeUpdateAsync(QuestionnaireModel questionnaireModel, Questionnaire questionnaire) {
        return CompletableFuture.runAsync(()-> synchronizeUpdate(questionnaireModel, questionnaire));
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
                        deleteQueenCampaign(IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(questionnaire.getId(), mode)));
    }

    /**
     * Create campaign in queen (questionnaire model, campaign, survey units) and update synchronisation state for questionnaire mode
     *
     * @param questionnaireModel               questionnaireModel
     * @param questionnaire     questionnaire
     * @param questionnaireMode questionnaire mode
     * @throws CampaignNotFoundException exception thrown if the campaign was not found
     */
    private void createQueenCampaign(QuestionnaireModel questionnaireModel, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) throws CampaignNotFoundException {
        String questionnaireModelId = IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(questionnaire.getId(), questionnaireMode.getMode());
        List<Interrogation> interrogations = List.of();
        InterrogationData.FormatType dataFormat = InterrogationData.getDataFormat(questionnaire.getInterrogationData());
        if(InterrogationData.FormatType.CSV.equals(dataFormat)){
            interrogations = interrogationCsvService.initInterrogations(questionnaire.getInterrogationData(), questionnaireModelId);
        } else if(InterrogationData.FormatType.JSON.equals(dataFormat)) {
            interrogations = interrogationJsonService.initInterrogations(questionnaire.getInterrogationData(), questionnaireModelId);
        } else {
            log.warn("Invalid format of data");
        }
        createQuestionnaireModel(questionnaireModelId, questionnaireModel, questionnaire.getContext(), questionnaireMode);
        createCampaign(questionnaireModelId, questionnaireModel, questionnaire, questionnaireMode);
        createInterrogations(questionnaireModelId, interrogations, questionnaireMode);
        createPersonalizationMappings(interrogations, questionnaire.getId(), questionnaireMode.getMode(), questionnaireMode);
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
    private void updateQueenCampaign(QuestionnaireModel questionnaireModel, Questionnaire questionnaire, QuestionnaireMode questionnaireMode) {
        Mode mode = questionnaireMode.getMode();
        String questionnaireModelId = IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(questionnaire.getId(), mode);
       // try to delete campaign if exists
        try {
            log.info(String.format("%s: delete campaign %s", questionnaire.getPoguesId(), questionnaireModelId));
            queenService.deleteCampaign(questionnaireModelId);
            personalizationService.deletePersonalizationMappingsByQuestionnaireIdAndMode(questionnaire.getId(), questionnaireMode.getMode());
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
    private void createQuestionnaireModel(String questionnaireModelId, QuestionnaireModel questionnaireModel, Context context, QuestionnaireMode questionnaireMode) {
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
     * Create interrogations in queen and update synchronisation state for questionnaire mode
     *
     * @param campaignId        campaign id
     * @param interrogations       interrogations list
     * @param questionnaireMode questionnaire mode
     */
    private void createInterrogations(String campaignId, List<Interrogation> interrogations, QuestionnaireMode questionnaireMode) {
        log.info(String.format("create interrogations for campaign %s", campaignId));
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_SURVEY_UNIT.name());
        queenService.createInterrogations(campaignId, interrogations);
    }

    private void createInterrogation(String campaignId, Interrogation interrogation) {
        log.info(String.format("create interrogation %s for campaign %s", interrogation.id(), campaignId));
        queenService.createInterrogation(campaignId, interrogation);
    }

    private void createPersonalizationMappings(List<Interrogation> interrogations, Long questionnaireId, Mode mode, QuestionnaireMode questionnaireMode){
        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_PERSO_MAPPING.name());
        IntStream.range(0, interrogations.size())
                .mapToObj(index -> new PersonalizationMapping(
                        interrogations.get(index).id(),
                        questionnaireId,
                        mode,
                        index)
                )
                .forEach(personalizationService::addPersonalizationMapping);
    }
}
