package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class NumericDatatypeType implements IDataType {

    /**
     * used to check that a field value is equals or greater to that minimum field
     */
    private BigDecimal minimum;

    /**
     * used to check that a field value is less or equals to that maximum field
     */
    private BigDecimal maximum;

    /**
     * used to calculate the number of decimals allowed in a field value
     */
    private Integer decimals;

    @JsonCreator
    public NumericDatatypeType(@JsonProperty(value = "Minimum") BigDecimal minimum,
                               @JsonProperty(value = "Maximum") BigDecimal maximum,
                               @JsonProperty(value = "Decimals") Integer decimals) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.decimals = decimals;
    }

    /**
     * @param fieldValue field value to validate
     * @return data validation object validation success ii successful, object validation failure otherwise
     */
    public DataTypeValidationResult validate(String fieldValue) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        // replace decimal separator in a more javaish way,
        // handle the thousand separator and trim
        fieldValue = fieldValue
                .replace(',', '.')
                .replaceAll("\\s", "");

        BigDecimal numericValue;

        try {
            numericValue = new BigDecimal(fieldValue);
        } catch (NumberFormatException nfe) {
            return DataTypeValidationResult.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.format"));
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        if (minimum != null && numericValue.compareTo(minimum) < 0) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.inferior-minimum", fieldValue, minimum.toString()));
        }

        if (maximum != null && numericValue.compareTo(maximum) > 0) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.superior-maximum", fieldValue, maximum.toString()));
        }

        int scale = numericValue.stripTrailingZeros().scale();
        if (decimals != null && scale > decimals) {
            errorMessages.add(
                    DataTypeValidationMessage.createMessage("datatype.error.numeric.decimals-precision", fieldValue, decimals.toString(), scale + ""));
        }

        if (errorMessages.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }
        return DataTypeValidationResult.createErrorDataTypeValidation(errorMessages);
    }
}

