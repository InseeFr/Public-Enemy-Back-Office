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
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfService implements PdfServicePort {

    private final WebClient webClient;
    private final String lunaticPdfApiUrl;
    private final I18nMessagePort messageService;

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public PdfService(I18nMessagePort messageService, WebClient webClient, @Value("${application.lunatic-pdf-api.url}") String lunaticPdfApiUrl){
        this.webClient = webClient;
        this.lunaticPdfApiUrl = lunaticPdfApiUrl;
        this.messageService = messageService;
    }

    @Override
    public PdfRecap getPdfFromSourceAndData(JsonNode lunaticModel, SimpleInterrogationDto interrogation) {

            PdfRequestDto pdfRequestDto = new PdfRequestDto(
                    lunaticModel,
                    fromSimpleInterrogationDto(interrogation)
            );

        URI uri = UriComponentsBuilder
                .fromUriString(lunaticPdfApiUrl)
                .path("/api/pdf/generate")
                .build()
                .toUri();
        ResponseEntity<byte[]> response = webClient.post()
                .uri(uri)
                .body(BodyInserters.fromValue(pdfRequestDto))
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

    public  InterrogationPdfDto fromSimpleInterrogationDto(SimpleInterrogationDto simpleInterrogationDto){
        return new InterrogationPdfDto(
                simpleInterrogationDto.id(),
                "Unité enquêtée personnalisée",
                simpleInterrogationDto.questionnaireId(),
                getIsoDateFromInterrogation(simpleInterrogationDto),
                simpleInterrogationDto.data());
    }


    private static String getIsoDateFromInterrogation(SimpleInterrogationDto simpleInterrogationDto){
        // if no stateData i.e questionnaire not opened -> fallback to Now
        if(simpleInterrogationDto.stateData() == null || simpleInterrogationDto.stateData().isNull()){
            return getNowIsoDate();
        }
        JsonNode dateField = simpleInterrogationDto.stateData().get("date");

        if(dateField == null || dateField.isNull()) {
            return getNowIsoDate();
        }
        Instant instant = Instant.ofEpochMilli(dateField.longValue());
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        return zonedDateTime.format(formatter);
    }


    private static String getNowIsoDate() {
        ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneId.systemDefault());
        return zonedDateTimeNow.format(formatter);
    }
}
