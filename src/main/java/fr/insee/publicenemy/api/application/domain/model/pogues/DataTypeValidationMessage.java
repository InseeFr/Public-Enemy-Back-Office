package fr.insee.publicenemy.api.application.domain.model.pogues;

import java.io.Serial;

public class DataTypeValidationMessage extends ValidationMessage {
    @Serial
    private static final long serialVersionUID = 3597305019701764297L;

    public DataTypeValidationMessage(String code, String[] arguments) {
        super(code, arguments);
    }

    /**
     * @param code message code used for i18n
     * @return a datatype error validation message
     */
    public static DataTypeValidationMessage createMessage(String code) {
        return new DataTypeValidationMessage(code, null);
    }

    /**
     * @param code message code used for i18n
     * @param arguments params to use for this message (field value, ...)
     * @return a data type error validation message
     */
    public static DataTypeValidationMessage createMessage(String code, String... arguments) {
        return new DataTypeValidationMessage(code, arguments);
    }
}
