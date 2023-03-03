package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VariableTypeEnum{
    COLLECTED("CollectedVariableType"),
    CALCULATED("CalculatedVariableType"),
    EXTERNAL("ExternalVariableType");

    private final String value;

    VariableTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}