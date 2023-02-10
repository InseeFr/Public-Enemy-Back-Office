package fr.insee.publicenemy.api.infrastructure.ddi;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.DdiServicePort;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.DdiNotFoundException;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.PoguesJsonNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DdiPoguesServiceImpl implements DdiServicePort {

    private final WebClient webClient;
    private final String poguesUrl;

    /**
     * Constructor
     * @param webClient webclient
     * @param poguesUrl pogues url
     */
    public DdiPoguesServiceImpl(WebClient webClient, @Value("${application.pogues.url}") String poguesUrl) {
        this.webClient = webClient; 
        this.poguesUrl = poguesUrl;
    }

    @Override
    public Ddi getDdi(@NonNull String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        PoguesDataSummary summary = getPoguesSummary(jsonPogues);
        byte[] xmlDdi = getXmlDdi(poguesId, jsonPogues);
        return new Ddi(poguesId, summary.label(), summary.modes(), xmlDdi);
    }

    @Override
    public Questionnaire getQuestionnaire(@NonNull String poguesId) {
        PoguesDataSummary summary = getPoguesSummary(poguesId);
        return new Questionnaire(poguesId, summary.label(), summary.modes());
    }

    /**
     * Retrieve summary details from JSON Pogues
     * @param poguesId questionnaire pogues Id
     * @return questionnaire summary from pogues
     */
    private PoguesDataSummary getPoguesSummary(@NonNull String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        return getPoguesSummary(jsonPogues);
    }

    /**
     * Retrieve summary details from JSON Pogues
     * @param jsonPogues json from pogues
     * @return questionnaire summary from pogues
     */
    private PoguesDataSummary getPoguesSummary(@NonNull JsonNode jsonPogues) {
        List<Mode> modes = new ArrayList<>();
        String label = "";
        jsonPogues.get("TargetMode").forEach(node -> modes.add(Mode.valueOf(node.asText())));
        JsonNode labelNode = jsonPogues.get("Label");
        if(!labelNode.isEmpty()) {
            label = labelNode.get(0).asText();
        }
        return new PoguesDataSummary(label, modes);
    }

    /**
     * Get Json Pogues
     * @param questionnaireId pogues questionnaire Id
     * @return the json from pogues
     */
    private JsonNode getJsonPogues(@NonNull String questionnaireId) {
        return webClient.get().uri(poguesUrl + "/api/persistence/questionnaire/{id}", questionnaireId)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new PoguesJsonNotFoundException(questionnaireId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(response.statusCode().value(), errorMessage)))
                )
                .bodyToMono(JsonNode.class)
                .blockOptional().orElseThrow(() -> new PoguesJsonNotFoundException(questionnaireId));
    }
    
    /**
     *  Get DDI 
     * @param jsonPogues Json from Pogues
     * @return the DDI
     */
    private byte[] getXmlDdi(@NonNull String poguesId, @NonNull JsonNode jsonPogues) {
        return webClient.post().uri(poguesUrl + "/api/transform/visualize-ddi")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromValue(jsonPogues))
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new DdiNotFoundException(poguesId)))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(response.statusCode().value(), errorMessage)))
                )
                .bodyToMono(byte[].class).blockOptional().orElseThrow(() -> new DdiNotFoundException(poguesId));
    }
}
