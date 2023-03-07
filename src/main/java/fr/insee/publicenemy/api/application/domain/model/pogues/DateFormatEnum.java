package fr.insee.publicenemy.api.application.domain.model.pogues;

public enum DateFormatEnum {

    YYYY_MM_DD("yyyy-MM-dd"),
    YYYY_MM("yyyy-MM"),
    YYYY("yyyy");
    private final String value;

    DateFormatEnum(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
}
