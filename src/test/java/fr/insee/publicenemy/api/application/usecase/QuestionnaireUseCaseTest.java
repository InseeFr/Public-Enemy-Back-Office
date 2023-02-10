package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireUseCaseTest {

    @Mock
    private QueenUseCase queenUseCase;
    @Mock
    private QuestionnairePort questionnairePort;
    @Mock
    private DDIUseCase ddiUseCase;
    @Mock
    private Ddi ddi;
    @Mock
    private Questionnaire questionnaire;

    private QuestionnaireUseCase questionnaireUseCase;

    @BeforeEach
    public void init() {
        questionnaireUseCase = new QuestionnaireUseCase(questionnairePort, ddiUseCase, queenUseCase);
    }

    @Test
    void onAddQuestionnaireShouldInvokeCampaignCreationInQueen() {
        String poguesId = "l8wwljbo";
        Context context = Context.BUSINESS;
        when(ddiUseCase.getDdi(any())).thenReturn(ddi);
        when(questionnairePort.addQuestionnaire(any())).thenReturn(questionnaire);
        questionnaireUseCase.addQuestionnaire(poguesId, context, new byte[0]);
        verify(queenUseCase, times(1)).synchronizeCreate(ddi, context, questionnaire);
    }

    @Test
    void onDeleteQuestionnaireShouldInvokeCampaignDeletionInQueen() {
        Long questionnaireId = 1L;
        when(questionnairePort.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        questionnaireUseCase.deleteQuestionnaire(questionnaireId);
        verify(queenUseCase, times(1)).synchronizeDelete(questionnaire);
    }
}
