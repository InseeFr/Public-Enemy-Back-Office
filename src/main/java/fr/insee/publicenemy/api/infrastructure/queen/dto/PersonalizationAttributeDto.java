package fr.insee.publicenemy.api.infrastructure.queen.dto;

import java.util.ArrayList;
import java.util.List;

public record PersonalizationAttributeDto<T>(String name, T value) {

    static List<PersonalizationAttributeDto<String>> getDefaultAttributes() {
        List<PersonalizationAttributeDto<String>> personalizationData = new ArrayList<>();
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers1", "Mr Dupond"));
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers2", ""));
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers3", ""));
        return personalizationData;
    }

}
