package fr.insee.publicenemy.api.application.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;

/**
 * This class is used to return non specific API Exceptions
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public class ApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4958764671007300122L;
    private final int statusCode;
    private final String message;

    public ApiException(int statusCode,String message) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
