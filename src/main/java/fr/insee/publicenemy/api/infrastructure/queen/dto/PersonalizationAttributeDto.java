package fr.insee.publicenemy.api.infrastructure.queen.dto;

import java.util.ArrayList;
import java.util.List;

public record PersonalizationAttributeDto<T>(String name, T value) {

    static List<PersonalizationAttributeDto<String>> getDefaultAttributes() {
        return new ArrayList<>();
    }

}
