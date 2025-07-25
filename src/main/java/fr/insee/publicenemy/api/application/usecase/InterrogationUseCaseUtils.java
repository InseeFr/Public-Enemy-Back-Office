package fr.insee.publicenemy.api.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.controllers.dto.InterrogationRest;
import fr.insee.publicenemy.api.infrastructure.queen.dto.InterrogationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class InterrogationUseCaseUtils {

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

    public String getUrlOfInterrogation(PersonalizationMapping personalizationMapping, Mode mode, JsonNode nomenclatures) {
        String interrogationId = personalizationMapping.interrogationId();
        String questionnaireModelId = personalizationMapping.getQuestionnaireModelId();
        switch (mode){
            case CAWI -> {
                return String.format(cawiVisuSchema,
                        cawiOrchestratorUrl,
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
    }

    public InterrogationRest buildInterrogationRest(PersonalizationMapping personalizationMapping, int orderId, Mode mode, JsonNode nomenclatures) {
        return new InterrogationRest(
                personalizationMapping.interrogationId(),
                personalizationMapping.dataIndex() + 1 ,
                getUrlOfInterrogation(personalizationMapping, mode, nomenclatures));
    }
}
