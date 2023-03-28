package fr.insee.publicenemy.api.controllers.exceptions.dto;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;

/**
 * API Error object returned as JSON response to client including fields details
 */
public class ApiErrorWithFields extends ApiErrorDetails<List<ApiFieldError>> {
    /**
     * @param status http status
     * @param path origin request path
     * @param timestamp timestamp of the generated error
     * @param errorMessage error message
     * @param details specific details about this error
     */
    public ApiErrorWithFields(HttpStatus status, String path, Date timestamp, String errorMessage, @NonNull List<ApiFieldError> details) {
        super(status, path, timestamp, errorMessage, details);
    }
}
