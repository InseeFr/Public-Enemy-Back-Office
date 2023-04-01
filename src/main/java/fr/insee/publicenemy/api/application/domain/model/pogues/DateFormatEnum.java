package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DateFormatEnum {

    YYYY_MM_DD("YYYY-MM-DD", "yyyy-MM-dd"),
    YYYY_MM("YYYY-MM", "yyyy-MM"),
    YYYY("YYYY", "yyyy");
    private final String questionnaireFormat;

    private final String internalFormat;

    DateFormatEnum(String questionnaireFormat, String internalFormat) {
        this.questionnaireFormat = questionnaireFormat;
        this.internalFormat = internalFormat;
    }

    @JsonValue
    public String getQuestionnaireFormat() {
        return questionnaireFormat;
    }

    public String getInternalFormat() {
        return internalFormat;
    }


}
