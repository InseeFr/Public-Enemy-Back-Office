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
     * questionnaire ID
     */
    private final Long questionnaireId;

    /**
     * questionnaire model identifier (ex: 11-CAPI)
     */
    private final String questionnaireModelId;
    /**
     * survey unit identifier (ex: 1)
     */
    private final int surveyUnitIdentifier;

    /**
     * queen identifier (ex: 11-CAPI-1)
     */
    private final String queenIdentifier;

    /**
     * @param questionnaireModelId questionnaire model id
     * @param surveyUnitIdentifier survey unit identifier
     */
    public SurveyUnitIdentifierHandler(@NonNull String questionnaireModelId, int surveyUnitIdentifier) {
        //check that questionnaireModelId match pattern of type (11-CAPI)
        if (!questionnaireModelId.matches("^\\d*-[^-]*$"))
            throw new IllegalArgumentException();

        this.questionnaireId = Long.parseLong(questionnaireModelId.substring(0, questionnaireModelId.indexOf('-')));
        this.questionnaireModelId = questionnaireModelId;
        this.surveyUnitIdentifier = surveyUnitIdentifier;
        this.queenIdentifier = String.format("%s-%d", questionnaireModelId, surveyUnitIdentifier);
    }

    /**
     * @param queenIdentifier queen identifier
     */
    public SurveyUnitIdentifierHandler(@NonNull String queenIdentifier) {
        //check that string match pattern of type (11-CAPI-1)
        if (!queenIdentifier.matches("^\\d*-[^-]*-\\d*$"))
            throw new IllegalArgumentException();

        this.queenIdentifier = queenIdentifier;
        this.questionnaireModelId = queenIdentifier.substring(0, queenIdentifier.lastIndexOf('-'));
        this.questionnaireId = Long.parseLong(questionnaireModelId.substring(0, questionnaireModelId.indexOf('-')));
        this.surveyUnitIdentifier = Integer.parseInt(queenIdentifier.substring(queenIdentifier.lastIndexOf('-') + 1));
    }
}
