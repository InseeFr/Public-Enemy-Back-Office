package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CampaignDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("label")
        String label,
        @JsonProperty("questionnaireIds")
        List<String> questionnaireIds,
        @JsonProperty("metadata")
        QuestionnaireMetadataDto metadata) {

        public CampaignDto(String id, String label, QuestionnaireMetadataDto metadata) {
                this(id, label, List.of(id), metadata);
        }
}
