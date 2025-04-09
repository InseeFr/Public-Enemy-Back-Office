package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataValidationResult;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitExceptionCode;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.PoguesUseCase;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import fr.insee.publicenemy.api.utils.AuthenticatedUserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration
@AutoConfigureMockMvc
class SurveyUnitControllerTest {
    @MockBean
    private QueenUseCase queenUseCase;

    @MockBean
    private PoguesUseCase poguesUseCase;

    @MockBean
    private SurveyUnitCsvUseCase csvUseCase;

    @MockBean
    private SurveyUnitMessagesComponent messageComponent;

    @MockBean
    private ApiExceptionComponent errorComponent;

    @MockBean
    private I18nMessagePort messageService;

    @MockBean
    private QuestionnaireUseCase questionnaireUseCase;

    @Autowired
    private MockMvc mockMvc;

    private final AuthenticatedUserTestHelper authenticatedUserTestHelper = new AuthenticatedUserTestHelper();

    @Mock
    private List<SurveyUnit> surveyUnits;

    @Mock
    private Questionnaire questionnaire;


    @BeforeEach
    public void init() {
        SurveyUnitData data = new SurveyUnitData(new ArrayList<>());
        surveyUnits = new ArrayList<>();
        surveyUnits.add(new SurveyUnit("11-CAPI-1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("11-CAPI-2", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("11-CAPI-3", "q1", data, SurveyUnitStateData.createInitialStateData()));

        questionnaire = new Questionnaire("poguesId","label",List.of(Mode.valueOf("CAWI"), Mode.valueOf("CATI")));
    }

    @Test
    void onGetSurveyUnitsShouldFetchAllSurveyUnits() throws Exception {
        Long questionnaireId = 12L;
        Mode cawi = Mode.valueOf("CAWI");
        String questionnaireModelId = String.format("%s-%s", questionnaireId, cawi.name());
        when(questionnaireUseCase.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(poguesUseCase.getNomenclatureOfQuestionnaire(questionnaire.getPoguesId())).thenReturn(JsonNodeFactory.instance.missingNode());
        when(queenUseCase.getSurveyUnits(questionnaireModelId)).thenReturn(surveyUnits);
        mockMvc.perform(get("/api/questionnaires/{questionnaireId}/modes/{mode}/survey-units", questionnaireId, cawi.name())
                        .with(authentication(authenticatedUserTestHelper.getUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surveyUnits.size()", is(surveyUnits.size())))
                .andExpect(jsonPath("$.questionnaireModelId", is(questionnaireModelId)));
    }

    @Test
    void onGetCsvSchemaReturnsCSVHeaders() throws Exception {
        String poguesId = "l8wwljbo";
        Set<String> headers = new HashSet<>();
        headers.add("Header1");
        headers.add("Header2");
        headers.add("Header3");
        SurveyUnitCsvHeaderLine headerLine = new SurveyUnitCsvHeaderLine(headers);
        when(csvUseCase.getHeadersLine(poguesId)).thenReturn(headerLine);
        MvcResult result = mockMvc.perform(get("/api/questionnaires/{poguesId}/csv", poguesId)
                        .with(authentication(authenticatedUserTestHelper.getUser())))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("\"Header1\",\"Header2\",\"Header3\"\n", result.getResponse().getContentAsString());
    }

    @Test
    void onCheckCorrectCsvSchemaReturnsEmptyWarningMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "".getBytes();

        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenReturn(new ArrayList<>());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        MvcResult result = mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("[]", result.getResponse().getContentAsString());
    }

    @Test
    void onCheckInCorrectCsvSchemaReturnsWarningMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "".getBytes();
        List<ValidationWarningMessage> messages = new ArrayList<>();
        String code = "warning.code";
        messages.add(new ValidationWarningMessage(code, "plop"));
        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenReturn(messages);
        when(messageService.getMessage(eq(code), any())).thenReturn(code);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        MvcResult result = mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("[\"" + code + "\"]", result.getResponse().getContentAsString());
    }

    @Test
    void onCheckInCorrectCsvSchemaReturnGlobalErrorMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "dsfgsdgfs".getBytes();
        List<ValidationErrorMessage> messages = new ArrayList<>();
        String code = "error.code";
        messages.add(new ValidationErrorMessage(code, "plop"));
        when(messageService.getMessage(eq(code), any())).thenReturn(code);
        SurveyUnitsGlobalValidationException surveyUnitsValidationException = new SurveyUnitsGlobalValidationException("main error message", messages);
        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenThrow(surveyUnitsValidationException);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(errorComponent).buildApiErrorWithMessages(any(), eq(SurveyUnitExceptionCode.SURVEY_UNIT_GLOBAL_VALIDATION_FAILED.value()),
                eq(surveyUnitsValidationException.getMessage()), any());

    }

    @Test
    void onCheckInCorrectCsvSchemaReturnSpecificErrorMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "".getBytes();

        List<SurveyUnitDataValidationResult> surveyUnitDataValidationResults = new ArrayList<>();
        List<SurveyUnitDataAttributeValidationResult> attributesValidations = new ArrayList<>();
        DataTypeValidationMessage message1 = DataTypeValidationMessage.createMessage("error.code1");
        DataTypeValidationMessage message2 = DataTypeValidationMessage.createMessage("error.code2");
        List<DataTypeValidationMessage> messages = List.of(message1, message2);

        DataTypeValidationResult typeValidation = new DataTypeValidationResult(false, messages);
        attributesValidations.add(new SurveyUnitDataAttributeValidationResult("att1", typeValidation));
        attributesValidations.add(new SurveyUnitDataAttributeValidationResult("att2", typeValidation));
        surveyUnitDataValidationResults.add(new SurveyUnitDataValidationResult("1", attributesValidations));
        surveyUnitDataValidationResults.add(new SurveyUnitDataValidationResult("2", attributesValidations));
        SurveyUnitsSpecificValidationException ex = new SurveyUnitsSpecificValidationException("main error message", surveyUnitDataValidationResults);

        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenThrow(ex);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(messageComponent).getErrors(surveyUnitDataValidationResults);
    }

    @Test
    void onResetSurveyUnitCallResetService() throws Exception {
        String surveyUnitId = "11-CAPI-1";
        byte[] surveyUnitData = "".getBytes();
        when(questionnaireUseCase.getSurveyUnitData(11L)).thenReturn(surveyUnitData);

        mockMvc.perform(put("/api/survey-units/{surveyUnitId}/reset", surveyUnitId)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        verify(queenUseCase).resetSurveyUnit(surveyUnitId, surveyUnitData);
    }
}
