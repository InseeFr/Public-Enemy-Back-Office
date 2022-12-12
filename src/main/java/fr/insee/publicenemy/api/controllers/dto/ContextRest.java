package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.Context;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContextRest {
    private Long id;
    private String value;

    public static ContextRest createFromModel(Context context) {
        return new ContextRest(context.getId(), context.getValue());
    }

    public static Context mapToModel(ContextRest contextRest) {
        return new Context(contextRest.getId(), contextRest.getValue());
    }
}
