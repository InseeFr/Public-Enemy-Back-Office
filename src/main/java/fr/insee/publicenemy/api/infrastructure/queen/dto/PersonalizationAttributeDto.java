package fr.insee.publicenemy.api.infrastructure.queen.dto;

public record PersonalizationAttributeDto<T>(String name, T value) {
}
