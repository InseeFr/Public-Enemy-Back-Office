package fr.insee.publicenemy.api.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationIdentifierHandler;
import fr.insee.publicenemy.api.controllers.dto.InterrogationRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class InterrogationUseCase {

    @Value("${application.orchestrator.cawi.url}")
    private String cawiOrchestratorUrl;

    @Value("${application.orchestrator.cawi.visualize-schema}")
    private String cawiVisuSchema;

    @Value("${application.orchestrator.capi-cati.url}")
    private String capiCatiOrchestratorUrl;

    @Value("${application.orchestrator.capi-cati.visualize-schema}")
    private String capiCatiVisuSchema;

    @Value("${application.queen.public-url}")
    private String apiQuestionnaire;

    public String getUrlOfInterrogation(Interrogation interrogation, String questionnaireModelId, Mode mode, JsonNode nomenclatures) {
        String interrogationId = interrogation.id();
        switch (mode){
            case CAWI -> {
                return String.format(cawiVisuSchema,
                        cawiOrchestratorUrl,
                        questionnaireModelId,
                        interrogationId);
            }
            case CAPI,CATI -> {
                String questionnaireUrl = String.format("%s/api/questionnaire/%s/data",
                        apiQuestionnaire,
                        questionnaireModelId);
                String dataUrl = String.format("%s/api/interrogations/%s",
                        apiQuestionnaire,
                        interrogationId);
                return String.format(capiCatiVisuSchema,
                        capiCatiOrchestratorUrl,
                        URLEncoder.encode(questionnaireUrl, StandardCharsets.UTF_8),
                        URLEncoder.encode(dataUrl, StandardCharsets.UTF_8),
                        URLEncoder.encode(nomenclatures.toString(), StandardCharsets.UTF_8));
            }
            case null, default -> {
                return null;
            }
        }
    };

    public InterrogationRest buildInterrogationRest(Interrogation interrogation, String questionnaireModelId, Mode mode, JsonNode nomenclatures) throws UnsupportedEncodingException {
        String queenIdentifier = interrogation.id();
        // split the id to get rid of the questionnaire id part for frontend
        InterrogationIdentifierHandler identifierHandler = new InterrogationIdentifierHandler(queenIdentifier);
        return new InterrogationRest(
                queenIdentifier,
                identifierHandler.getInterrogationIdentifier(),
                getUrlOfInterrogation(interrogation, questionnaireModelId, mode, nomenclatures));
    }
}
