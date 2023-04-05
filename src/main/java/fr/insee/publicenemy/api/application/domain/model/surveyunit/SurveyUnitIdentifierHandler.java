package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import lombok.Data;
import lombok.NonNull;

@Data
/**
 * Identifier handling for survey units
 * This handler can generate identifier for queen or for a frontend
 */
public class SurveyUnitIdentifierHandler {
    /**
     * questionnaire model identifier (ex: 11-CAPI)
     */
    private final String questionnaireModelId;
    /**
     * survey unit identifier (ex: 1)
     */
    private final String surveyUnitIdentifier;

    /**
     * queen identifier (ex: 11-CAPI-1)
     */
    private final String queenIdentifier;

    /**
     * @param questionnaireModelId questionnaire model id
     * @param surveyUnitIdentifier survey unit identifier
     */
    public SurveyUnitIdentifierHandler(@NonNull String questionnaireModelId, int surveyUnitIdentifier) {
        this.questionnaireModelId = questionnaireModelId;
        this.surveyUnitIdentifier = String.format("%s", surveyUnitIdentifier);
        this.queenIdentifier = String.format("%s-%s", questionnaireModelId, surveyUnitIdentifier);
    }

    /**
     * @param queenIdentifier queen identifier
     */
    public SurveyUnitIdentifierHandler(@NonNull String queenIdentifier) {
        //check that string match pattern of type (11-CAPI-1)
        if (!queenIdentifier.matches("^[^-]*-[^-]*-\\d*$"))
            throw new IllegalArgumentException();

        this.queenIdentifier = queenIdentifier;
        this.questionnaireModelId = queenIdentifier.substring(0, queenIdentifier.lastIndexOf('-'));
        this.surveyUnitIdentifier = queenIdentifier.substring(queenIdentifier.lastIndexOf('-') + 1);
    }
}
