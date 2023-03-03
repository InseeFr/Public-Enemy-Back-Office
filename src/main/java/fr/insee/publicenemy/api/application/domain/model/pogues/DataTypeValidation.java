package fr.insee.publicenemy.api.application.domain.model.pogues;

/**
 * Record containing a DataType validation status and error messages if validation failed
 * @param isValid
 * @param errorMessage
 */
public record DataTypeValidation(Boolean isValid, String errorMessage) {
    public static DataTypeValidation createOkDataTypeValidation() {
        return new DataTypeValidation(true, null);
    }

    public static DataTypeValidation createErrorDataTypeValidation(String errorMessage) {
        return new DataTypeValidation(false, errorMessage);
    }
}
