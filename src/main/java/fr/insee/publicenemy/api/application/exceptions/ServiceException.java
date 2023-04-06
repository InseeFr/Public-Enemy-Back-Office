package fr.insee.publicenemy.api.application.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7676423077405016593L;
    private final HttpStatus status;

    public ServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
