package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationWarningMessage;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import fr.insee.publicenemy.api.application.exceptions.InterrogationExceptionCode;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.usecase.*;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvHeaderLine;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import fr.insee.publicenemy.api.utils.AuthenticatedUserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
class InterrogationControllerTest {
    @MockitoBean
    private QueenUseCase queenUseCase;

    @MockitoBean
    private PoguesUseCase poguesUseCase;

    @MockitoBean
    private PersonalizationUseCase personalizationUseCase;

    @MockitoBean
    private InterrogationUseCase csvUseCase;

    @MockitoBean
    private InterrogationMessagesComponent messageComponent;

    @MockitoBean
    private ApiExceptionComponent errorComponent;

    @MockitoBean
    private I18nMessagePort messageService;

    @MockitoBean
    private QuestionnaireUseCase questionnaireUseCase;

    @Autowired
    private MockMvc mockMvc;

    private final AuthenticatedUserTestHelper authenticatedUserTestHelper = new AuthenticatedUserTestHelper();

    @Mock
    private List<Interrogation> interrogations;

    @Mock
    private List<PersonalizationMapping> personalizationMappings;

    @Mock
    private Questionnaire questionnaire;


    @BeforeEach
    void init() {
        InterrogationData data = new InterrogationData(new ArrayList<>());
        interrogations = new ArrayList<>();
        interrogations.add(new Interrogation("11-CAPI-1", "q1", data, InterrogationStateData.createInitialStateData()));
        interrogations.add(new Interrogation("11-CAPI-2", "q1", data, InterrogationStateData.createInitialStateData()));
        interrogations.add(new Interrogation("11-CAPI-3", "q1", data, InterrogationStateData.createInitialStateData()));

        personalizationMappings = new ArrayList<>();
        personalizationMappings.add(new PersonalizationMapping("11-CAWI-1", 11L, Mode.CAWI, 0));
        personalizationMappings.add(new PersonalizationMapping("11-CAWI-2", 11L, Mode.CAWI, 1));
        personalizationMappings.add(new PersonalizationMapping("11-CAWI-3", 11L, Mode.CAWI, 2));

        questionnaire = new Questionnaire("poguesId","label",List.of(Mode.valueOf("CAWI"), Mode.valueOf("CATI")));
    }

    @Test
    void onGetCsvSchemaReturnsCSVHeaders() throws Exception {
        String poguesId = "l8wwljbo";
        Set<String> headers = new HashSet<>();
        headers.add("Header1");
        headers.add("Header2");
        headers.add("Header3");
        InterrogationCsvHeaderLine headerLine = new InterrogationCsvHeaderLine(headers);
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

        when(csvUseCase.validateInterrogations(surveyUnitData, poguesId)).thenReturn(new ArrayList<>());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
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
        when(csvUseCase.validateInterrogations(surveyUnitData, poguesId)).thenReturn(messages);
        when(messageService.getMessage(eq(code), any())).thenReturn(code);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
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
        InterrogationsGlobalValidationException surveyUnitsValidationException = new InterrogationsGlobalValidationException("main error message", messages);
        when(csvUseCase.validateInterrogations(surveyUnitData, poguesId)).thenThrow(surveyUnitsValidationException);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(errorComponent).buildApiErrorWithMessages(any(), eq(InterrogationExceptionCode.INTERROGATION_GLOBAL_VALIDATION_FAILED.value()),
                eq(surveyUnitsValidationException.getMessage()), any());

    }

    @Test
    void onCheckInCorrectCsvSchemaReturnSpecificErrorMessages() throws Exception {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitData = "".getBytes();

        List<InterrogationDataValidationResult> interrogationDataValidationResults = new ArrayList<>();
        List<InterrogationDataAttributeValidationResult> attributesValidations = new ArrayList<>();
        DataTypeValidationMessage message1 = DataTypeValidationMessage.createMessage("error.code1");
        DataTypeValidationMessage message2 = DataTypeValidationMessage.createMessage("error.code2");
        List<DataTypeValidationMessage> messages = List.of(message1, message2);

        DataTypeValidationResult typeValidation = new DataTypeValidationResult(false, messages);
        attributesValidations.add(new InterrogationDataAttributeValidationResult("att1", typeValidation));
        attributesValidations.add(new InterrogationDataAttributeValidationResult("att2", typeValidation));
        interrogationDataValidationResults.add(new InterrogationDataValidationResult("1", attributesValidations));
        interrogationDataValidationResults.add(new InterrogationDataValidationResult("2", attributesValidations));
        InterrogationsSpecificValidationException ex = new InterrogationsSpecificValidationException("main error message", interrogationDataValidationResults);

        when(csvUseCase.validateInterrogations(surveyUnitData, poguesId)).thenThrow(ex);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("interrogationData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);
        mockMvc.perform(multipart("/api/questionnaires/{poguesId}/checkdata", poguesId).file(surveyUnitMockPart)
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(messageComponent).getErrors(interrogationDataValidationResults);
    }

    @Test
    void onResetSurveyUnitCallResetService() throws Exception {
        PersonalizationMapping mapping = new PersonalizationMapping("11-CAPI-1", 11L, Mode.CAPI, 0);

        byte[] surveyUnitData = "".getBytes();
        when(questionnaireUseCase.getInterrogationData(mapping.questionnaireId())).thenReturn(surveyUnitData);
        when(personalizationUseCase.getPersoMappingByInterrogationId(mapping.interrogationId())).thenReturn(mapping);

        mockMvc.perform(put("/api/interrogations/{interrogationId}/reset", mapping.interrogationId())
                        .with(authentication(authenticatedUserTestHelper.getUser()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        verify(queenUseCase).resetInterrogation(mapping, surveyUnitData);
    }
}
