package fr.insee.publicenemy.api.application.domain.model.pogues;

import java.io.Serial;

public class ValidationWarningMessage extends ValidationMessage {

    @Serial
    private static final long serialVersionUID = 2975152486460059056L;

    public ValidationWarningMessage(String code, String... arguments) {
        super(code, arguments);
    }
}
