package fr.insee.publicenemy.api.application.exceptions;

/**
 * Code returned for exceptions
 */
public enum InterrogationExceptionCode {
    SURVEY_UNIT_GLOBAL_VALIDATION_FAILED(1001),
    SURVEY_UNIT_SPECIFIC_VALIDATION_FAILED(1002);

    private final int value;

    InterrogationExceptionCode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
