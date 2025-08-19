package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InterrogationSurveyUnitDto(
        @JsonProperty("interrogationId")
        String interrogationId,
        @JsonProperty("campaignId")
        String campaignId) {
}
