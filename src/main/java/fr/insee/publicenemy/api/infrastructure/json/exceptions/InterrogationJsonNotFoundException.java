package fr.insee.publicenemy.api.infrastructure.json.exceptions;

public class InterrogationJsonNotFoundException extends RuntimeException {
    public InterrogationJsonNotFoundException(String message) {
        super(message);
    }
}
