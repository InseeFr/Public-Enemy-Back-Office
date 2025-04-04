package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Context;
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
        String poguesId = "l8wwljbo";
        Context context = Context.BUSINESS;
        when(poguesUseCase.getQuestionnaireModel(any())).thenReturn(questionnaireModel);
        when(questionnairePort.hasQuestionnaire(poguesId)).thenReturn(false);
        when(questionnairePort.addQuestionnaire(any())).thenReturn(questionnaire);
        questionnaireUseCase.addQuestionnaire(poguesId, context, new byte[0]);
        verify(queenUseCase, times(1)).synchronizeCreate(questionnaireModel, questionnaire);
    }

    @Test
    void onAddQuestionnaireWhenQuestionnaireAlreadyExistsThrowsException() {
        String poguesId = "l8wwljbo";
        Context context = Context.BUSINESS;
        when(questionnairePort.hasQuestionnaire(poguesId)).thenReturn(true);
        assertThrows(ServiceException.class, () -> questionnaireUseCase.addQuestionnaire(poguesId, context, new byte[0]));
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
        Long questionnaireId = 1L;
        when(questionnairePort.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(poguesUseCase.getQuestionnaireModel(any())).thenReturn(questionnaireModel);
        questionnaireUseCase.updateQuestionnaire(questionnaireId, Context.BUSINESS, "data".getBytes());
        verify(queenUseCase, times(1)).synchronizeUpdate(questionnaireModel, questionnaire);
        verify(questionnairePort, times(1)).updateQuestionnaire(questionnaire);
        verify(questionnaire).setSynchronized(true);
    }
}
