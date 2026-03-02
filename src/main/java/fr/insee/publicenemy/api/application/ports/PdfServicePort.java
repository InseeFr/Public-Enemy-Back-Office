package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.PdfRecap;
import fr.insee.publicenemy.api.infrastructure.queen.dto.SimpleInterrogationDto;
import tools.jackson.databind.JsonNode;

public interface PdfServicePort {
    PdfRecap getPdfFromSourceAndData(JsonNode lunaticModel, SimpleInterrogationDto interrogation);
}
