package fr.insee.publicenemy.api.controllers.exceptions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * API Error object returned as JSON response to client including specific details
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ApiErrorDetails<T> extends ApiError {
    @JsonProperty("details")
    private T details;

    /**
     * @param code error code
     * @param path origin request path
     * @param timestamp timestamp of the generated error
     * @param errorMessage error message
     * @param details specific details about this error
     */
    public ApiErrorDetails(int code, String path, Date timestamp, String errorMessage, @NonNull T details) {
        super(code, path, timestamp, errorMessage);
        this.details = details;
    }

    /**
     * @param status http status
     * @param path origin request path
     * @param timestamp timestamp of the generated error
     * @param errorMessage error message
     * @param details specific details about this error
     */
    public ApiErrorDetails(HttpStatus status, String path,
                           Date timestamp, String errorMessage, @NonNull T details) {
        super(status, path, timestamp, errorMessage);
        this.details = details;
    }
}
