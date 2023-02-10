package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnitData;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SurveyUnitController.class)
@ContextConfiguration(classes = SurveyUnitController.class)
@Slf4j
class SurveyUnitControllerTest {
    @MockBean
    private QueenUseCase queenUseCase;

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
}
