package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;


public class NumericDatatypeType extends DataType {

    private BigDecimal minimum;
    private BigDecimal maximum;
    private Integer decimals;

    @JsonCreator
    public NumericDatatypeType(@JsonProperty(value="type") String type, @JsonProperty(value="typename") String typeName,
                               @JsonProperty(value="minimum") BigDecimal minimum, @JsonProperty(value="maximum") BigDecimal maximum,
                               @JsonProperty(value="decimals") Integer decimals) {
        super(type, typeName);
        this.minimum = minimum;
        this.maximum = maximum;
        this.decimals = decimals;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        BigDecimal numericValue = null;

        try {
            numericValue = new BigDecimal(fieldValue);
        } catch (NumberFormatException nfe) {
            return DataTypeValidation.createErrorDataTypeValidation("Value is not in numeric format");
        }

        StringBuilder errorMessage = new StringBuilder();

        if(minimum != null && numericValue.compareTo(minimum) == -1) {
            errorMessage.append(String.format("Value should be < %s. ", minimum));
        }

        if(maximum != null && numericValue.compareTo(minimum) >= 0) {
            errorMessage.append(String.format("Value should be >= %s. ", maximum));
        }

        int scale =  numericValue.stripTrailingZeros().scale();
        if(decimals != null && scale > decimals) {
            errorMessage.append(String.format("Value should have a precision decimal value of %s (%s at this time). ", decimals, scale));
        }

        if(errorMessage.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessage.toString());
    }

    public BigDecimal getMinimum() {
        return minimum;
    }

    public void setMinimum(BigDecimal minimum) {
        this.minimum = minimum;
    }

    public BigDecimal getMaximum() {
        return maximum;
    }

    public void setMaximum(BigDecimal maximum) {
        this.maximum = maximum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NumericDatatypeType that = (NumericDatatypeType) o;
        return Objects.equals(minimum, that.minimum) && Objects.equals(maximum, that.maximum) && Objects.equals(decimals, that.decimals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minimum, maximum, decimals);
    }

    @Override
    public String toString() {
        return "NumericDataType{" +
                "minimum=" + minimum +
                ", maximum=" + maximum +
                ", decimals=" + decimals +
                '}';
    }
}

