package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;

import java.util.List;

public interface SurveyUnitCsvPort {
    /**
     * @param surveyUnitData survey units data
     * @param questionnaireModelId questionnaire model id
     * @return survey units model from questionnaire csv survey units
     */
    List<SurveyUnit> initSurveyUnits(byte[] surveyUnitData, String questionnaireModelId);

    /**
     *
     * @param variablesType list of variables types for a questionnaire
     * @return csv headers for survey units from variables type
     */
    SurveyUnitCsvHeaderLine getSurveyUnitsCsvHeaders(List<VariableType> variablesType);
}
