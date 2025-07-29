package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DurationDatatypeType implements IDataType {

    /**
     * used to check that a field value is equals or greater to that minimum field
     */
    private String minimum;

    /**
     * used to check that a field value is less or equals to that maximum field
     */
    private String maximum;

    /**
     * used to check that a field value/minimum/maximum has this specific format
     */
    private String format;

    @JsonCreator
    public DurationDatatypeType(@JsonProperty(value = "Minimum") String minimum, @JsonProperty(value = "Maximum") String maximum,
                                @JsonProperty(value = "Format") String format) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;
    }

    @Override
    public DataTypeValidationResult validate(Object fieldValue) {
        throw new IllegalArgumentException("Validate method is not yet implemented");
    }
}

