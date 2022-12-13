package fr.insee.publicenemy.api.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    private Long id;

    private String value;

    public Context(Long id) {
        this.id = id;
    }
}
