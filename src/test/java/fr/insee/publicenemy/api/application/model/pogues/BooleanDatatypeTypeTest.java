package fr.insee.publicenemy.api.application.model.pogues;

import fr.insee.publicenemy.api.application.domain.model.pogues.BooleanDatatypeType;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanDatatypeTypeTest {

    private BooleanDatatypeType booleanType;

    @BeforeEach
    public void init() {
        booleanType = new BooleanDatatypeType();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "true", "oui", "yes", "1", "false", "non", "no", "0"})
    void onValidateWhenFieldValueCorrespondsToBooleanValueReturnOkValidationObject(String fieldValue) {
        DataTypeValidation validation = booleanType.validate(fieldValue);
        assertTrue(validation.isValid());
    }

    @Test
    void onValidateWhenFieldValueIncorrectReturnErrorValidationObject() {
        DataTypeValidation validation = booleanType.validate("plop");
        assertFalse(validation.isValid());
        assertTrue(validation.errorMessages().stream().anyMatch(message -> message.code().equals("datatype.error.boolean.incorrect-value")));
    }

    @Test
    void onValidateWhenFieldValueIsNullReturnOkValidationObject() {
        DataTypeValidation validation = booleanType.validate(null);
        assertTrue(validation.isValid());
    }
}
