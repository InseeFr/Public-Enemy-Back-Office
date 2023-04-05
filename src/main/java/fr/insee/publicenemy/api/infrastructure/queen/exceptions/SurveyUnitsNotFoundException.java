package fr.insee.publicenemy.api.infrastructure.queen.exceptions;

import java.io.Serial;

public class SurveyUnitsNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 5571333541383262369L;

    public SurveyUnitsNotFoundException(String message) {
        super(message);
    }
}
