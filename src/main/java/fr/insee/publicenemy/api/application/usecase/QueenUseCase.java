package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * Create campaign in queen, with one questionnaire-model for each mode
     * @param ddi DDI for the questionnaire
     * @param context insee context
     * @param questionnaire questionnaire
     */
    public void synchronizeCreate(Ddi ddi, Context context, Questionnaire questionnaire) {
        // Create questionnaire in queen for the different modes
        questionnaire.getQuestionnaireModes().stream()
            .filter(questionnaireMode -> questionnaireMode.getMode().isWebMode())
            .forEach(questionnaireMode -> {
                    Mode mode = questionnaireMode.getMode();
                    String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode);
                    JsonLunatic jsonLunatic = ddiUseCase.getJsonLunatic(ddi, context, mode);
                    questionnaireMode.setSynchronisationState(SynchronisationState.INIT_QUESTIONNAIRE.name());
                    queenService.createQuestionnaireModel(questionnaireModelId, ddi, jsonLunatic);
                    questionnaireMode.setSynchronisationState(SynchronisationState.INIT_CAMPAIGN.name());
                    queenService.createCampaign(questionnaireModelId, questionnaire, ddi);
                    questionnaireMode.setSynchronisationState(SynchronisationState.OK.name());
                    List<SurveyUnit> surveyUnits = surveyUnitCsvService.initSurveyUnits(questionnaire, questionnaireModelId);
                    queenService.createSurveyUnits(questionnaireModelId, surveyUnits);
            });
        questionnaire.setSynchronized(true);
    }

    /**
     * Delete questionnaire in queen
     * @param questionnaire questionnaire to delete
     */
    public void synchronizeDelete(Questionnaire questionnaire) {
        // Delete questionnaire in queen for the different modes
        questionnaire.getQuestionnaireModes().stream()
            .map(QuestionnaireMode::getMode)
            .filter(Mode::isWebMode)
            .forEach(mode -> {
                try {
                    queenService.deleteCampaign(IdentifierGenerationUtils.generateQueenIdentifier(questionnaire.getId(), mode));
                } catch (ServiceException ex) {
                    //!\\ fail silently in case of errors with deletion in queen service
                    // we admit "orphan" campaigns can appear in queen backoffice
                    log.error(ex.toString());
                }
            });
    }

    /**
     * @param questionnaireModelId questionnaire model id
     * @return all survey units linked oto the questionnaire model id
     */
    public List<SurveyUnit> getSurveyUnits(String questionnaireModelId) {
        return queenService.getSurveyUnits(questionnaireModelId);
    }
}
