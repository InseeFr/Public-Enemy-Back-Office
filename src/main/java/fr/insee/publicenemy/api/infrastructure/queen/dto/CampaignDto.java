package fr.insee.publicenemy.api.infrastructure.queen.dto;

import java.util.List;

public record CampaignDto(
        String id,
        String label,
        List<String> questionnaireIds,
        QuestionnaireMetadataDto metadata) {

        public CampaignDto(String id, String label, QuestionnaireMetadataDto metadata) {
                this(id, label, List.of(id), metadata);
        }
}
