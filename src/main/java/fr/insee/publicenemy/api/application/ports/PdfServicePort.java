package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.PdfRecap;
import fr.insee.publicenemy.api.infrastructure.queen.dto.SimpleInterrogationDto;

public interface PdfServicePort {
    PdfRecap getPdfFromSourceAndData(String lunaticUri, SimpleInterrogationDto interrogation);
}
