package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DateDatatypeType implements IDataType {
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
    private DateFormatEnum format;

    @JsonCreator
    public DateDatatypeType(@JsonProperty(value = "Minimum") String minimum, @JsonProperty(value = "Maximum") String maximum,
                            @JsonProperty(value = "Format") DateFormatEnum format) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;
    }

    /**
     * @param fieldValue field value to validate
     * @return data validation object validation success ii successful, object validation failure otherwise
     */
    public DataTypeValidationResult validate(String fieldValue) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        if (format == null) {
            return DataTypeValidationResult.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.date.format-empty")
            );
        }

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(format.getInternalFormat())
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();

        LocalDate date;
        try {
            date = LocalDate.parse(fieldValue, formatter);
        } catch (DateTimeParseException dte) {
            return DataTypeValidationResult.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.date.format-incorrect", fieldValue, format.getInternalFormat())
            );
        }

        try {
            LocalDate dateMinimum = LocalDate.parse(minimum, formatter);
            if (date.isBefore(dateMinimum)) {
                errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.before-minimum", fieldValue, minimum));
            }
        } catch (DateTimeParseException dte) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.format-minimum", minimum, format.getInternalFormat()));
        }

        try {
            LocalDate dateMaximum = LocalDate.parse(maximum, formatter);
            if (date.isAfter(dateMaximum)) {
                errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.after-maximum", fieldValue, maximum));
            }
        } catch (DateTimeParseException dte) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.format-maximum", maximum, format.getInternalFormat()));
        }

        if (errorMessages.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }
        return DataTypeValidationResult.createErrorDataTypeValidation(errorMessages);
    }
}

