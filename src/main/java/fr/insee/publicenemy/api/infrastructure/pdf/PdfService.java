package fr.insee.publicenemy.api.infrastructure.pdf;

import fr.insee.publicenemy.api.application.domain.model.PdfRecap;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.PdfServicePort;
import fr.insee.publicenemy.api.infrastructure.queen.dto.SimpleInterrogationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
@Slf4j
public class PdfService implements PdfServicePort {

    private final WebClient webClient;
    private final String lunaticPdfApiUrl;
    private final I18nMessagePort messageService;

    public PdfService(I18nMessagePort messageService, WebClient webClient, @Value("${application.lunatic-pdf-api.url}") String lunaticPdfApiUrl){
        this.webClient = webClient;
        this.lunaticPdfApiUrl = lunaticPdfApiUrl;
        this.messageService = messageService;
    }

    @Override
    public PdfRecap getPdfFromSourceAndData(String lunaticUri, SimpleInterrogationDto interrogation) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(lunaticPdfApiUrl)
                .path("/api/pdf/generate-from-source")
                .queryParam("source", lunaticUri)
                .build()
                .toUri();
        ResponseEntity<byte[]> response = webClient.post()
                .uri(uri)
                .body(BodyInserters.fromValue(interrogation))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        httpResponse -> Mono.error(new ServiceException(HttpStatus.valueOf(httpResponse.statusCode().value()),
                                messageService.getMessage("interrogation.error.pdf", interrogation.id())))
                )
                .toEntity(byte[].class)
                .block();
        if (response == null || response.getBody() == null) {
            throw new ServiceException(HttpStatus.valueOf(500),
                    messageService.getMessage("interrogation.error.pdf", interrogation.id()));
        }
        String filename = "document.pdf"; // default value
        if (response.getHeaders().getContentDisposition().getFilename() != null) {
            filename = response.getHeaders().getContentDisposition().getFilename();
        }
        return new PdfRecap(filename, response.getBody());
    }
}
