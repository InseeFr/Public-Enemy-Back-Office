package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataValidationResult;
import lombok.Getter;

import java.util.List;

/**
 * When validating survey units csv data attributes against variables types from a questionnaire model, this exception is throwed
 * if validation has failed on specific attributes
 */
@Getter
public class InterrogationsSpecificValidationException extends Exception {
    private final List<InterrogationDataValidationResult> interrogationsErrors;

    private final InterrogationExceptionCode code;

    public InterrogationsSpecificValidationException(String message, List<InterrogationDataValidationResult> interrogationsErrors) {
        super(message);
        this.interrogationsErrors = interrogationsErrors;
        this.code = InterrogationExceptionCode.INTERROGATION_SPECIFIC_VALIDATION_FAILED;
    }
}
