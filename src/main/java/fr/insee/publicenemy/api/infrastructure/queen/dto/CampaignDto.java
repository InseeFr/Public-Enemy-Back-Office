package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.publicenemy.api.infrastructure.queen.QuestionnaireMetadataDto;

import java.util.List;

public record CampaignDto(
        @JsonProperty("id")
        String id,
        @JsonProperty("label")
        String label,
        @JsonProperty("metadata")
        QuestionnaireMetadataDto metadata,
        @JsonProperty("questionnaireIds")
        List<String> questionnaireIds) {

        public CampaignDto(String id, String label, QuestionnaireMetadataDto metadata) {
                this(id, label, metadata, List.of(id));
        }
}
