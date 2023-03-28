package fr.insee.publicenemy.api.controllers.exceptions.dto;

import java.util.Arrays;
/**
 * ENUM of specific API error codes for this application
 */
public enum ApiErrorCode {
    SURVEY_UNITS_GLOBAL_ERRORS(1001),
    SURVEY_UNITS_DETAILS_ERRORS(1002);

    private final int value;

    ApiErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean contains(int code) {
        return Arrays.stream(ApiErrorCode.values()).anyMatch(errorCodeEnum -> errorCodeEnum.getValue() == code);
    }
}

