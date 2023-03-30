package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.*;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.SurveyUnitsValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvService;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyUnitCsvUseCaseTest {
    private SurveyUnitCsvUseCase usecase;

    @Mock
    private DDIUseCase ddiUseCase;

    @Mock
    private QuestionnaireUseCase questionnaireUseCase;

    @Mock
    private SurveyUnitCsvService surveyUnitService;

    @Mock
    private I18nMessagePort messageService;

    @Mock
    private List<VariableType> variables;

    @Mock
    Questionnaire questionnaire;

    @BeforeEach
    void init() {
        usecase = new SurveyUnitCsvUseCase(surveyUnitService, ddiUseCase, questionnaireUseCase, messageService);
    }

    @Test
    void onGetHeadersLineReturnHeaderLines() {
        String poguesId = "l8wwljbo";
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variables);
        usecase.getHeadersLine(poguesId);
        verify(surveyUnitService, times(1)).getSurveyUnitsCsvHeaders(variables);
    }

    @Test
    void onValidateSurveyUnitsWhenNoSurveyUnitsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();

        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(new ArrayList<>());
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(new ArrayList<>());

        SurveyUnitsGlobalValidationException ex = assertThrows(SurveyUnitsGlobalValidationException.class, () -> usecase.validateSurveyUnits(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.survey-units.no-exist", message.getCode());
    }

    @Test
    void onValidateSurveyUnitsWhenMaxLimitSurveyUnitsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        SurveyUnitData data = new SurveyUnitData(new ArrayList<>());
        List<SurveyUnit> surveyUnits = new ArrayList<>();
        for(int i=0; i<50; i++) {
            surveyUnits.add(new SurveyUnit(i+"", "q1", data, SurveyUnitStateData.createInitialStateData()));
        }
        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(surveyUnits);
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(new ArrayList<>());

        SurveyUnitsGlobalValidationException ex = assertThrows(SurveyUnitsGlobalValidationException.class, () -> usecase.validateSurveyUnits(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.survey-units.max-size", message.getCode());
    }

    @Test
    void onValidateSurveyUnitsWhenVariablesNotInSurveyUnitAttributesThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<SurveyUnit> surveyUnits = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("name","true"));
        SurveyUnitData data = new SurveyUnitData(csvFields);

        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));

        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(surveyUnits);

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "name", null, new BooleanDatatypeType()));
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "label", "dsgdfg", new BooleanDatatypeType()));
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);

        SurveyUnitsGlobalValidationException ex = assertThrows(SurveyUnitsGlobalValidationException.class, () -> usecase.validateSurveyUnits(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.variable.not-defined", message.getCode());
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeErrorsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<SurveyUnit> surveyUnits = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect","not a boolean value"));
        SurveyUnitData data = new SurveyUnitData(csvFields);

        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("2", "q1", data, SurveyUnitStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(surveyUnits);

        assertThrows(SurveyUnitsValidationException.class, () -> usecase.validateSurveyUnits(surveyUnitDataByte, poguesId));
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeNotDefinedInVariablesReturnsWarningMessages() throws SurveyUnitsGlobalValidationException, SurveyUnitsValidationException {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<SurveyUnit> surveyUnits = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect","1"));
        csvFields.add(new AbstractMap.SimpleEntry<>("nonExistingAttribute","test"));
        SurveyUnitData data = new SurveyUnitData(csvFields);

        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("2", "q1", data, SurveyUnitStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(surveyUnits);

        List<ValidationWarningMessage> messages = usecase.validateSurveyUnits(surveyUnitDataByte, poguesId);

        assertEquals("validation.attribute.not-exist", messages.get(0).getCode());
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeValidReturnNothing() throws SurveyUnitsGlobalValidationException, SurveyUnitsValidationException {
        Long questionnaireId = 1L;
        String poguesId = "l8wwljbo";
        when(questionnaireUseCase.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(questionnaire.getPoguesId()).thenReturn(poguesId);

        byte[] surveyUnitDataByte = "data".getBytes();
        List<SurveyUnit> surveyUnits = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect","1"));
        SurveyUnitData data = new SurveyUnitData(csvFields);

        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("2", "q1", data, SurveyUnitStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(ddiUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(surveyUnitService.initSurveyUnits(surveyUnitDataByte, poguesId)).thenReturn(surveyUnits);

        List<ValidationWarningMessage> messages = usecase.validateSurveyUnits(surveyUnitDataByte, questionnaireId);

        assertTrue(messages.isEmpty());
    }
}
