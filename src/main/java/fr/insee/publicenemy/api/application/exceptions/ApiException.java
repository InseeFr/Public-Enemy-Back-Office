package fr.insee.publicenemy.api.application.exceptions;

import java.util.List;

import fr.insee.publicenemy.api.application.dto.ApiFieldError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final List<ApiFieldError> errors;
    private final String message;

    /**
     * @param statusCode
     * @param message
     */
    public ApiException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    /**
     * @param status
     * @param message
     * @param errors
     */
    public ApiException(int statusCode, String message, List<ApiFieldError> errors) {
        this.message = message;
        this.statusCode = statusCode;
        this.errors = errors;
    }
}
