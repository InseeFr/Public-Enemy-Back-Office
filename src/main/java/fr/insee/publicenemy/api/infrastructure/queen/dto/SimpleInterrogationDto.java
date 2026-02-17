package fr.insee.publicenemy.api.infrastructure.queen.dto;


import tools.jackson.databind.JsonNode;

public record SimpleInterrogationDto(
        String id,
        String questionnaireId,
        JsonNode personalization,
        JsonNode data,
        JsonNode stateData){
}
