package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;

@JsonSerialize(using = SurveyUnitUpdateSerializer.class)
public record SurveyUnitUpdateDto(
        SurveyUnitData data,
        SurveyUnitStateData stateData) {

    /**
     * @param surveyUnit survey unit model
     * @return a new survey unit dto from the survey unit model
     */
    public static SurveyUnitUpdateDto fromModel(SurveyUnit surveyUnit) {
        return new SurveyUnitUpdateDto(surveyUnit.data(), surveyUnit.stateData());
    }
}
