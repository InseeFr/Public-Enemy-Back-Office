package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service
@Slf4j
public class QueenSynchronizationUseCase {

        private final QueenServicePort queenService;

        private final DDIUseCase ddiUseCase;

        public QueenSynchronizationUseCase(DDIUseCase ddiUseCase, QueenServicePort queenService) {
            this.ddiUseCase = ddiUseCase;
            this.queenService = queenService;
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
                        String questionnaireModelId = generateIdentifier(questionnaire, mode);
                        JsonLunatic jsonLunatic = ddiUseCase.getJsonLunatic(ddi, context, mode);
                        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_QUESTIONNAIRE.name());
                        queenService.createQuestionnaireModel(questionnaireModelId, ddi, jsonLunatic);
                        questionnaireMode.setSynchronisationState(SynchronisationState.INIT_CAMPAIGN.name());
                        queenService.createCampaign(questionnaireModelId, questionnaire, ddi);
                        questionnaireMode.setSynchronisationState(SynchronisationState.OK.name());
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
                        queenService.deleteCampaign(generateIdentifier(questionnaire, mode));
                    } catch (ServiceException ex) {
                        //!\\ fail silently in case of errors with deletion in queen service
                        // we admit "orphan" campaigns can appear in queen backoffice
                        log.error(ex.toString());
                    }
                });
        }

    /**
     *
     * @param questionnaire Questionnaire on which an identifier will be created
     * @param mode mode
     * @return campaign/questionnaire-model identifier
     */
        private String generateIdentifier(Questionnaire questionnaire, Mode mode) {
            return String.format("%s-%s", questionnaire.getId(), mode.name());
        }

}
