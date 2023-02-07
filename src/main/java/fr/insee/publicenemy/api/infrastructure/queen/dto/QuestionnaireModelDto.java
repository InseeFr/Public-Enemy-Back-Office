package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

public record QuestionnaireModelDto(
    @JsonProperty("idQuestionnaireModel")
    String id,
    @JsonProperty("label")
    String label,
    @JsonProperty("requiredNomenclatureIds")
    List<String> requiredNomenclatureIds,
    @JsonRawValue
    @JsonProperty("value")
    String jsonLunatic) {}
