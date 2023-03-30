package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitValidation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.List;

@Getter
public class SurveyUnitsValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = -1619203216771899549L;
    private final List<SurveyUnitValidation> surveyUnitsErrors;

    private final int code;

    public SurveyUnitsValidationException(String message, List<SurveyUnitValidation> surveyUnitsErrors) {
        super(message);
        this.surveyUnitsErrors = surveyUnitsErrors;
        this.code = HttpStatus.BAD_REQUEST.value();
    }
}
