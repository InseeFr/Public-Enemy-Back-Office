package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;

import java.time.Instant;


public class InterrogationStateDataAttributeParser {

    private InterrogationStateDataAttributeParser() {
        throw new IllegalStateException("Utility class");
    }

    public static InterrogationStateData parseStateData(InterrogationJsonLine line) {
        JsonNode stateData = line.getFields().path("stateData");
        if(stateData == null || stateData.isNull() || !stateData.isObject()) return InterrogationStateData.createInitialStateData();

        String defaultPage = "1";
        long defaultDate = Instant.now().toEpochMilli(); // current Timestamp
        String defaultState = "INIT";

        String currentPage = safeGetText(stateData, "currentPage", defaultPage);
        long date = safeGetLong(stateData, "date", defaultDate);
        String state = safeGetText(stateData, "state", defaultState);

        return new InterrogationStateData(currentPage, date, state);
    }
    private static String safeGetText(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.isTextual()) ? field.asText() : defaultValue;
    }
    private static long safeGetLong(JsonNode node, String fieldName, long defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.canConvertToLong()) ? field.asLong() : defaultValue;
    }

}
