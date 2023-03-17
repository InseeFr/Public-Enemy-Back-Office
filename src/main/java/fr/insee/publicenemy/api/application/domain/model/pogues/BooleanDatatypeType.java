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
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        List<String> correctValues = List.of("", "true", "oui", "yes", "1", "false", "non", "no", "0");
        if(correctValues.contains(fieldValue)) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(
                new DataTypeValidationMessage("datatype.error.boolean.incorrect-value", correctValues.toArray(String[]::new)));
    }
}
