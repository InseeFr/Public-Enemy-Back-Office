package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.DDIUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.ModeRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionnaireController.class)
@ContextConfiguration(classes = QuestionnaireController.class)
@Slf4j
class QuestionnaireControllerTest {
    @MockBean
    private QuestionnaireUseCase questionnaireUseCase;

    @MockBean
    private SurveyUnitCsvUseCase surveyUnitCsvUseCase;

    @MockBean
    private I18nMessagePort messageService;

    @MockBean
    private ApiExceptionComponent errorComponent;

    @MockBean
    private QuestionnaireComponent questionnaireComponent;

    @MockBean
    private DDIUseCase ddiUseCase;

    @Autowired
    private MockMvc mockMvc;
    private List<Questionnaire> questionnaires;

    private Questionnaire questionnaire;

    private QuestionnaireRest questionnaireRest;

    @BeforeEach
    public void init() {
        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        List<QuestionnaireMode> questionnaireModes = List.of(questionnaireMode);
        List<ModeRest> modesRest = List.of(new ModeRest(Mode.CAPI.name(), Mode.CAWI.isWebMode()));
        ContextRest contextRest = new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name());

        questionnaires = new LinkedList<>();
        List<QuestionnaireRest> questionnairesRest = new LinkedList<>();
        for (long nbQuestionnaires = 0; nbQuestionnaires < 3; nbQuestionnaires++) {
            Long id = nbQuestionnaires + 1;
            Questionnaire q = new Questionnaire(id, "l8wwljbo" + id, "label" + id, Context.BUSINESS,
                    questionnaireModes, "data".getBytes(), false);
            QuestionnaireRest qRest = new QuestionnaireRest(q.getId(), q.getPoguesId(),
                    q.getLabel(), contextRest, modesRest, q.isSynchronized());
            questionnaires.add(q);
            questionnairesRest.add(qRest);

            when(questionnaireComponent.createFromModel(q)).thenReturn(qRest);
        }
        questionnaire = questionnaires.get(0);
        questionnaireRest = questionnairesRest.get(0);
    }

    @Test
    void onGetQuestionnairesShouldFetchAllQuestionnaires() throws Exception {

        when(questionnaireUseCase.getQuestionnaires()).thenReturn(questionnaires);

        mockMvc.perform(get("/api/questionnaires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(questionnaires.size())));
    }

    @Test
    void onGetQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {
        Long id = questionnaire.getId();
        when(questionnaireUseCase.getQuestionnaire(id)).thenReturn(questionnaire);

        mockMvc.perform(get("/api/questionnaires/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaireRest.id().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaireRest.poguesId())))
                .andExpect(jsonPath("$.label", is(questionnaireRest.label())))
                .andExpect(jsonPath("$.context.name", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.context.value", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaireRest.modes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaireRest.isSynchronized())));
    }

    @Test
    void onGetSurveyUnitsDataReturnCSV() throws Exception {
        Long id = 1L;
        byte[] data = "\"att1\",\"att2\"".getBytes();
        when(questionnaireUseCase.getSurveyUnitData(id)).thenReturn(data);

        MvcResult result = mockMvc.perform(get("/api/questionnaires/{id}/data", id))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(new String(data), result.getResponse().getContentAsString());
    }

    @Test
    void onAddQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {
        QuestionnaireAddRest questionnaireAddRest = new QuestionnaireAddRest("l8wwljbo", new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] surveyUnitData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireAddRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);

        when(questionnaireUseCase.addQuestionnaire(questionnaireAddRest.poguesId(), Context.BUSINESS, surveyUnitData)).thenReturn(questionnaire);

        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(multipart("/api/questionnaires/add").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaireRest.id().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaireRest.poguesId())))
                .andExpect(jsonPath("$.label", is(questionnaireRest.label())))
                .andExpect(jsonPath("$.context.name", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.context.value", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaireRest.modes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaireRest.isSynchronized())));
    }

    @Test
    void onGetQuestionnaireFromPoguesShouldFetchQuestionnaireAttributes() throws Exception {
        String poguesId = questionnaire.getPoguesId();
        when(ddiUseCase.getQuestionnaire(poguesId)).thenReturn(questionnaire);

        mockMvc.perform(get("/api/questionnaires/pogues/{poguesId}", poguesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poguesId", is(questionnaireRest.poguesId())))
                .andExpect(jsonPath("$.label", is(questionnaireRest.label())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaireRest.modes().size())));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = {"filecontent"})
    void onSaveQuestionnaireShouldFetchQuestionnaireAttributes(String surveyUnitData) throws Exception {

        ObjectMapper Obj = new ObjectMapper();
        String jsonContext = Obj.writeValueAsString(questionnaireRest.context());
        MockPart contextMockPart = new MockPart("context", jsonContext.getBytes());
        contextMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", surveyUnitData, MediaType.MULTIPART_FORM_DATA_VALUE, questionnaire.getSurveyUnitData());

        Long id = questionnaire.getId();
        when(questionnaireUseCase.updateQuestionnaire(questionnaire.getId(),
                questionnaire.getContext(), questionnaire.getSurveyUnitData())).thenReturn(questionnaire);

        mockMvc.perform(multipart("/api/questionnaires/{id}", id).part(contextMockPart).file(surveyUnitMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaireRest.id().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaireRest.poguesId())))
                .andExpect(jsonPath("$.label", is(questionnaireRest.label())))
                .andExpect(jsonPath("$.context.name", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.context.value", is(questionnaireRest.context().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaireRest.modes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaireRest.isSynchronized())));
    }

    @Test
    void onDeleteQuestionnaireShouldReturnEmptyJsonObject() throws Exception {
        Long id = questionnaire.getId();

        mockMvc.perform(delete("/api/questionnaires/{id}/delete", id))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void onAddQuestionnaireWhenGlobalErrorsOnCsvSchemaReturnGenericErrorMessages() throws Exception {
        QuestionnaireAddRest questionnaireAddRest = new QuestionnaireAddRest("l8wwljbo", new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] surveyUnitData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireAddRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file",
                MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String code = "error.code";
        when(messageService.getMessage("validation.errors")).thenReturn(code);
        SurveyUnitsGlobalValidationException surveyUnitsValidationException = new SurveyUnitsGlobalValidationException("main error message", new ArrayList<>());
        when(surveyUnitCsvUseCase.validateSurveyUnits(surveyUnitData, "l8wwljbo")).thenThrow(surveyUnitsValidationException);
        mockMvc.perform(multipart("/api/questionnaires/add").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(errorComponent).buildApiErrorObject(any(), eq(HttpStatus.BAD_REQUEST),
                eq(code));
    }

    @Test
    void onAddQuestionnaireWhenSpecificErrorsOnCsvSchemaReturnGenericErrorMessages() throws Exception {
        QuestionnaireAddRest questionnaireAddRest = new QuestionnaireAddRest("l8wwljbo", new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] surveyUnitData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireAddRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file",
                MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String code = "error.code";
        when(messageService.getMessage("validation.errors")).thenReturn(code);
        SurveyUnitsSpecificValidationException surveyUnitsSpecificValidationException = new SurveyUnitsSpecificValidationException("main error message", new ArrayList<>());
        when(surveyUnitCsvUseCase.validateSurveyUnits(surveyUnitData, "l8wwljbo")).thenThrow(surveyUnitsSpecificValidationException);

        mockMvc.perform(multipart("/api/questionnaires/add").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(errorComponent).buildApiErrorObject(any(), eq(HttpStatus.BAD_REQUEST),
                eq(code));
    }


}
