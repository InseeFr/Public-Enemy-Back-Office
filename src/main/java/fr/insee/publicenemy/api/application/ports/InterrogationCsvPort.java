package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.infrastructure.csv.InterrogationCsvHeaderLine;

import java.util.List;

public interface InterrogationCsvPort {
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
     * @param interrogationData       interrogation csv data
     * @param questionnaireModelId questionnaire model id used to generate queen identifier
     * @return interrogations model from questionnaire csv interrogations
     */
    Interrogation getCsvInterrogation(int interrogationId, byte[] interrogationData, String questionnaireModelId);

    /**
     * retrieve csv headers based on variable types from a questionnaire model
     *
     * @param variablesType list of variables types for a questionnaire
     * @return csv headers for interrogations from variables type
     */
    InterrogationCsvHeaderLine getInterrogationsCsvHeaders(List<VariableType> variablesType);
}
