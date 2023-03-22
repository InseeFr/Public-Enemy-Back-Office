package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TextDatatypeType implements IDataType {

    /**
     * used to check that a field value length is less or equals to that maximum length
     */
    private Integer maxLength;

    /**
     * used to check that a field value has this specific pattern
     */
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
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.superior-maxlength", fieldValue, maxLength.toString(), fieldValue.length()+""));
        }

        if(pattern != null && !pattern.isEmpty() && !fieldValue.matches(pattern)) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.format-pattern", fieldValue, pattern));
        }

        if(errorMessages.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessages);
    }
}

