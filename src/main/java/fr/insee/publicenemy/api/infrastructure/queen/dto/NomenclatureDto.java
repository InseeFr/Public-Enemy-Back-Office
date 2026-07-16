package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

public record NomenclatureDto(String id, String label, @JsonRawValue String value) {}
