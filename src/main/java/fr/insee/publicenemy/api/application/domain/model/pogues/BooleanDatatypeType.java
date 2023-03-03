package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BooleanDatatypeType extends DataType {
    @JsonCreator
    public BooleanDatatypeType(@JsonProperty(value="type") String type, @JsonProperty(value="typename") String typeName) {
        super(type, typeName);
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        List<String> correctValues = List.of(null, "", "true", "oui", "yes", "1", "false", "non", "no", "0");
        if(correctValues.contains(fieldValue)) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(
                String.format("Value should contain one of the following values: %s", correctValues.toString()));
    }

    @Override
    public String toString() {
        return "BooleanDataType{" +
                "type='" + getType() + '\'' +
                ", typeName='" + getTypeName() + '\'' +
                '}';
    }
}
