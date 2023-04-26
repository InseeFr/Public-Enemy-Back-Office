package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
        verify(queenServicePort).createCampaign(any(), eq(questionnaire), eq(ddi));
    }

    @Test
    void onSynchronizeShouldInvokeQuestionnaireModelCreationInQueen() {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;

        QuestionnaireMode questionnaireMode = new QuestionnaireMode(mode);
        when(questionnaire.getQuestionnaireModes()).thenReturn(List.of(questionnaireMode));
        when(ddiUseCase.getJsonLunatic(ddi, context, mode)).thenReturn(jsonLunatic);
        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
        verify(queenServicePort).createQuestionnaireModel(any(), eq(ddi), eq(jsonLunatic));
    }

    @Test
    void onSynchronizeShouldInvokeCampaignCreationInQueenForEachWebMode() {
        Context context = Context.BUSINESS;
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
        verify(queenServicePort, times(modes.size())).createCampaign(any(), eq(questionnaire), eq(ddi));
    }

    @Test
    void onSynchronizeShouldNotInvokeCampaignCreationInQueenForNonWebMode() {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.PAPI);
        Context context = Context.BUSINESS;
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
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

        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
        modes.forEach(mode -> verify(queenServicePort).createQuestionnaireModel(any(), eq(ddi), eq(map.get(mode))));
    }

    @Test
    void onSynchronizeShouldNotInvokeQuestionnaireModelCreationInQueenForNonWebMode() {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CATI, Mode.PAPI);
        Context context = Context.BUSINESS;
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        Mockito.lenient().when(ddiUseCase.getJsonLunatic(ddi, context, Mode.PAPI)).thenReturn(jsonLunatic);
        when(questionnaire.getContext()).thenReturn(context);
        queenUseCase.synchronizeCreate(ddi, questionnaire);
        verify(queenServicePort, times(0)).createQuestionnaireModel(any(), eq(ddi), eq(jsonLunatic));
    }

    @Test
    void onDeleteCampaignShouldInvokeCampaignDeletionInQueen() throws CampaignNotFoundException {
        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        when(questionnaire.getQuestionnaireModes()).thenReturn(List.of(questionnaireMode));
        queenUseCase.synchronizeDelete(questionnaire);
        verify(queenServicePort).deleteCampaign(any());
    }

    @Test
    void onDeleteCampaignShouldInvokeCampaignDeletionInQueenForEachWebMode() throws CampaignNotFoundException {
        List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = modes.stream().map(QuestionnaireMode::new).toList();

        when(questionnaire.getQuestionnaireModes()).thenReturn(questionnaireModes);
        queenUseCase.synchronizeDelete(questionnaire);
        verify(queenServicePort, times(modes.size())).deleteCampaign(any());
    }

    @Test
    void onGetSurveyUnitsReturnSurveyUnits() {
        String questionnaireModelId = "13-CAWI";
        queenUseCase.getSurveyUnits(questionnaireModelId);
        verify(queenServicePort, times(1)).getSurveyUnits(questionnaireModelId);
    }

    @Test
    void onSynchronizeUpdateVerifyOnlyWebModesAreDeleted() {
        String poguesId = "l8wwljbo";
        Long questionnaireId = 1L;
        List<Mode> qModes = List.of(Mode.CAWI, Mode.CAPI, Mode.PAPI);
        List<Mode> ddiModes = List.of();
        List<QuestionnaireMode> questionnaireModes = qModes.stream().map(QuestionnaireMode::new).toList();
        Context context = Context.HOUSEHOLD;
        /*
        when(ddiUseCase.getJsonLunatic(eq(ddi), eq(context), any())).thenReturn(jsonLunatic);*/
        Ddi ddi = new Ddi(poguesId, "Label", ddiModes, "data".getBytes());
        Questionnaire questionnaire = new Questionnaire(questionnaireId, poguesId, "Label", context, questionnaireModes, "data".getBytes(), false);
        queenUseCase.synchronizeUpdate(ddi, questionnaire);
        verify(queenServicePort, times(0)).deleteCampaign("1-PAPI");
        verify(queenServicePort, times(1)).deleteCampaign("1-CAWI");
        verify(queenServicePort, times(1)).deleteCampaign("1-CAPI");
    }

    @Test
    void onSynchronizeUpdateVerifyOnlyWebModesAreAddedOrUpdated() {
        String poguesId = "l8wwljbo";
        List<Mode> qModes = List.of(Mode.PAPI, Mode.CAPI);
        List<Mode> ddiModes = List.of(Mode.PAPI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = qModes.stream().map(QuestionnaireMode::new).toList();
        Context context = Context.HOUSEHOLD;

        Mockito.lenient().when(ddiUseCase.getJsonLunatic(eq(ddi), eq(context), any())).thenReturn(jsonLunatic);
        Ddi ddi = new Ddi(poguesId, "Label", ddiModes, "data".getBytes());
        Questionnaire questionnaire = new Questionnaire(1L, poguesId, "Label", context, questionnaireModes, "data".getBytes(), false);
        queenUseCase.synchronizeUpdate(ddi, questionnaire);

        verify(queenServicePort, times(0)).createCampaign("1-PAPI", questionnaire, ddi);
        verify(queenServicePort, times(1)).createCampaign("1-CATI", questionnaire, ddi);
        verify(queenServicePort, times(1)).createCampaign("1-CAPI", questionnaire, ddi);
    }

    @Test
    void onSynchronizeUpdateVerifyQuestionnaireModesAreDeleted() {
        String poguesId = "l8wwljbo";
        Long questionnaireId = 1L;
        List<Mode> qModes = List.of(Mode.CAWI, Mode.CAPI, Mode.PAPI);
        List<Mode> ddiModes = List.of(Mode.CAWI);
        List<QuestionnaireMode> questionnaireModes = qModes.stream().map(QuestionnaireMode::new).toList();
        Context context = Context.HOUSEHOLD;
        /*
        when(ddiUseCase.getJsonLunatic(eq(ddi), eq(context), any())).thenReturn(jsonLunatic);*/
        Ddi ddi = new Ddi(poguesId, "Label", ddiModes, "data".getBytes());
        Questionnaire questionnaire = new Questionnaire(questionnaireId, poguesId, "Label", context, questionnaireModes, "data".getBytes(), false);
        queenUseCase.synchronizeUpdate(ddi, questionnaire);

        questionnaire.getQuestionnaireModes().stream()
                .map(QuestionnaireMode::getMode)
                .forEach(mode -> assertTrue(ddiModes.contains(mode)));
        assertEquals(ddiModes.size(), questionnaire.getQuestionnaireModes().size());
    }

    @Test
    void onSynchronizeUpdateVerifyQuestionnaireModesAreAdded() {
        String poguesId = "l8wwljbo";
        List<Mode> qModes = List.of(Mode.PAPI, Mode.CAPI);
        List<Mode> ddiModes = List.of(Mode.PAPI, Mode.CAPI, Mode.CATI);
        List<QuestionnaireMode> questionnaireModes = qModes.stream().map(QuestionnaireMode::new).toList();
        Context context = Context.HOUSEHOLD;

        Mockito.lenient().when(ddiUseCase.getJsonLunatic(eq(ddi), eq(context), any())).thenReturn(jsonLunatic);
        Ddi ddi = new Ddi(poguesId, "Label", ddiModes, "data".getBytes());
        Questionnaire questionnaire = new Questionnaire(1L, poguesId, "Label", context, questionnaireModes, "data".getBytes(), false);
        queenUseCase.synchronizeUpdate(ddi, questionnaire);

        for (QuestionnaireMode questionnaireMode : questionnaire.getQuestionnaireModes()) {
            Mode mode = questionnaireMode.getMode();
            assertTrue(ddiModes.contains(mode));
        }
        assertEquals(ddiModes.size(), questionnaire.getQuestionnaireModes().size());
    }

    @Test
    void onResetSurveyUnitCallResetService() {
        String surveyUnitId = "11-CAPI-1";
        byte[] data = "data".getBytes();
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(surveyUnitId);
        SurveyUnit su = new SurveyUnit(surveyUnitId, "11", null, SurveyUnitStateData.createInitialStateData());
        when(surveyUnitServicePort.getCsvSurveyUnit(identifierHandler.getSurveyUnitIdentifier(), data, identifierHandler.getQuestionnaireModelId())).thenReturn(su);
        queenUseCase.resetSurveyUnit(surveyUnitId, data);
        verify(queenServicePort).updateSurveyUnit(su);
    }
}
