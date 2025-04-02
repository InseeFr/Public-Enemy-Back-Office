package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.NumericDatatypeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericDatatypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"11/05/1984", "3-54"})
    void onValidateWhenFieldValueIsInWrongFormatReturnErrorValidationObject(String fieldValue) {
        NumericDatatypeType numericType = new NumericDatatypeType(new BigDecimal("0"), new BigDecimal("10000"), 0);
        DataTypeValidationResult validation = numericType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.numeric.format"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "10000", "10000.0000", "42", "42,0", "42,1234", "42,12340000", " 9 000.00 "})
    void onValidateWhenFieldValueIsInCorrectFormatReturnOkValidationObject(String fieldValue) {
        NumericDatatypeType numericType = new NumericDatatypeType(new BigDecimal("0"), new BigDecimal("10000"), 4);
        DataTypeValidationResult validation = numericType.validate(fieldValue);
        assertTrue(validation.isValid());
    }

    @Test
    void onValidateWhenFieldValueIsInferiorToMinimumReturnErrorValidationObject() {
        String fieldValue = "0";
        NumericDatatypeType numericType = new NumericDatatypeType(new BigDecimal("1"), new BigDecimal("5"), 0);
        DataTypeValidationResult validation = numericType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.numeric.inferior-minimum"));
    }

    @Test
    void onValidateWhenFieldValueIsSuperiorToMaximumReturnErrorValidationObject() {
        String fieldValue = "7";
        NumericDatatypeType numericType = new NumericDatatypeType(new BigDecimal("1"), new BigDecimal("5"), 2);
        DataTypeValidationResult validation = numericType.validate(fieldValue);
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.numeric.superior-maximum"));
    }

    @Test
    void onValidateWhenFieldValueHasIncorrectDecimalsReturnErrorValidationObject() {
        NumericDatatypeType numericType = new NumericDatatypeType(new BigDecimal("1"), new BigDecimal("5"), 2);
        DataTypeValidationResult validation = numericType.validate("14785.123");
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.numeric.decimals-precision"));
    }

    private Boolean hasValidationMessage(List<DataTypeValidationMessage> messages, String code) {
        return messages.stream().anyMatch(message -> message.getCode().equals(code));
    }
}
