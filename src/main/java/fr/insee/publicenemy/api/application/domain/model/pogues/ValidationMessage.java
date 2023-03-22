package fr.insee.publicenemy.api.application.domain.model.pogues;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ValidationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1609556194717727842L;

    private String code;
    private String[] arguments;

    /**
     * @param code message code used for i18n
     */
    public ValidationMessage(String code) {
       this.code = code;
       this.arguments = null;
    }

    /**
     * @param code message code used for i18n
     * @param arguments params to use for this message (field value, ...)
     */
    public ValidationMessage(String code, String... arguments) {
        this.code = code;
        this.arguments = arguments;
    }
}
