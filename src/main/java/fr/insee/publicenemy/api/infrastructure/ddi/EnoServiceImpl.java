package fr.insee.publicenemy.api.infrastructure.ddi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.ports.EnoServicePort;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnoServiceImpl implements EnoServicePort {

    private final WebClient webClient;
    private final String enoUrl;

    public EnoServiceImpl(WebClient webClient, @Value("${application.eno.url}") String enoUrl) {
        this.webClient = webClient; 
        this.enoUrl = enoUrl;
    }

    @Override
    public JsonLunatic getJsonLunatic(Ddi ddi, Context context, Mode mode) {    

        MultipartBodyBuilder resourceBuilder = new MultipartBodyBuilder();
        Resource ddiResource = new FileNameAwareByteArrayResource("resource.json", ddi.getContent(), "description");
        resourceBuilder.part("in", ddiResource);
        
        byte[] lunaticJsonBytes = webClient.post().uri(enoUrl + "/questionnaire/{context}/lunatic-json/{mode}", context.name(), mode.name())
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(resourceBuilder.build()))
            .retrieve()
            .onStatus(status-> status.is4xxClientError()||status.is5xxServerError(), clientResponse -> {
                log.error("Error retrieving DDI - ENO response: "+ clientResponse.statusCode().value());
                clientResponse.toEntity(String.class).subscribe(s->log.error(s.getBody()));
                return clientResponse.createException();
            })
            .bodyToMono(byte[].class).block();

        return new JsonLunatic(lunaticJsonBytes);
    } 
}
