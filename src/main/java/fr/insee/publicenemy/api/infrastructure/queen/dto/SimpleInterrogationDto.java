package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record SimpleInterrogationDto(
        String id,
        String questionnaireId,
        JsonNode personalization,
        JsonNode data,
        JsonNode stateData){
}
