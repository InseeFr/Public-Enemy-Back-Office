package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitCsvHeaderLine;

import java.util.List;

public interface SurveyUnitCsvPort {
    /**
     * initialize survey units from a csv data file
     *
     * @param surveyUnitData       survey units data
     * @param questionnaireModelId questionnaire model id
     * @return survey units model from questionnaire csv survey units
     */
    List<SurveyUnit> initSurveyUnits(byte[] surveyUnitData, String questionnaireModelId);

    /**
     * get survey unit from a csv data file
     *
     * @param surveyUnitId         survey unit id
     * @param surveyUnitData       survey units csv data
     * @param questionnaireModelId questionnaire model id used to generate queen identifier
     * @return survey units model from questionnaire csv survey units
     */
    SurveyUnit getCsvSurveyUnit(int surveyUnitId, byte[] surveyUnitData, String questionnaireModelId);

    /**
     * retrieve csv headers based on variable types from a questionnaire model
     *
     * @param variablesType list of variables types for a questionnaire
     * @return csv headers for survey units from variables type
     */
    SurveyUnitCsvHeaderLine getSurveyUnitsCsvHeaders(List<VariableType> variablesType);
}
