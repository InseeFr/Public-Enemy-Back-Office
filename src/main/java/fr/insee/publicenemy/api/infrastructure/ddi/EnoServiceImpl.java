package fr.insee.publicenemy.api.infrastructure.ddi;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.EnoServicePort;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.LunaticJsonNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    public JsonLunatic getJsonLunatic(@NonNull Ddi ddi, @NonNull Context context, @NonNull Mode mode) {

        MultipartBodyBuilder resourceBuilder = new MultipartBodyBuilder();
        Resource ddiResource = new FileNameAwareByteArrayResource("resource.json", ddi.content(), "description");
        resourceBuilder.part("in", ddiResource);
        
        String lunaticJson = webClient.post().uri(enoUrl + "/questionnaire/{context}/lunatic-json/{mode}", context.name(), mode.name())
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(resourceBuilder.build()))
            .retrieve()
            .onStatus(
                    HttpStatus.NOT_FOUND::equals,
                    response -> response.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new LunaticJsonNotFoundException(ddi.poguesId(), context, mode)))
            )
            .onStatus(
                    HttpStatusCode::isError,
                    response -> response.bodyToMono(String.class)
                            .flatMap(errorMessage -> Mono.error(new ServiceException(response.statusCode().value(), errorMessage)))
            )
            .bodyToMono(String.class).blockOptional().orElseThrow(() -> new LunaticJsonNotFoundException(ddi.poguesId(), context, mode));

        return new JsonLunatic(lunaticJson);
    } 
}
