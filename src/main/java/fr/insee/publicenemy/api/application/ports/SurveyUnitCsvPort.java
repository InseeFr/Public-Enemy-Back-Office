package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;

import java.util.List;

public interface SurveyUnitCsvPort {
    /**
     * @param questionnaire questionnaire
     * @param questionnaireModelId questionnaire model id
     * @return survey units model from questionnaire csv survey units
     */
    List<SurveyUnit> initSurveyUnits(Questionnaire questionnaire, String questionnaireModelId);
}
