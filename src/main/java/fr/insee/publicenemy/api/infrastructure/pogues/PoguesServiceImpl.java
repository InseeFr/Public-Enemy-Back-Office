package fr.insee.publicenemy.api.infrastructure.pogues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.PoguesServicePort;
import fr.insee.publicenemy.api.infrastructure.pogues.exceptions.PoguesJsonNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PoguesServiceImpl implements PoguesServicePort {

    private final WebClient webClient;
    private final String poguesUrl;

    private final I18nMessagePort messageService;

    private static final String QUESTIONNAIRE_NOT_FOUND_ERROR = "questionnaire.notfound";

    /**
     * Constructor
     *
     * @param webClient webclient
     * @param poguesUrl pogues url
     */
    public PoguesServiceImpl(WebClient webClient, @Value("${application.pogues.url}") String poguesUrl, I18nMessagePort messagePort) {
        this.webClient = webClient;
        this.poguesUrl = poguesUrl;
        this.messageService = messagePort;
    }

    @Override
    public QuestionnaireModel getQuestionnaireModel(@NonNull String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        PoguesDataSummary summary = getPoguesSummary(jsonPogues);
        return new QuestionnaireModel(poguesId, summary.label(), summary.modes(), jsonPogues);
    }

    @Override
    public Questionnaire getQuestionnaire(@NonNull String poguesId) {
        PoguesDataSummary summary = getPoguesSummary(poguesId);
        return new Questionnaire(poguesId, summary.label(), summary.modes());
    }

    @Override
    public JsonNode getNomenclaturesByQuestionnaire(String poguesId) {
        return getNomeclatureOfQuestionnaire(poguesId);
    }

    /**
     * Retrieve summary details from JSON Pogues
     *
     * @param poguesId questionnaire pogues Id
     * @return questionnaire summary from pogues
     */
    private PoguesDataSummary getPoguesSummary(@NonNull String poguesId) {
        JsonNode jsonPogues = getJsonPogues(poguesId);
        return getPoguesSummary(jsonPogues);
    }

    /**
     * Retrieve summary details from JSON Pogues
     *
     * @param jsonPogues json from pogues
     * @return questionnaire summary from pogues
     */
    private PoguesDataSummary getPoguesSummary(@NonNull JsonNode jsonPogues) {
        List<Mode> modes = new ArrayList<>();
        String label = "";
        jsonPogues.get("TargetMode").forEach(node -> modes.add(Mode.valueOf(node.asText())));
        JsonNode labelNode = jsonPogues.get("Label");
        if (!labelNode.isEmpty()) {
            label = labelNode.get(0).asText();
        }
        return new PoguesDataSummary(label, modes);
    }

    /**
     * Get Json Pogues
     *
     * @param questionnaireId pogues questionnaire Id
     * @return the json from pogues
     */
    private JsonNode getJsonPogues(@NonNull String questionnaireId) {
        return webClient.get().uri(poguesUrl + "/api/persistence/questionnaire/{id}?references=true", questionnaireId)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR))))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), errorMessage)))
                )
                .bodyToMono(JsonNode.class)
                .blockOptional().orElseThrow(() -> new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR, questionnaireId)));
    }

    private JsonNode getNomeclatureOfQuestionnaire(@NonNull String questionnaireId) {
        return webClient.get().uri(poguesUrl + "/api/persistence/questionnaire/{id}/nomenclatures", questionnaireId)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR))))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), errorMessage)))
                )
                .bodyToMono(JsonNode.class)
                .blockOptional().orElseThrow(() -> new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR)));
    }

    @Override
    public List<VariableType> getQuestionnaireVariables(@NonNull String questionnaireId) {
        String variablesString = webClient.get().uri(poguesUrl + "/api/persistence/questionnaire/{id}/vars", questionnaireId)
                .retrieve()
                .onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR))))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorMessage -> Mono.error(new ServiceException(HttpStatus.valueOf(response.statusCode().value()), errorMessage)))
                )
                .bodyToMono(String.class)
                .blockOptional().orElseThrow(() -> new PoguesJsonNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_ERROR)));

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(variablesString, new TypeReference<List<VariableType>>() {
            });
        } catch (JsonProcessingException e) {
            log.error(String.format("Exception during variables deserialization of questionnaire id: %s", questionnaireId), e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error retrieving variables from questionnaire id %s", questionnaireId));
        }
    }
}
