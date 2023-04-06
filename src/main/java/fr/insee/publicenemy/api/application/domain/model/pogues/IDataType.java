package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME,
        property = "type",
        visible = true)
@JsonSubTypes({
        @Type(value = DateDatatypeType.class),
        @Type(value = BooleanDatatypeType.class),
        @Type(value = DurationDatatypeType.class),
        @Type(value = NumericDatatypeType.class),
        @Type(value = TextDatatypeType.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public interface IDataType {

    /**
     * @param fieldValue field value to validate
     * @return validation object. validation object status is "true" if field value is valid, "false" otherwise. validation
     * object contains validation error messages if the field value is not valid
     */
    DataTypeValidationResult validate(String fieldValue);

}