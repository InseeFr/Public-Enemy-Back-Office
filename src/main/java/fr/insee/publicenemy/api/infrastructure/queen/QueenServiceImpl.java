package fr.insee.publicenemy.api.infrastructure.queen;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QueenServicePort;
import fr.insee.publicenemy.api.configuration.MetadataProps;
import fr.insee.publicenemy.api.infrastructure.queen.dto.*;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.CampaignNotFoundException;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.SurveyUnitsNotFoundException;
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

    public QueenServiceImpl(I18nMessagePort messagePort, WebClient webClient, @Value("${application.queen.url}") String queenUrl,
                            MetadataProps metadataProps) {
        this.webClient = webClient;
        this.queenUrl = queenUrl;
        this.metadataProps = metadataProps;
        this.messageService = messagePort;
    }

    public void createQuestionnaireModel(String questionnaireModelId, @NotNull Ddi ddi, @NotNull JsonLunatic jsonLunatic) {
        QuestionnaireModelDto questionnaireModel = new QuestionnaireModelDto(questionnaireModelId, ddi.label(), new ArrayList<>(), jsonLunatic.jsonContent());

        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/questionnaire-models")
                .build()
                .toUri();

        webClient.post().uri(uri)
                .body(BodyInserters.fromValue(questionnaireModel))
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
                .path("/api/questionnaire/{id}")
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

    public void createCampaign(@NotNull String campaignId, @NotNull Questionnaire questionnaire, Ddi ddi) {
        QuestionnaireMetadataDto metadata = QuestionnaireMetadataDto.createDefaultQuestionnaireMetadata(questionnaire, metadataProps.getMetadata());
        CampaignDto campaign = new CampaignDto(campaignId, ddi.label(), metadata);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaigns")
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

    public void createSurveyUnits(@NotNull String questionnaireModelId, @NotNull List<SurveyUnit> surveyUnits) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}/survey-unit")
                .build(questionnaireModelId);

        List<SurveyUnitDto> surveyUnitsDto = surveyUnits.stream().map(SurveyUnitDto::fromModel).toList();
        surveyUnitsDto.forEach(surveyUnit ->
                webClient.post().uri(uri)
                        .body(BodyInserters.fromValue(surveyUnit))
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                        messageService.getMessage("queen.error.campaign.su.create", surveyUnit.id(), questionnaireModelId)))
                        )
                        .toBodilessEntity()
                        .block());
    }

    public List<SurveyUnit> getSurveyUnits(@NotNull String campaignId) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/campaign/{id}/survey-units")
                .build(campaignId);

        return webClient.get().uri(uri)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> Mono.error(new SurveyUnitsNotFoundException(messageService.getMessage("queen.error.campaign.su.not-found", campaignId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.su", campaignId)))
                )
                .bodyToMono(new ParameterizedTypeReference<List<SurveyUnit>>() {
                })
                .blockOptional()
                .orElseThrow(() -> new SurveyUnitsNotFoundException(messageService.getMessage("queen.error.campaign.su.not-found", campaignId)));
    }

    public void updateSurveyUnit(@NotNull SurveyUnit surveyUnit) {
        SurveyUnitUpdateDto surveyUnitDto = SurveyUnitUpdateDto.fromModel(surveyUnit);
        URI uri = UriComponentsBuilder
                .fromHttpUrl(queenUrl)
                .path("/api/survey-unit/{id}")
                .build(surveyUnit.id());

        webClient.put().uri(uri)
                .body(BodyInserters.fromValue(surveyUnitDto))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()),
                                messageService.getMessage("queen.error.campaign.su.update", surveyUnit.id(), surveyUnit.questionnaireId())))
                )
                .toBodilessEntity()
                .block();
    }
}
