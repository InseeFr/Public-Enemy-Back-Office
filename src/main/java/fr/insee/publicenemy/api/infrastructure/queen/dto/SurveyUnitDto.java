package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnitData;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
@JsonSerialize(using = SurveyUnitSerializer.class)
public record SurveyUnitDto (
        String id,
        String questionnaireId,
        SurveyUnitData data,
        SurveyUnitStateData stateData){

    /**
     *
     * @param surveyUnit survey unit model
     * @return a new survey unit dto from the survey unit model
     */
    public static SurveyUnitDto fromModel(SurveyUnit surveyUnit) {
        return new SurveyUnitDto(surveyUnit.id(), surveyUnit.questionnaireId(), surveyUnit.data(), surveyUnit.stateData());
    }
}
