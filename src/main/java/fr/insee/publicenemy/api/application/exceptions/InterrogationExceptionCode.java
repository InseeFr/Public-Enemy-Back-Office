package fr.insee.publicenemy.api.application.exceptions;

/**
 * Code returned for exceptions
 */
public enum InterrogationExceptionCode {
    INTERROGATION_GLOBAL_VALIDATION_FAILED(1001),
    INTERROGATION_SPECIFIC_VALIDATION_FAILED(1002);

    private final int value;

    InterrogationExceptionCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
