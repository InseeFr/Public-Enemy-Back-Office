package fr.insee.publicenemy.api.application.domain.model.pogues;

import java.util.ArrayList;
import java.util.List;

/**
 * Record containing a DataType validation status and error messages if validation failed
 * @param isValid return true if the validation returning this object is true
 * @param errorMessages message list specified when objet is not valid
 */
public record DataTypeValidation(Boolean isValid, List<DataTypeValidationMessage> errorMessages) {
    public static DataTypeValidation createOkDataTypeValidation() {
        return new DataTypeValidation(true, new ArrayList<>());
    }

    public static DataTypeValidation createErrorDataTypeValidation(DataTypeValidationMessage errorMessage) {
        return new DataTypeValidation(false, List.of(errorMessage));
    }

    public static DataTypeValidation createErrorDataTypeValidation(List<DataTypeValidationMessage> errorMessages) {
        return new DataTypeValidation(false, errorMessages);
    }
}
