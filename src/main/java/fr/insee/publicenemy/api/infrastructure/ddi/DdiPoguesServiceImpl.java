package fr.insee.publicenemy.api.infrastructure.ddi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.application.ports.DdiServicePort;

@Service
public class DdiPoguesServiceImpl implements DdiServicePort {

    private final WebClient webClient;
    private final String poguesUrl;

    /**
     * Constructor
     * @param webClient
     * @param poguesUrl
     */
    public DdiPoguesServiceImpl(WebClient webClient, @Value("${application.pogues.url}") String poguesUrl) {
        this.webClient = webClient; 
        this.poguesUrl = poguesUrl;
    }

    @Override
    public Ddi getDdi(String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        PoguesDataSummary summary = getPoguesSummary(jsonPogues);
        byte[] xmlDdi = getXmlDdi(jsonPogues);
        return new Ddi(poguesId, summary.getLabel(), summary.getModes(), xmlDdi);
    }

    @Override
    public Questionnaire getQuestionnaire(String poguesId) {
        PoguesDataSummary summary = getPoguesSummary(poguesId);
        return new Questionnaire(poguesId, summary.getLabel(), summary.getModes());
    }

    /**
     * Retrieve summary details from JSON Pogues
     * @param poguesId
     * @return
     */
    private PoguesDataSummary getPoguesSummary(String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        return getPoguesSummary(jsonPogues);
    }

    /**
     * Retrieve summary details from JSON Pogues
     * @param poguesId
     * @param jsonPogues
     * @return
     */
    private PoguesDataSummary getPoguesSummary(JsonNode jsonPogues) {
        List<Mode> modes = new ArrayList<>();
        jsonPogues.get("TargetMode").forEach(node -> modes.add(Mode.valueOf(node.asText())));
        String label = jsonPogues.get("Label").get(0).asText();
        return new PoguesDataSummary(label, modes);
    }

    /**
     * Get Json Pogues
     * @param questionnaireId
     * @return the json from pogues
     */
    private JsonNode getJsonPogues(String questionnaireId) {
        JsonNode jsonPogues = webClient.get().uri(poguesUrl + "/api/persistence/questionnaire/{id}", questionnaireId)
                .retrieve()
                .bodyToMono(JsonNode.class).block();
        
        if(jsonPogues == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while getting JSON Pogues");
        }

        return jsonPogues;
    }
    
    /**
     *  Get DDI 
     * @param jsonPogues
     * @return the DDI
     */
    private byte[] getXmlDdi(JsonNode jsonPogues) {
        return webClient.post().uri(poguesUrl + "/api/transform/visualize-ddi")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(BodyInserters.fromValue(jsonPogues))
                .retrieve()
                .bodyToMono(byte[].class).block();
    }
}
