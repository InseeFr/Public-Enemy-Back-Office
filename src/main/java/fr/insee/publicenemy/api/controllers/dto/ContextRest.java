package fr.insee.publicenemy.api.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContextRest {
    private String name;
    private String value;
}
