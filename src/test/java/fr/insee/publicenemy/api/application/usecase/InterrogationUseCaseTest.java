package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.*;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsGlobalValidationException;
import fr.insee.publicenemy.api.application.exceptions.InterrogationsSpecificValidationException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvService;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
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
class InterrogationUseCaseTest {
    private InterrogationUseCase usecase;

    @Mock
    private PoguesUseCase poguesUseCase;

    @Mock
    private QuestionnaireUseCase questionnaireUseCase;

    @Mock
    private InterrogationCsvService interrogationCsvService;

    @Mock
    private I18nMessagePort messageService;

    @Mock
    private List<VariableType> variables;

    @Mock
    Questionnaire questionnaire;

    @BeforeEach
    void init() {
        usecase = new InterrogationUseCase(interrogationCsvService, poguesUseCase, questionnaireUseCase, messageService, 10);
    }

    @Test
    void onGetHeadersLineReturnHeaderLines() {
        String poguesId = "l8wwljbo";
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variables);
        usecase.getHeadersLine(poguesId);
        verify(interrogationCsvService, times(1)).getInterrogationsCsvHeaders(variables);
    }

    @Test
    void onValidateSurveyUnitsWhenNoInterrogationsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();

        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(new ArrayList<>());
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(new ArrayList<>());

        InterrogationsGlobalValidationException ex = assertThrows(InterrogationsGlobalValidationException.class, () -> usecase.validateInterrogations(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.survey-units.no-exist", message.getCode());
    }

    @Test
    void onValidateSurveyUnitsWhenMaxLimitInterrogationsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        InterrogationData data = new InterrogationData(new ArrayList<>());
        List<Interrogation> interrogations = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            interrogations.add(new Interrogation(i + "", "q1", data, InterrogationStateData.createInitialStateData()));
        }
        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(interrogations);
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(new ArrayList<>());

        InterrogationsGlobalValidationException ex = assertThrows(InterrogationsGlobalValidationException.class, () -> usecase.validateInterrogations(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.survey-units.max-size", message.getCode());
    }

    @Test
    void onValidateSurveyUnitsWhenVariablesNotInSurveyUnitAttributesThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<Interrogation> interrogations = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("name", "true"));
        InterrogationData data = new InterrogationData(csvFields);

        interrogations.add(new Interrogation("1", "q1", data, InterrogationStateData.createInitialStateData()));

        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(interrogations);

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "name", null, new BooleanDatatypeType()));
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "label", "dsgdfg", new BooleanDatatypeType()));
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);

        InterrogationsGlobalValidationException ex = assertThrows(InterrogationsGlobalValidationException.class, () -> usecase.validateInterrogations(surveyUnitDataByte, poguesId));
        ValidationErrorMessage message = ex.getGlobalErrorMessages().get(0);
        assertEquals("validation.variable.not-defined", message.getCode());
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeErrorsThrowsException() {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<Interrogation> interrogations = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect", "not a boolean value"));
        InterrogationData data = new InterrogationData(csvFields);

        interrogations.add(new Interrogation("1", "q1", data, InterrogationStateData.createInitialStateData()));
        interrogations.add(new Interrogation("2", "q1", data, InterrogationStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(interrogations);

        assertThrows(InterrogationsSpecificValidationException.class, () -> usecase.validateInterrogations(surveyUnitDataByte, poguesId));
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeNotDefinedInVariablesReturnsWarningMessages() throws InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {
        String poguesId = "l8wwljbo";
        byte[] surveyUnitDataByte = "data".getBytes();
        List<Interrogation> interrogations = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect", "1"));
        csvFields.add(new AbstractMap.SimpleEntry<>("nonExistingAttribute", "test"));
        InterrogationData data = new InterrogationData(csvFields);

        interrogations.add(new Interrogation("1", "q1", data, InterrogationStateData.createInitialStateData()));
        interrogations.add(new Interrogation("2", "q1", data, InterrogationStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(interrogations);

        List<ValidationWarningMessage> messages = usecase.validateInterrogations(surveyUnitDataByte, poguesId);

        assertEquals("validation.attribute.not-exist", messages.get(0).getCode());
    }

    @Test
    void onValidateSurveyWhenSurveyUnitsAttributeValidReturnNothing() throws InterrogationsGlobalValidationException, InterrogationsSpecificValidationException {
        Long questionnaireId = 1L;
        String poguesId = "l8wwljbo";
        when(questionnaireUseCase.getQuestionnaire(questionnaireId)).thenReturn(questionnaire);
        when(questionnaire.getPoguesId()).thenReturn(poguesId);

        byte[] surveyUnitDataByte = "data".getBytes();
        List<Interrogation> interrogations = new ArrayList<>();

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        csvFields.add(new AbstractMap.SimpleEntry<>("isCorrect", "1"));
        InterrogationData data = new InterrogationData(csvFields);

        interrogations.add(new Interrogation("1", "q1", data, InterrogationStateData.createInitialStateData()));
        interrogations.add(new Interrogation("2", "q1", data, InterrogationStateData.createInitialStateData()));

        List<VariableType> variablesTypes = new ArrayList<>();
        variablesTypes.add(new VariableType(VariableTypeEnum.EXTERNAL, "isCorrect", null, new BooleanDatatypeType()));
        when(poguesUseCase.getQuestionnaireVariables(poguesId)).thenReturn(variablesTypes);
        when(interrogationCsvService.initInterrogations(surveyUnitDataByte, null)).thenReturn(interrogations);

        List<ValidationWarningMessage> messages = usecase.validateInterrogations(surveyUnitDataByte, questionnaireId);

        assertTrue(messages.isEmpty());
    }
}
