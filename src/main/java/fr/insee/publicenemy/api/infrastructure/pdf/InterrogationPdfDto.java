package fr.insee.publicenemy.api.infrastructure.pdf;


import tools.jackson.databind.JsonNode;

public record InterrogationPdfDto(
        String interrogationId,
        String usualSurveyUnitId,
        String collectionInstrumentId,
        String validationDate,
        JsonNode data
) {
}
