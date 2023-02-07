package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class QueenUseCaseTest {
    @Mock
    private QueenServicePort queenServicePort;
    @Mock
    private SurveyUnitCsvPort surveyUnitServicePort;
    @Mock
    private DDIUseCase ddiUseCase;
    @Mock
    private Ddi ddi;
    @Mock
    private Questionnaire questionnaire;
    @Mock
    private JsonLunatic jsonLunatic;
    private QueenUseCase queenUseCase;

    @BeforeEach
    public void init() {
        queenUseCase = new QueenUseCase(ddiUseCase, queenServicePort, surveyUnitServicePort);
    }

    @Test
    void onSynchronizeShouldInvokeCampaignCreationInQueen() {
        Context context = Context.BUSINESS;
        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        when(questionnaire.getQuestionnaireModes()).thenReturn(List.of(questionnaireMode));
        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        verify(queenServicePort).createCampaign(any(), eq(questionnaire), eq(ddi));
    }

    @Test
    void onSynchronizeShouldInvokeQuestionnaireModelCreationInQueen() {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;

        QuestionnaireMode questionnaireMode = new QuestionnaireMode(mode);
        when(questionnaire.getQuestionnaireModes()).thenReturn(List.of(questionnaireMode));
        when(ddiUseCase.getJsonLunatic(ddi, context, mode)).thenReturn(jsonLunatic);
        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        verify(queenServicePort).createQuestionnaireModel(any(), eq(ddi), eq(jsonLunatic));
    }

    @Test
    void onSynchronizeShouldInvokeCampaignCreationInQueenForEachWebMode() {
        Context context = Context.BUSINESS;
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        verify(queenServicePort, times(modes.size())).createCampaign(any(), eq(questionnaire), eq(ddi));
    }

    @Test
    void onSynchronizeShouldNotInvokeCampaignCreationInQueenForNonWebMode() {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.PAPI);
        Context context = Context.BUSINESS;
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        verify(queenServicePort, times(2)).createCampaign(any(), eq(questionnaire), eq(ddi));
    }

    @Test
    void onSynchronizeShouldInvokeQuestionnaireModelCreationInQueenForEachWebMode() {
        Map<Mode, JsonLunatic> map = new HashMap<>();
        map.put(Mode.CAWI, new JsonLunatic("{\"id\":\"1\"}"));
        map.put(Mode.CAPI, new JsonLunatic("{\"id\":\"2\"}"));
        List<Mode> modes = new ArrayList<>(map.keySet());
        Context context = Context.BUSINESS;
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        modes.forEach(mode -> when(ddiUseCase.getJsonLunatic(ddi, context, mode)).thenReturn(map.get(mode)));

        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        modes.forEach(mode -> verify(queenServicePort).createQuestionnaireModel(any(), eq(ddi), eq(map.get(mode))));
    }

    @Test
    void onSynchronizeShouldNotInvokeQuestionnaireModelCreationInQueenForNonWebMode() {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CATI, Mode.PAPI);
        Context context = Context.BUSINESS;
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        Mockito.lenient().when(ddiUseCase.getJsonLunatic(ddi, context, Mode.PAPI)).thenReturn(jsonLunatic);
        queenUseCase.synchronizeCreate(ddi, context, questionnaire);
        verify(queenServicePort, times(0)).createQuestionnaireModel(any(), eq(ddi), eq(jsonLunatic));
    }

    @Test
    void onDeleteCampaignShouldInvokeCampaignDeletionInQueen() {
        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        when(questionnaire.getQuestionnaireModes()).thenReturn(List.of(questionnaireMode));
        queenUseCase.synchronizeDelete(questionnaire);
        verify(queenServicePort).deleteCampaign(any());
    }

    @Test
    void onDeleteCampaignShouldInvokeCampaignDeletionInQueenForEachWebMode() {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        queenUseCase.synchronizeDelete(questionnaire);
        verify(queenServicePort, times(modes.size())).deleteCampaign(any());
    }
}
