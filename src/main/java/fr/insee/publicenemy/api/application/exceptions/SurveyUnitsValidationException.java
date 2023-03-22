package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitValidation;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

@Getter
public class SurveyUnitsValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = -1619203216771899549L;
    private final List<SurveyUnitValidation> surveyUnitsErrors;

    public SurveyUnitsValidationException(List<SurveyUnitValidation> surveyUnitsErrors) {
        super("An error has occurred");
        this.surveyUnitsErrors = surveyUnitsErrors;
    }
}
