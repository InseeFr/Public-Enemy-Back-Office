package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.TextDatatypeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextDatatypeTypeTest {
    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"", "plop", "hello world", "0"})
    void onValidateWhenFieldValueCorrespondsToStringValueReturnOkValidationObject(String fieldValue) {
        TextDatatypeType textType = new TextDatatypeType(null, null);
        DataTypeValidation validation = textType.validate(fieldValue);
        assertTrue(validation.isValid());
    }

    @Test
    void onValidateWhenFieldValueSuperiorToMaxLengthReturnErrorValidationObject() {
        TextDatatypeType textType = new TextDatatypeType(2, null);
        DataTypeValidation validation = textType.validate("plop");
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.text.superior-maxlength"));
    }

    @Test
    void onValidateWhenFieldValueMatchPatternReturnOkValidationObject() {
        TextDatatypeType textType = new TextDatatypeType(20, "[a-z]*");
        DataTypeValidation validation = textType.validate("plop");
        assertTrue(validation.isValid());
    }

    @Test
    void onValidateWhenFieldValueDoesntMatchPatternReturnErrorValidationObject() {
        TextDatatypeType textType = new TextDatatypeType(20, "[a-z]*");
        DataTypeValidation validation = textType.validate("PLOP");
        assertFalse(validation.isValid());
        assertTrue(hasValidationMessage(validation.errorMessages(), "datatype.error.text.format-pattern"));
    }

    Boolean hasValidationMessage(List<DataTypeValidationMessage> messages, String code) {
        return messages.stream().anyMatch(message -> message.code().equals(code));
    }
}
