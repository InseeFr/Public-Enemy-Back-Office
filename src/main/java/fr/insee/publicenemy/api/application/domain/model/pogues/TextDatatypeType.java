package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextDatatypeType implements IDataType {
    private Integer maxLength;
    private String pattern;

    @JsonCreator
    public TextDatatypeType(@JsonProperty(value="maxLength") Integer maxLength, @JsonProperty(value="pattern") String pattern) {
        this.maxLength = maxLength;
        this.pattern = pattern;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        if(maxLength != null && fieldValue.length() > maxLength) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.superior-maxlength", maxLength.toString(), fieldValue.length()+""));
        }

        if(pattern != null && !fieldValue.matches(pattern)) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.format-pattern", pattern));
        }

        if(errorMessages.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessages);
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

