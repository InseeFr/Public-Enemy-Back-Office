package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;

import java.util.List;

public interface InterrogationJsonPort {
    /**
     * initialize interrogations from a csv data file
     *
     * @param interrogationData       interrogations data
     * @param questionnaireModelId questionnaire model id
     * @return interrogations model from questionnaire csv interrogations
     */
    List<Interrogation> initInterrogations(byte[] interrogationData, String questionnaireModelId);

    /**
     * get interrogation from a csv data file
     *
     * @param interrogationId         interrogation id
     * @param interrogationData       interrogation json data
     * @param questionnaireModelId questionnaire model id used to generate queen identifier
     * @return interrogations model from questionnaire csv interrogations
     */
    Interrogation getJsonInterrogation(int interrogationId, byte[] interrogationData, String questionnaireModelId);


}
