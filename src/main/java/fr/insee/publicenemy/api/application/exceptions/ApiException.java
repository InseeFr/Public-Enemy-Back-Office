package fr.insee.publicenemy.api.application.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiFieldError;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final List<ApiFieldError> errors;
    private final List<String> messages;

    public ApiException(int statusCode, String message) {
        this(statusCode, new ArrayList<>(Arrays.asList(message)));
    }

    public ApiException(int statusCode, String message, List<ApiFieldError> errors) {
        this(statusCode, new ArrayList<>(Arrays.asList(message)), errors);
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
