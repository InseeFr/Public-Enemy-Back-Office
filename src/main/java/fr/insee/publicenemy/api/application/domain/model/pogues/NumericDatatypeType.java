package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class NumericDatatypeType implements IDataType {

    private BigDecimal minimum;
    private BigDecimal maximum;
    private Integer decimals;

    @JsonCreator
    public NumericDatatypeType(@JsonProperty(value="minimum") BigDecimal minimum,
                               @JsonProperty(value="maximum") BigDecimal maximum,
                               @JsonProperty(value="decimals") Integer decimals) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.decimals = decimals;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        fieldValue = fieldValue.replace(',','.');

        BigDecimal numericValue;

        try {
            numericValue = new BigDecimal(fieldValue);
        } catch (NumberFormatException nfe) {
            return DataTypeValidation.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.format"));
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        if(minimum != null && numericValue.compareTo(minimum) < 0) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.inferior-minimum", minimum.toString()));
        }

        if(maximum != null && numericValue.compareTo(maximum) >= 0) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.superior-maximum", maximum.toString()));
        }

        int scale =  numericValue.stripTrailingZeros().scale();
        if(decimals != null && scale > decimals) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.decimals-precision", decimals.toString()));
        }

        if(errorMessages.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessages);
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

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
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

