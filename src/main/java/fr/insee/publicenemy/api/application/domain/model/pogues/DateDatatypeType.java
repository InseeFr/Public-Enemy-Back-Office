package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class DateDatatypeType extends DataType {
    private String minimum;
    private String maximum;
    private String format;

    @JsonCreator
    public DateDatatypeType(@JsonProperty(value="type") String type, @JsonProperty(value="typename") String typeName,
                            @JsonProperty(value="minimum") String minimum, @JsonProperty(value="maximum") String maximum,
                            @JsonProperty(value="format") String format) {
        super(type, typeName);
        this.minimum = minimum;
        this.maximum = maximum;
        this.format = format;
    }

    @Override
    public DataTypeValidation validate(String fieldValue) {
        if(fieldValue == null || fieldValue.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        StringBuilder errorMessage = new StringBuilder();

        if(format == null || format.isEmpty()) {
            errorMessage.append(String.format("The date format is empty, you should fill it in Pogues"));
            return DataTypeValidation.createErrorDataTypeValidation(errorMessage.toString());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate date;

        try {
            date = LocalDate.parse(fieldValue, formatter);
        } catch (DateTimeParseException dte) {
            errorMessage.append(String.format("The date %s could not be parsed, this date is not in specified format: %s", fieldValue, format));
            return DataTypeValidation.createErrorDataTypeValidation(errorMessage.toString());
        }

        try {
            LocalDate dateMinimum = LocalDate.parse(minimum, formatter);
            if(date.isBefore(dateMinimum)) {
                errorMessage.append(String.format("The date %s is before the minimum date: %s", fieldValue, minimum));
            }
        } catch (DateTimeParseException dte) {
            errorMessage.append(String.format("The minimum date %s could not be parsed, this date is not in specified format: %s", minimum, format));
        }

        try {
            LocalDate dateMaximum = LocalDate.parse(maximum, formatter);
            if(date.isAfter(dateMaximum)) {
                errorMessage.append(String.format("The date %s is after the maximum date: %s", fieldValue, maximum));
            }
        } catch (DateTimeParseException dte) {
            errorMessage.append(String.format("The maximum date %s could not be parsed, this date is not in specified format: %s", maximum, format));
        }

        if(errorMessage.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }
        return DataTypeValidation.createErrorDataTypeValidation(errorMessage.toString());
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
        DateDatatypeType that = (DateDatatypeType) o;
        return Objects.equals(minimum, that.minimum) && Objects.equals(maximum, that.maximum) && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minimum, maximum, format);
    }

    @Override
    public String toString() {
        return "DateDataType{" +
                "minimum='" + minimum + '\'' +
                ", maximum='" + maximum + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}

