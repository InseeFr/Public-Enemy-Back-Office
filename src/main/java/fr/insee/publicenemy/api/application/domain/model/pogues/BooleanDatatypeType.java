package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class BooleanDatatypeType implements IDataType {
    @JsonCreator
    public BooleanDatatypeType() {
        super();
    }

    @Override
    public DataTypeValidationResult validate(String fieldValue) {
        if (fieldValue == null) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        List<String> correctValues = List.of("", "1", "0");
        if (correctValues.contains(fieldValue)) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        String delimiter = ", ";
        String correctValuesString = String.join(delimiter, correctValues);

        return DataTypeValidationResult.createErrorDataTypeValidation(
                DataTypeValidationMessage.createMessage("datatype.error.boolean.incorrect-value", fieldValue, correctValuesString));
    }
}
