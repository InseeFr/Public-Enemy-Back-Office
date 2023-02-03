package fr.insee.publicenemy.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.usecase.DDIUseCase;
import fr.insee.publicenemy.api.application.usecase.QuestionnaireUseCase;
import fr.insee.publicenemy.api.controllers.dto.ContextRest;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireAddRest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
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
    private DDIUseCase ddiUseCase;

    @Autowired
    private MockMvc mockMvc;
    private List<Questionnaire> questionnaires;

    private Questionnaire questionnaire1;

    @BeforeEach
    public void init() {
        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        List<QuestionnaireMode> questionnaireModes = List.of(questionnaireMode);


        questionnaire1 = new Questionnaire(1L, "l8wwljbo", "label1", Context.BUSINESS,
                questionnaireModes, "questionnaire1".getBytes(),  false);
        Questionnaire questionnaire2 = new Questionnaire(2L, "l8wwljbo2", "label2", Context.BUSINESS,
                questionnaireModes, null, true);
        Questionnaire questionnaire3 = new Questionnaire(3L, "l8wwljbo3", "label3", Context.BUSINESS,
                questionnaireModes, null, true);

        questionnaires = new ArrayList<>();
        questionnaires.add(questionnaire1);
        questionnaires.add(questionnaire2);
        questionnaires.add(questionnaire3);
    }

    @Test
    void onGetQuestionnairesShouldFetchAllQuestionnaires() throws Exception {

        given(questionnaireUseCase.getQuestionnaires()).willReturn(questionnaires);

        mockMvc.perform(get("/api/questionnaires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(questionnaires.size())));
    }

    @Test
    void onGetQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {
        Long id = questionnaire1.getId();
        when(questionnaireUseCase.getQuestionnaire(id)).thenReturn(questionnaire1);

        mockMvc.perform(get("/api/questionnaires/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaire1.getId().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaire1.getPoguesId())))
                .andExpect(jsonPath("$.label", is(questionnaire1.getLabel())))
                .andExpect(jsonPath("$.context", is(questionnaire1.getContext().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaire1.getQuestionnaireModes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaire1.isSynchronized())));
    }

    @Test
    void onAddQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {
        QuestionnaireAddRest questionnaireRest =  new QuestionnaireAddRest( "l8wwljbo",  new ContextRest(Context.BUSINESS.name(), Context.BUSINESS.name()));
        byte[] surveyUnitData = "test".getBytes();
        ObjectMapper Obj = new ObjectMapper();
        String jsonQuestionnaire = Obj.writeValueAsString(questionnaireRest);
        MockPart questionnaireMockPart = new MockPart("questionnaire", jsonQuestionnaire.getBytes());
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, surveyUnitData);

        when(questionnaireUseCase.addQuestionnaire(questionnaireRest.poguesId(), Context.BUSINESS, surveyUnitData)).thenReturn(questionnaire1);

        questionnaireMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(multipart("/api/questionnaires/add").file(surveyUnitMockPart).part(questionnaireMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaire1.getId().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaire1.getPoguesId())))
                .andExpect(jsonPath("$.label", is(questionnaire1.getLabel())))
                .andExpect(jsonPath("$.context", is(questionnaire1.getContext().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaire1.getQuestionnaireModes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaire1.isSynchronized())));
    }

    @Test
    void onGetQuestionnaireFromPoguesShouldFetchQuestionnaireAttributes() throws Exception {
        String poguesId = questionnaire1.getPoguesId();
        when(ddiUseCase.getQuestionnaire(poguesId)).thenReturn(questionnaire1);

        mockMvc.perform(get("/api/questionnaires/pogues/{poguesId}", poguesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.poguesId", is(questionnaire1.getPoguesId())))
                .andExpect(jsonPath("$.label", is(questionnaire1.getLabel())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaire1.getQuestionnaireModes().size())))
        ;
    }

    @Test
    void onSaveQuestionnaireShouldFetchQuestionnaireAttributes() throws Exception {

        ObjectMapper Obj = new ObjectMapper();
        String jsonContext = Obj.writeValueAsString(questionnaire1.getContext());
        MockPart contextMockPart = new MockPart("context", jsonContext.getBytes());
        contextMockPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        MockMultipartFile surveyUnitMockPart = new MockMultipartFile("surveyUnitData", "file", MediaType.MULTIPART_FORM_DATA_VALUE, questionnaire1.getSurveyUnitData());

        Long id = questionnaire1.getId();
        when(questionnaireUseCase.updateQuestionnaire(questionnaire1.getId(),
                questionnaire1.getContext(), questionnaire1.getSurveyUnitData())).thenReturn(questionnaire1);

        mockMvc.perform(multipart("/api/questionnaires/{id}", id).part(contextMockPart).file(surveyUnitMockPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(questionnaire1.getId().intValue())))
                .andExpect(jsonPath("$.poguesId", is(questionnaire1.getPoguesId())))
                .andExpect(jsonPath("$.label", is(questionnaire1.getLabel())))
                .andExpect(jsonPath("$.context", is(questionnaire1.getContext().name())))
                .andExpect(jsonPath("$.modes.size()", is(questionnaire1.getQuestionnaireModes().size())))
                .andExpect(jsonPath("$.isSynchronized", is(questionnaire1.isSynchronized())));
    }

    @Test
    void onDeleteQuestionnaireShouldReturnEmptyJsonObject() throws Exception {
        Long id = questionnaire1.getId();

        mockMvc.perform(delete("/api/questionnaires/{id}/delete", id))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }
}
