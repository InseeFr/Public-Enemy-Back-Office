package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataValidationResult;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Getter
/**
 * When validating survey units csv data attributes against variables types from a questionnaire model, this exception is throwed
 * if validation has failed on specific attributes
 */
public class SurveyUnitsSpecificValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = -1619203216771899549L;
    private final List<SurveyUnitDataValidationResult> surveyUnitsErrors;

    private final SurveyUnitExceptionCode code;

    public SurveyUnitsSpecificValidationException(String message, List<SurveyUnitDataValidationResult> surveyUnitsErrors) {
        super(message);
        this.surveyUnitsErrors = surveyUnitsErrors;
        this.code = SurveyUnitExceptionCode.SURVEY_UNIT_SPECIFIC_VALIDATION_FAILED;
    }
}
