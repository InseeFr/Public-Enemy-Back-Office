package fr.insee.publicenemy.api.infrastructure.pdf;

import tools.jackson.databind.JsonNode;

public record PdfRequestDto(
        JsonNode source,
        InterrogationPdfDto interrogation
) {
}
