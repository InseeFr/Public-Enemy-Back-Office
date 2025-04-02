package fr.insee.publicenemy.api.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitIdentifierHandler;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SurveyUnitUseCase {

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

    public String getUrlOfSurveyUnit(SurveyUnit surveyUnit, String questionnaireModelId, Mode mode, JsonNode nomenclatures) {
        String surveyUnitId = surveyUnit.id();
        switch (mode){
            case CAWI -> {
                return String.format(cawiVisuSchema,
                        cawiOrchestratorUrl,
                        questionnaireModelId,
                        surveyUnitId);
            }
            case CAPI,CATI -> {
                String questionnaireUrl = String.format("%s/api/questionnaire/%s/data",
                        apiQuestionnaire,
                        questionnaireModelId);
                String dataUrl = String.format("%s/api/survey-unit/%s",
                        apiQuestionnaire,
                        surveyUnitId);
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

    public SurveyUnitRest buildSurveyUnitRest(SurveyUnit surveyUnit, String questionnaireModelId, Mode mode, JsonNode nomenclatures) throws UnsupportedEncodingException {
        String queenIdentifier = surveyUnit.id();
        // split the id to get rid of the questionnaire id part for frontend
        SurveyUnitIdentifierHandler identifierHandler = new SurveyUnitIdentifierHandler(queenIdentifier);
        return new SurveyUnitRest(
                queenIdentifier,
                identifierHandler.getSurveyUnitIdentifier(),
                getUrlOfSurveyUnit(surveyUnit, questionnaireModelId, mode, nomenclatures));
    }
}
