package fr.insee.publicenemy.api.controllers.dto;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModeRest {
    private Long id;
    
    private String value;

    public static ModeRest createFromModel(Mode mode) {
        return new ModeRest(mode.getId(), mode.getValue());
    }

    public static Mode mapToModel(ModeRest modeRest) {
        return new Mode(modeRest.getId(), modeRest.getValue());
    }
}
