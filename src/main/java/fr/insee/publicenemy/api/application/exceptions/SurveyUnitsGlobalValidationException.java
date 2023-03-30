package fr.insee.publicenemy.api.application.exceptions;

import fr.insee.publicenemy.api.application.domain.model.pogues.ValidationErrorMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SurveyUnitsGlobalValidationException extends Exception {
    @Serial
    private static final long serialVersionUID = 6805779307322272561L;
    private final List<ValidationErrorMessage> globalErrorMessages;

    private final int code;

    public SurveyUnitsGlobalValidationException(String message, List<ValidationErrorMessage> globalErrorMessages) {
        super(message);
        this.globalErrorMessages = globalErrorMessages;
        this.code = HttpStatus.BAD_REQUEST.value();
    }

    public SurveyUnitsGlobalValidationException(String message, ValidationErrorMessage globalErrorMessage) {
        super(message);
        this.globalErrorMessages = new ArrayList<>();
        globalErrorMessages.add(globalErrorMessage);
        this.code = HttpStatus.BAD_REQUEST.value();
    }
}
