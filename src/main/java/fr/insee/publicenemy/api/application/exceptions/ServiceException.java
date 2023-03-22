package fr.insee.publicenemy.api.application.exceptions;

import lombok.Getter;

import java.io.Serial;

@Getter
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7676423077405016593L;
    private final int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
