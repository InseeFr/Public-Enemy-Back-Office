package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.ports.SurveyUnitCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyUnitCsvUseCase {

    private final SurveyUnitCsvPort surveyUnitCsvService;

    private final DDIUseCase ddiUseCase;

    public SurveyUnitCsvUseCase(SurveyUnitCsvPort surveyUnitCsvService, DDIUseCase ddiUseCase) {
        this.surveyUnitCsvService = surveyUnitCsvService;
        this.ddiUseCase = ddiUseCase;
    }

    public SurveyUnitCsvHeaderLine getHeadersLine(String poguesId) {
        List<VariableType> variables = ddiUseCase.getQuestionnaireVariables(poguesId);
        return surveyUnitCsvService.getSurveyUnitsCsvHeaders(variables);
    }
}
