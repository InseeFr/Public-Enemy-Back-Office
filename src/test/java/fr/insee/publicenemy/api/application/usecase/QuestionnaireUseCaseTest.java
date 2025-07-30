package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.PreparedQuestionnaire;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireUseCaseTest {

    @Mock
    private QueenUseCase queenUseCase;
    @Mock
    private QuestionnairePort questionnairePort;

    @Mock
    private I18nMessagePort messagePort;

    @Mock
    private PoguesUseCase poguesUseCase;
    @Mock
    private QuestionnaireModel questionnaireModel;
    @Mock
    private Questionnaire questionnaire;

    private QuestionnaireUseCase questionnaireUseCase;

    @BeforeEach
    public void init() {
        questionnaireUseCase = new QuestionnaireUseCase(questionnairePort, poguesUseCase, queenUseCase, messagePort);
    }

    @Test
    void onAddQuestionnaireShouldInvokeCampaignCreationInQueen() {
        PreparedQuestionnaire preparedQuestionnaire = new PreparedQuestionnaire(questionnaire, questionnaireModel);
        when(queenUseCase.synchronizeCreateAsync(questionnaireModel, questionnaire)).thenReturn(CompletableFuture.runAsync(()-> {}));
        questionnaireUseCase.addQuestionnaire(preparedQuestionnaire);
        verify(queenUseCase, times(1)).synchronizeCreateAsync(questionnaireModel, questionnaire);
    }

    @Test
    void onAddQuestionnaireWhenQuestionnaireAlreadyExistsThrowsException() {
        String poguesId = "l8wwljbo";
        Context context = Context.BUSINESS;
        when(questionnairePort.hasQuestionnaire(poguesId)).thenReturn(true);
        assertThrows(ServiceException.class, () -> questionnaireUseCase.prepareQuestionnaire(poguesId, context, null));
    }

    @Test
    void onDeleteQuestionnaireShouldInvokeCampaignDeletionInQueen() {
        Long questionnaireId = 1L;
        when(questionnairePort.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        questionnaireUseCase.deleteQuestionnaire(questionnaireId);
        verify(queenUseCase, times(1)).synchronizeDelete(questionnaire);
    }

    @Test
    void onUpdateQuestionnaireShouldInvokeCampaignUpdateInQueen() {
        when(queenUseCase.synchronizeUpdateAsync(questionnaireModel, questionnaire)).thenReturn(CompletableFuture.runAsync(()-> {}));
        questionnaireUseCase.updateQuestionnaire(new PreparedQuestionnaire(questionnaire, questionnaireModel));
        verify(queenUseCase, times(1)).synchronizeUpdateAsync(questionnaireModel, questionnaire);
        verify(questionnairePort, times(1)).updateQuestionnaireState(questionnaire);
    }
}
