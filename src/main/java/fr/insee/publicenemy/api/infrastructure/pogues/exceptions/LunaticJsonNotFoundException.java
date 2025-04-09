package fr.insee.publicenemy.api.infrastructure.pogues.exceptions;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;

public class LunaticJsonNotFoundException extends RuntimeException {
    public LunaticJsonNotFoundException(String poguesId, Context context, Mode mode){
        super(String.format("Lunatic JSON is empty for pogues id: %s - context: %s - mode: %s", poguesId, context.name(), mode.name()));
    }
}
