package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.pogues.NomenclatureUrl;
import fr.insee.publicenemy.api.controllers.dto.InterrogationRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    public String buildLunaticUri(String questionnaireModelId){
        return String.format("%s/api/questionnaire/%s/data",
                apiQuestionnaire,
                questionnaireModelId);
    }

    public String getUrlOfInterrogation(PersonalizationMapping personalizationMapping, Mode mode, List<NomenclatureUrl> nomenclatureUrls) {
        String interrogationId = personalizationMapping.interrogationId();
        String questionnaireModelId = personalizationMapping.getQuestionnaireModelId();
        switch (mode){
            case CAWI -> {
                return String.format(cawiVisuSchema,
                        cawiOrchestratorUrl,
                        interrogationId);
            }
            case CAPI,CATI -> {
                String questionnaireUrl = buildLunaticUri(questionnaireModelId);
                String dataUrl = String.format("%s/api/interrogations/%s",
                        apiQuestionnaire,
                        interrogationId);
                    String nomenclaturesJson = OBJECT_MAPPER.writeValueAsString(nomenclatureUrls);
                    return String.format(capiCatiVisuSchema,
                            capiCatiOrchestratorUrl,
                            URLEncoder.encode(questionnaireUrl, StandardCharsets.UTF_8),
                            URLEncoder.encode(dataUrl, StandardCharsets.UTF_8),
                            URLEncoder.encode(nomenclaturesJson, StandardCharsets.UTF_8));
            }
            case null, default -> {
                return null;
            }
        }
    }

    public InterrogationRest buildInterrogationRest(PersonalizationMapping personalizationMapping, Mode mode, List<NomenclatureUrl> nomenclatureUrls) {
        return new InterrogationRest(
                personalizationMapping.interrogationId(),
                personalizationMapping.dataIndex() + 1,
                getUrlOfInterrogation(personalizationMapping, mode, nomenclatureUrls));
    }
}
