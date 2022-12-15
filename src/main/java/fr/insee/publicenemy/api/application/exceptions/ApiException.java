package fr.insee.publicenemy.api.application.exceptions;

import java.util.List;

import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiFieldError;
import lombok.Getter;
import lombok.Setter;
/**
 * This class is used to return non specific API Exceptions
 */
@Getter
@Setter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final List<ApiFieldError> errors;
    private final List<String> messages;

    public ApiException(int statusCode, String message) {
        this(statusCode, List.of(message));
    }

    public ApiException(int statusCode, String message, List<ApiFieldError> errors) {
        this(statusCode, List.of(message), errors);
    }

    public ApiException(int statusCode, List<String> messages) {
        this(statusCode, messages, null);
    }

    public ApiException(int statusCode, List<String> messages, List<ApiFieldError> errors) {
        this.messages = messages;
        this.statusCode = statusCode;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return messages != null ? String.join(" | ", messages) : null;
    }
}
