package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DurationDatatypeType extends DataType {

    private String minimum;
    private String maximum;
    private String format;

    @JsonCreator
    public DurationDatatypeType(@JsonProperty(value="type") String type, @JsonProperty(value="typename") String typeName,
                                @JsonProperty(value="minimum") String minimum, @JsonProperty(value="maximum") String maximum,
                                @JsonProperty(value="format") String format) {
        super(type, typeName);
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        return null;
    }

    public String getMinimum() {
        return minimum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DurationDatatypeType that = (DurationDatatypeType) o;
        return Objects.equals(minimum, that.minimum) && Objects.equals(maximum, that.maximum) && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minimum, maximum, format);
    }

    @Override
    public String toString() {
        return "DurationDataType{" +
                "minimum='" + minimum + '\'' +
                ", maximum='" + maximum + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}

