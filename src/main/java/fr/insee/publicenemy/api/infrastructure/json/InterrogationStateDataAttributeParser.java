package fr.insee.publicenemy.api.infrastructure.json;

import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import tools.jackson.databind.JsonNode;

import static fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData.*;


public class InterrogationStateDataAttributeParser {

    private InterrogationStateDataAttributeParser() {
        throw new IllegalStateException("Utility class");
    }

    public static InterrogationStateData parseStateData(InterrogationJsonLine line) {
        JsonNode stateData = line.getFields().path("stateData");
        if(stateData == null || stateData.isNull() || !stateData.isObject()) return InterrogationStateData.createInitialStateData();
        String currentPage = safeGetText(stateData, "currentPage", DEFAULT_PAGE);
        long date = safeGetLong(stateData, "date", getDefaultDate());
        String state = safeGetText(stateData, "state", DEFAULT_STATE);

        return new InterrogationStateData(currentPage, date, state);
    }
    private static String safeGetText(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.isString()) ? field.asString() : defaultValue;
    }
    private static long safeGetLong(JsonNode node, String fieldName, long defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull() && field.canConvertToLong()) ? field.asLong() : defaultValue;
    }

}
