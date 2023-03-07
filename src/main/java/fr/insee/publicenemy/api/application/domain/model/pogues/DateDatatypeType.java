package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DateDatatypeType implements IDataType {
    private String minimum;
    private String maximum;
    private DateFormatEnum format;

    @JsonCreator
    public DateDatatypeType(@JsonProperty(value="minimum") String minimum, @JsonProperty(value="maximum") String maximum,
                            @JsonProperty(value="format") DateFormatEnum format) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        if(format == null) {
            return DataTypeValidation.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.date.format-empty")
            );
        }

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern(format.value())
                .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();

        LocalDate date;
        try {
            date = LocalDate.parse(fieldValue, formatter);
        } catch (DateTimeParseException dte) {
            return DataTypeValidation.createErrorDataTypeValidation(
                    DataTypeValidationMessage.createMessage("datatype.error.date.format-incorrect", fieldValue, format.value())
            );
        }

        try {
            LocalDate dateMinimum = LocalDate.parse(minimum, formatter);
            if(date.isBefore(dateMinimum)) {
                errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.before-minimum", fieldValue, minimum));
            }
        } catch (DateTimeParseException dte) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.format-minimum", minimum, format.value()));
        }

        try {
            LocalDate dateMaximum = LocalDate.parse(maximum, formatter);
            if(date.isAfter(dateMaximum)) {
                errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.after-maximum", fieldValue, maximum));
            }
        } catch (DateTimeParseException dte) {
            errorMessages.add(DataTypeValidationMessage.createMessage("datatype.error.date.format-maximum", maximum, format.value()));
        }

        if(errorMessages.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessages);
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

    public DateFormatEnum getFormat() {
        return format;
    }

    public void setFormat(DateFormatEnum format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DateDatatypeType that = (DateDatatypeType) o;
        return Objects.equals(minimum, that.minimum) && Objects.equals(maximum, that.maximum) && format == that.format;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minimum, maximum, format);
    }

    @Override
    public String toString() {
        return "DateDatatypeType{" +
                "minimum='" + minimum + '\'' +
                ", maximum='" + maximum + '\'' +
                ", format=" + format +
                '}';
    }
}

