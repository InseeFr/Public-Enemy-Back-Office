package fr.insee.publicenemy.api.infrastructure.queen;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.configuration.MetadataProps;
import fr.insee.publicenemy.api.infrastructure.queen.dto.*;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.InterrogationsNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class QueenServiceImpl implements QueenServicePort {

    private final MetadataProps metadataProps;
    private final WebClient webClient;
    private final String queenUrl;

    private final I18nMessagePort messageService;

    private static final String INTERROGATION_PATH = "/api/interrogations/{id}";
    private static final String INTERROGATION_NOT_FOUND_MSG = "queen.error.interrogation.not-found";

    public QueenServiceImpl(I18nMessagePort messagePort, WebClient webClient, @Value("${application.queen.url}") String queenUrl,
                            MetadataProps metadataProps) {
        this.webClient = webClient;
        this.queenUrl = queenUrl;
        this.metadataProps = metadataProps;
        this.messageService = messagePort;
    }

    public void createQuestionnaireModel(String questionnaireModelId, @NotNull QuestionnaireModel questionnaireModel, @NotNull JsonLunatic jsonLunatic) {
        QuestionnaireModelDto questionnaireModelDto = new QuestionnaireModelDto(questionnaireModelId, questionnaireModel.label(), new ArrayList<>(), jsonLunatic.jsonContent());

        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/questionnaire-models")
                .build()
                .toUri();

        webClient.post().uri(uri)
                .body(BodyInserters.fromValue(questionnaireModelDto))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), errorMessage)))
                )
                .toBodilessEntity()
                .block();
    }

    @Override
    public boolean hasQuestionnaireModel(String questionnaireModelId) {

        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/questionnaire/{id}/data")
                .build(questionnaireModelId);

        JsonNode result = webClient.get().uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.empty()
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), errorMessage)))
                )
                .bodyToMono(JsonNode.class)
                .block();

        return result != null && !result.isEmpty();
    }

    public void createCampaign(@NotNull String campaignId, @NotNull Questionnaire questionnaire, QuestionnaireModel questionnaireModel) {
        QuestionnaireMetadataDto metadata = QuestionnaireMetadataDto.createDefaultQuestionnaireMetadata(questionnaire, metadataProps.getMetadata());
        CampaignDto campaign = new CampaignDto(campaignId, questionnaireModel.label(), metadata);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign")
                .build()
                .toUri();

        webClient.post().uri(uri)
                .body(BodyInserters.fromValue(campaign))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.su.create", campaignId)))
                )
                .toBodilessEntity()
                .block();
    }

    public void deleteCampaign(String campaignId) throws CampaignNotFoundException {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}")
                .queryParam("force", true)
                .build(campaignId);

        webClient.delete()
                .uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new CampaignNotFoundException(messageService.getMessage("queen.error.campaign.not-found", campaignId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.delete", campaignId)))
                )
                .toBodilessEntity()
                .block();
    }

    public void createInterrogations(@NotNull String questionnaireModelId, @NotNull List<Interrogation> interrogations) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}/interrogation")
                .build(questionnaireModelId);

        List<InterrogationDto> interrogationDtos = interrogations.stream().map(InterrogationDto::fromModel).toList();
        interrogationDtos.forEach(interrogationDto ->
                webClient.post().uri(uri)
                        .body(BodyInserters.fromValue(interrogationDto))
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                        messageService.getMessage("queen.error.campaign.su.create", interrogationDto.id(), questionnaireModelId)))
                        )
                        .toBodilessEntity()
                        .block());
    }

    public void createInterrogation(@NotNull String questionnaireModelId, @NotNull Interrogation interrogation) {

        InterrogationDto interrogationDto = InterrogationDto.fromModel(interrogation);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}/interrogation")
                .build(questionnaireModelId);

        webClient.post().uri(uri)
                .body(BodyInserters.fromValue(interrogationDto))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), messageService.getMessage("queen.error.campaign.su.create", interrogation.id(), questionnaireModelId)))
                )
                .toBodilessEntity()
                .block();
    }

    public List<SimpleInterrogationDto> getInterrogations(@NotNull String campaignId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}/interrogations")
                .build(campaignId);

        return webClient.get().uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new InterrogationsNotFoundException(messageService.getMessage("queen.error.campaign.su.not-found", campaignId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.su", campaignId)))
                )
                .bodyToMono(new ParameterizedTypeReference<List<SimpleInterrogationDto>>() {
                })
                .blockOptional()
                .orElseThrow(() -> new InterrogationsNotFoundException(messageService.getMessage("queen.error.campaign.su.not-found", campaignId)));
    }

    public SimpleInterrogationDto getInterrogation(@NotNull String interrogationId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path(INTERROGATION_PATH)
                .build(interrogationId);

        return webClient.get().uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new InterrogationsNotFoundException(messageService.getMessage(INTERROGATION_NOT_FOUND_MSG, interrogationId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.interrogation", interrogationId)))
                )
                .bodyToMono(new ParameterizedTypeReference<SimpleInterrogationDto>() {
                })
                .blockOptional()
                .orElseThrow(() -> new InterrogationsNotFoundException(messageService.getMessage(INTERROGATION_NOT_FOUND_MSG, interrogationId)));
    }

    public void updateInterrogation(@NotNull Interrogation interrogation) {
        InterrogationUpdateDto interrogationUpdateDto = InterrogationUpdateDto.fromModel(interrogation);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path(INTERROGATION_PATH)
                .build(interrogation.id());

        webClient.put().uri(uri)
                .body(BodyInserters.fromValue(interrogationUpdateDto))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.su.update", interrogation.id(), interrogation.questionnaireModelId())))
                )
                .toBodilessEntity()
                .block();
    }

    @Override
    public void deteteInterrogation(Interrogation interrogation) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path(INTERROGATION_PATH)
                .build(interrogation.id());

        webClient.delete()
                .uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new InterrogationsNotFoundException(messageService.getMessage("interrogation.not-found", interrogation.id())))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("interrogation.error.delete", interrogation.id())))
                )
                .toBodilessEntity()
                .block();
    }
}
