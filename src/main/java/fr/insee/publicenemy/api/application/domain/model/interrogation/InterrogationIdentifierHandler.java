package fr.insee.publicenemy.api.application.domain.model.interrogation;

import lombok.Data;
import lombok.NonNull;

/**
 * Identifier handling for interrogations
 * This handler can generate identifier for queen or for a frontend
 */
@Data
public class InterrogationIdentifierHandler {

    /**
     * questionnaire ID
     */
    private final Long questionnaireId;

    /**
     * questionnaire model identifier (ex: 11-CAPI)
     */
    private final String questionnaireModelId;
    /**
     * interrogation identifier (ex: 1)
     */
    private final int interrogationIdentifier;

    /**
     * queen identifier (ex: 11-CAPI-1)
     */
    private final String queenIdentifier;

    /**
     *
     * @param questionnaireModelId questionnaire model id. A valid identifier looks like 11-CAPI, 1-CAWI, 25-CAPI
     * @param interrogationIdentifier interrogation identifier
     */
    public InterrogationIdentifierHandler(@NonNull String questionnaireModelId, int interrogationIdentifier) {
        //check that questionnaireModelId match pattern of type (11-CAPI)
        if (!questionnaireModelId.matches("^\\d*-[^-]*$"))
            throw new IllegalArgumentException("The questionnaire model ID has not a valid identifier");

        this.questionnaireId = Long.parseLong(questionnaireModelId.substring(0, questionnaireModelId.indexOf('-')));
        this.questionnaireModelId = questionnaireModelId;
        this.interrogationIdentifier = interrogationIdentifier;
        this.queenIdentifier = String.format("%s-%d", questionnaireModelId, interrogationIdentifier);
    }

    /**
     * @param queenIdentifier queen identifier. A valid identifier looks like 11-CAPI-1, 1-CAWI-33, 25-CAPI-10
     */
    public InterrogationIdentifierHandler(@NonNull String queenIdentifier) {
        //check that string match pattern of type (11-CAPI-1)
        if (!queenIdentifier.matches("^\\d*-[^-]*-\\d*$"))
            throw new IllegalArgumentException("Queen ID is not a valid identifier");

        this.queenIdentifier = queenIdentifier;
        this.questionnaireModelId = queenIdentifier.substring(0, queenIdentifier.lastIndexOf('-'));
        this.questionnaireId = Long.parseLong(questionnaireModelId.substring(0, questionnaireModelId.indexOf('-')));
        this.interrogationIdentifier = Integer.parseInt(queenIdentifier.substring(queenIdentifier.lastIndexOf('-') + 1));
    }
}
