package fr.insee.publicenemy.api.infrastructure.queen.exceptions;

import java.io.Serial;

public class InterrogationsNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5571333541383262369L;

    public InterrogationsNotFoundException(String message) {
        super(message);
    }
}
