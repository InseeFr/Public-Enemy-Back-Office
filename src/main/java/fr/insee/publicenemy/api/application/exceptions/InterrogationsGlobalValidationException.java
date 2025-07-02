package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import lombok.Getter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * When validating survey units csv data attributes against variables types from a questionnaire model, this exception is throwed
 * if global validation has failed (too many data in csv, too many rows, ...)
 */
@Getter
public class InterrogationsGlobalValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = 6805779307322272561L;
    private final List<ValidationErrorMessage> globalErrorMessages;

    private final InterrogationExceptionCode code;

    public InterrogationsGlobalValidationException(String message, List<ValidationErrorMessage> globalErrorMessages) {
        super(message);
        this.globalErrorMessages = globalErrorMessages;
        this.code = InterrogationExceptionCode.SURVEY_UNIT_GLOBAL_VALIDATION_FAILED;
    }

    public InterrogationsGlobalValidationException(String message, ValidationErrorMessage globalErrorMessage) {
        super(message);
        this.globalErrorMessages = new ArrayList<>();
        globalErrorMessages.add(globalErrorMessage);
        this.code = InterrogationExceptionCode.SURVEY_UNIT_GLOBAL_VALIDATION_FAILED;
    }
}
