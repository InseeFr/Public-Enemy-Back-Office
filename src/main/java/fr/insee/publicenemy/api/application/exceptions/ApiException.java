package fr.insee.publicenemy.api.application.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;

import fr.insee.publicenemy.api.application.dto.ApiFieldError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends Exception {
    private final HttpStatus status;
    private final List<ApiFieldError> errors;
    private final List<String> messages;

    /**
     * @param status
     * @param message
     */
    public ApiException(HttpStatus status, String message) {
        this(status, new ArrayList<>(Arrays.asList(message)));
    }

    /**
     * @param status
     * @param message
     * @param errors
     */
    public ApiException(HttpStatus status, String message, List<ApiFieldError> errors) {
        this(status, new ArrayList<>(Arrays.asList(message)), errors);
    }

    /**
     * @param status
     * @param messages
     */
    public ApiException(HttpStatus status, List<String> messages) {
        this(status, messages, null);
    }

    /**
     * @param status
     * @param messages
     * @param errors
     */
    public ApiException(HttpStatus status, List<String> messages, List<ApiFieldError> errors) {
        this.messages = messages;
        this.status = status;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        if (messages == null) {
            return null;
        }
        StringBuilder messageBuilder = new StringBuilder();
        for (int index = 0; index < messages.size(); index++) {
            messageBuilder.append(messages.get(index));
            if (index != messages.size() - 1) {
                messageBuilder.append(" | ");
            }
        }
        return messageBuilder.toString();
    }
}
