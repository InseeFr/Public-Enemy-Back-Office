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
    public TextDatatypeType(@JsonProperty(value = "MaxLength") Integer maxLength, @JsonProperty(value = "Pattern") String pattern) {
        this.maxLength = maxLength;
        this.pattern = pattern;
    }

    @Override
    public DataTypeValidationResult validate(Object abstractFieldValue) {
        String fieldValue = String.valueOf(abstractFieldValue);
        if (abstractFieldValue == null || fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();


        /*
         * Add new condition maxLength != 1
         * For QCU/QCM a Suggester -> response base on id of code-list, the associated variable constructed by Pogues
         * is mal formed. The variable is always a string of maxLength 1.
         * But the id of code-list can have max length of 3, 4, and more.
         *
         * Temporarily, for checking value of variable, we skip validation length when maxLength of variable is 1.
         *
         * TODO: remove this condition when modeling of QCU/QCM/suggester variable will be improved.
         */
        if (maxLength != null && fieldValue.length() > maxLength && maxLength != 1) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.superior-maxlength", fieldValue, maxLength.toString(), fieldValue.length() + ""));
        }

        if (pattern != null && !pattern.isEmpty() && !fieldValue.matches(pattern)) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.text.format-pattern", fieldValue, pattern));
        }

        if (errorMessages.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }
        return DataTypeValidationResult.createErrorDataTypeValidation(errorMessages);
    }
}

