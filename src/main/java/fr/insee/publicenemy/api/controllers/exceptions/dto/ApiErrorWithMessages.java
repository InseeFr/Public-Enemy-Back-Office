package fr.insee.publicenemy.api.controllers.exceptions.dto;

import lombok.NonNull;

import java.util.Date;
import java.util.List;

/**
 * API Error object returned as JSON response to client including messages details
 */
public class ApiErrorWithMessages extends ApiErrorDetails<List<String>> {
    /**
     * @param code error code
     * @param path origin request path
     * @param timestamp timestamp of the generated error
     * @param errorMessage error message
     * @param details specific details about this error
     */
    public ApiErrorWithMessages(int code, String path, Date timestamp, String errorMessage, @NonNull List<String> details) {
        super(code, path, timestamp, errorMessage, details);
    }
}
