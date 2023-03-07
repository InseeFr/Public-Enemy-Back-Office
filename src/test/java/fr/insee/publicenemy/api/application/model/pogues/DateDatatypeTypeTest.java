package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DateDatatypeType;
import fr.insee.publicenemy.api.application.domain.model.pogues.DateFormatEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateDatatypeTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"20222", "11/05/1984", "3-54"})
    void onValidateWhenFieldValueIsInWrongFormatReturnErrorValidationObject(String fieldValue) {
        DateDatatypeType dateType =
                new DateDatatypeType("2020", "2025", DateFormatEnum.YYYY);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.date.format-incorrect"));
    }

    @Test
    void onValidateWhenFieldValueIsInCorrectFormatReturnOkValidationObject() {
        String fieldValue = "2022";
        DateDatatypeType dateType =
                new DateDatatypeType("2020", "2025", DateFormatEnum.YYYY);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertTrue(validation.isValid());
    }

    @Test
    void onValidateWhenFieldValueIsInferiorToMinimumReturnErrorValidationObject() {
        String fieldValue = "2019";
        DateDatatypeType dateType =
                new DateDatatypeType("2020", "2025", DateFormatEnum.YYYY);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.date.before-minimum"));
    }

    @Test
    void onValidateWhenMinimumValueIsInWrongFormatReturnErrorValidationObject() {
        String fieldValue = "2022-01";
        DateDatatypeType dateType =
                new DateDatatypeType("219-01", "2025-01", DateFormatEnum.YYYY_MM);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.date.format-minimum"));
    }

    @Test
    void onValidateWhenMaximumValueIsInWrongFormatReturnErrorValidationObject() {
        String fieldValue = "2022-02-01";
        DateDatatypeType dateType =
                new DateDatatypeType("2019-10-02", "225-11-11", DateFormatEnum.YYYY_MM_DD);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.date.format-maximum"));
    }

    @Test
    void onValidateWhenFieldValueIsSuperiorToMaximumReturnErrorValidationObject() {
        String fieldValue = "2026";
        DateDatatypeType dateType =
                new DateDatatypeType("2020", "2025", DateFormatEnum.YYYY);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.date.after-maximum"));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void onValidateWhenFieldValueIsEmptyOrNullReturnOkValidationObject(String fieldValue) {
        DateDatatypeType dateType =
                new DateDatatypeType("2020", "2025", DateFormatEnum.YYYY);
        DataTypeValidation validation = dateType.validate(fieldValue);
        assertTrue(validation.isValid());
    }

    private Boolean hasValidationMessage(List<DataTypeValidationMessage> messages, String code) {
        return messages.stream().anyMatch(message -> message.code().equals(code));
    }
}
