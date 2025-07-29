package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.PoguesUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.InterrogationUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.ModeRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.utils.AuthenticatedUserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureMockMvc
class QuestionnaireControllerTest {
    @MockitoBean
    private QuestionnaireUseCase questionnaireUseCase;

    @MockitoBean
    private InterrogationUseCase interrogationUseCase;

    @MockitoBean
    private I18nMessagePort messageService;

    @MockitoBean
    private ApiExceptionComponent errorComponent;

    @MockitoBean
    private QuestionnaireComponent questionnaireComponent;

    @MockitoBean
    private PoguesUseCase poguesUseCase;

    @Autowired
    private MockMvc mockMvc;

    private final AuthenticatedUserTestHelper authenticatedUserTestHelper = new AuthenticatedUserTestHelper();
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
    void onGetQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {
        Long id = questionnaire.getId();
        when(questionnaireUseCase.getQuestionnaire(id)).thenReturn(questionnaire);

        mockMvc.perform(get("/api/questionnaires/{id}", id)
                        .with(authentication(authenticatedUserTestHelper.getUser())))
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
        when(questionnaireUseCase.getInterrogationData(id)).thenReturn(data);

        MvcResult result = mockMvc.perform(get("/api/questionnaires/{id}/data", id)
                        .with(authentication(authenticatedUserTestHelper.getUser())))
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
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);

        when(questionnaireUseCase.addQuestionnaire(questionnaireAddRest.poguesId(), Context.BUSINESS, surveyUnitData)).thenReturn(questionnaire);

        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(multipart("/api/questionnaires").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
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
        when(poguesUseCase.getQuestionnaire(poguesId)).thenReturn(questionnaire);

        mockMvc.perform(get("/api/questionnaires/pogues/{poguesId}", poguesId)
                        .with(authentication(authenticatedUserTestHelper.getUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poguesId", is(questionnaireRest.poguesId())))
                .andExpect(jsonPath("$.label", is(questionnaireRest.label())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaireRest.modes().size())));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = {"filecontent"})
    void onSaveQuestionnaireShouldFetchQuestionnaireAttributes(String interrogationData) throws Exception {

        ObjectMapper Obj = new ObjectMapper();
        String jsonContext = Obj.writeValueAsString(questionnaireRest.context());
        MockPart contextMockPart = new MockPart("context", jsonContext.getBytes());
        contextMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", interrogationData, MediaType.MULTIPART_FORM_DATA_VALUE, questionnaire.getInterrogationData());

        Long id = questionnaire.getId();
        when(questionnaireUseCase.updateQuestionnaire(questionnaire.getId(),
                questionnaire.getContext(), questionnaire.getInterrogationData())).thenReturn(questionnaire);

        mockMvc.perform(multipart("/api/questionnaires/{id}", id).part(contextMockPart).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
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
    void onSaveQuestionnaireWhenEmptyDataShouldFetchDataFromQuestionnaire() throws Exception {

        ObjectMapper Obj = new ObjectMapper();
        String jsonContext = Obj.writeValueAsString(questionnaireRest.context());
        MockPart contextMockPart = new MockPart("context", jsonContext.getBytes());
        contextMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Long id = questionnaire.getId();

        mockMvc.perform(multipart("/api/questionnaires/{id}", id).part(contextMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
        verify(questionnaireUseCase, times(1)).getInterrogationData(id);
    }

    @Test
    void onDeleteQuestionnaireShouldReturnEmptyJsonObject() throws Exception {
        Long id = questionnaire.getId();

        mockMvc.perform(delete("/api/questionnaires/{id}/delete", id)
                        .with(authentication(authenticatedUserTestHelper.getUser())))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void onAddQuestionnaireWhenGlobalErrorsOnCsvSchemaReturnGenericErrorMessages() throws Exception {
        QuestionnaireAddRest questionnaireAddRest = new QuestionnaireAddRest("l8wwljbo", new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] interrogationData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireAddRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file",
                MediaType.MULTIPART_FORM_DATA_VALUE, interrogationData);
        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String code = "error.code";
        when(messageService.getMessage("validation.errors")).thenReturn(code);
        InterrogationsGlobalValidationException surveyUnitsValidationException = new InterrogationsGlobalValidationException("main error message", new ArrayList<>());
        when(interrogationUseCase.validateInterrogations(interrogationData, "l8wwljbo")).thenThrow(surveyUnitsValidationException);
        mockMvc.perform(multipart("/api/questionnaires").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(errorComponent).buildApiErrorObject(any(), eq(HttpStatus.BAD_REQUEST),
                eq(code));
    }

    @Test
    void onAddQuestionnaireWhenSpecificErrorsOnCsvSchemaReturnGenericErrorMessages() throws Exception {
        QuestionnaireAddRest questionnaireAddRest = new QuestionnaireAddRest("l8wwljbo", new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] interrogationData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireAddRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file",
                MediaType.MULTIPART_FORM_DATA_VALUE, interrogationData);
        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String code = "error.code";
        when(messageService.getMessage("validation.errors")).thenReturn(code);
        InterrogationsSpecificValidationException interrogationsSpecificValidationException = new InterrogationsSpecificValidationException("main error message", new ArrayList<>());
        when(interrogationUseCase.validateInterrogations(interrogationData, "l8wwljbo")).thenThrow(interrogationsSpecificValidationException);

        mockMvc.perform(multipart("/api/questionnaires")
                        .file(surveyUnitMockPart)
                        .part(questionnaireMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(errorComponent).buildApiErrorObject(any(), eq(HttpStatus.BAD_REQUEST),
                eq(code));
    }


}
