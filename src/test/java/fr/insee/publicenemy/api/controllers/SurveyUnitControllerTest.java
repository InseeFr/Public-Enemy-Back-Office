package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitAttributeValidation;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitValidation;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.application.usecase.SurveyUnitCsvUseCase;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorCode;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurveyUnitController.class)
@ContextConfiguration(classes = SurveyUnitController.class)
@Slf4j
class SurveyUnitControllerTest {
    @MockBean
    private QueenUseCase queenUseCase;

    @MockBean
    private SurveyUnitCsvUseCase csvUseCase;

    @MockBean
    private SurveyUnitMessagesComponent messageComponent;

    @MockBean
    private ApiExceptionComponent errorComponent;

    @MockBean
    private I18nMessagePort messageService;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private List<SurveyUnit> surveyUnits;


    @BeforeEach
    public void init() {
        SurveyUnitData data = new SurveyUnitData(new ArrayList<>());
        surveyUnits = new ArrayList<>();
        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("2", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("3", "q1", data, SurveyUnitStateData.createInitialStateData()));
    }

    @Test
    void onGetSurveyUnitsShouldFetchAllSurveyUnits() throws Exception {
        Long questionnaireId = 12L;
        Mode cawi = Mode.valueOf("CAWI");
        String questionnaireModelId = String.format("%s-%s", questionnaireId, cawi.name());
        when(queenUseCase.getSurveyUnits(questionnaireModelId)).thenReturn(surveyUnits);
        mockMvc.perform(get("/api/questionnaires/{questionnaireId}/modes/{mode}/survey-units", questionnaireId, cawi.name()))
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
        MvcResult result = mockMvc.perform(get("/api/questionnaires/{poguesId}/csv", poguesId))
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
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("[\""+code+"\"]", result.getResponse().getContentAsString());
    }

    @Test
    void onCheckInCorrectCsvSchemaReturnGlobalErrorMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "dsfgsdgfs".getBytes();
        List<ValidationErrorMessage> messages = new ArrayList<>();
        String code = "error.code";
        messages.add(new ValidationErrorMessage(code, "plop"));
        when(messageService.getMessage(eq(code), any())).thenReturn(code);
        SurveyUnitsGlobalValidationException surveyUnitsValidationException = new SurveyUnitsGlobalValidationException(messages);
        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenThrow(surveyUnitsValidationException);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(errorComponent).buildApiErrorWithMessages(any(), eq(ApiErrorCode.SURVEY_UNITS_GLOBAL_ERRORS.getValue()),
                eq(surveyUnitsValidationException.getMessage()), any());

    }

    @Test
    void onCheckInCorrectCsvSchemaReturnSpecificErrorMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "".getBytes();

        List<SurveyUnitValidation> surveyUnitValidations = new ArrayList<>();
        List<SurveyUnitAttributeValidation>  attributesValidations = new ArrayList<>();
        DataTypeValidationMessage message1 = DataTypeValidationMessage.createMessage("error.code1");
        DataTypeValidationMessage message2 = DataTypeValidationMessage.createMessage("error.code2");
        List<DataTypeValidationMessage> messages = List.of(message1, message2);

        DataTypeValidation typeValidation = new DataTypeValidation(false, messages);
        attributesValidations.add(new SurveyUnitAttributeValidation("att1", typeValidation));
        attributesValidations.add(new SurveyUnitAttributeValidation("att2", typeValidation));
        surveyUnitValidations.add(new SurveyUnitValidation("1", attributesValidations));
        surveyUnitValidations.add(new SurveyUnitValidation("2", attributesValidations));
        SurveyUnitsValidationException ex = new SurveyUnitsValidationException(surveyUnitValidations);

        when(csvUseCase.validateSurveyUnits(surveyUnitData, poguesId)).thenThrow(ex);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(messageComponent).getErrors(surveyUnitValidations);
    }
}
