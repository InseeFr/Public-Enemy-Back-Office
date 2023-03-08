package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyUnitCsvUseCaseTest {
    private SurveyUnitCsvUseCase usecase;

    @Mock
    private DDIUseCase ddiUseCase;

    @Mock
    private SurveyUnitCsvService surveyUnitService;

    @Mock
    private List<VariableType> variables;

    @BeforeEach
    void init() {
        usecase = new SurveyUnitCsvUseCase(surveyUnitService, ddiUseCase);
    }

    @Test
    void onGetHeadersLineReturnHeaderLines() {
        String poguesId = "l8wwljbo";
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variables);
        usecase.getHeadersLine(poguesId);
        verify(surveyUnitService, times(1)).getSurveyUnitsCsvHeaders(variables);
    }
}
