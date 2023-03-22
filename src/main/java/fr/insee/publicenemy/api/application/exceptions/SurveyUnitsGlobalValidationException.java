package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import lombok.Getter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SurveyUnitsGlobalValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = 6805779307322272561L;
    private final List<ValidationErrorMessage> globalErrorMessages;

    public SurveyUnitsGlobalValidationException(List<ValidationErrorMessage> globalErrorMessages) {
        super("An error has occurred");
        this.globalErrorMessages = globalErrorMessages;
    }

    public SurveyUnitsGlobalValidationException(ValidationErrorMessage globalErrorMessage) {
        super("An error has occurred");
        this.globalErrorMessages = new ArrayList<>();
        globalErrorMessages.add(globalErrorMessage);
    }
}
