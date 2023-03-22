package fr.insee.publicenemy.api.application.domain.model.pogues;

import java.io.Serial;

public class ValidationErrorMessage extends ValidationMessage {
    @Serial
    private static final long serialVersionUID = 8545619328960707433L;

    public ValidationErrorMessage(String code) {
        super(code);
    }

    public ValidationErrorMessage(String code, String... arguments) {
        super(code, arguments);
    }
}
