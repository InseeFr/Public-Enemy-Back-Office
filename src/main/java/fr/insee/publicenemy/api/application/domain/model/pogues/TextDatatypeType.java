package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TextDatatypeType extends DataType {
    private Integer maxLength;
    private String pattern;

    @JsonCreator
    public TextDatatypeType(@JsonProperty(value="type") String type, @JsonProperty(value="typename") String typeName,
                            @JsonProperty(value="maxLength") Integer maxLength, @JsonProperty(value="pattern") String pattern) {
        super(type, typeName);
        this.maxLength = maxLength;
        this.pattern = pattern;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        StringBuilder errorMessage = new StringBuilder();

        if(maxLength != null && fieldValue.length() < maxLength) {
            errorMessage.append(String.format("Value should be < to %s characters (%s at this time). ", maxLength, fieldValue.length()));
        }

        if(pattern != null && !fieldValue.matches(pattern)) {
            errorMessage.append(String.format("Value should have the following pattern: %s.", pattern));
        }

        if(errorMessage.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessage.toString());
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TextDatatypeType that = (TextDatatypeType) o;
        return Objects.equals(maxLength, that.maxLength) && Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxLength, pattern);
    }

    @Override
    public String toString() {
        return "TextDataType{" +
                "maxLength=" + maxLength +
                ", pattern='" + pattern + '\'' +
                '}';
    }
}

