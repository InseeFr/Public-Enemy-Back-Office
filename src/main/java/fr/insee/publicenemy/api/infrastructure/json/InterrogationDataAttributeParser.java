package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValueList;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValueListList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InterrogationDataAttributeParser {

    public static Map<String, IInterrogationDataAttributeValue<?>> parseCollectedAttributes(InterrogationJsonLine line) {
        return parseAttributesAtPath(line.getFields().path("data"), "COLLECTED", true);
    }

    public static Map<String, IInterrogationDataAttributeValue<?>> parseExternalAttributes(InterrogationJsonLine line) {
        return parseAttributesAtPath(line.getFields().path("data"), "EXTERNAL", false);
    }

    private static Map<String, IInterrogationDataAttributeValue<?>> parseAttributesAtPath(JsonNode root, String pathName, boolean expectCollectedSubNode) {
        Map<String, IInterrogationDataAttributeValue<?>> attributes = new HashMap<>();

        JsonNode targetNode = root.path(pathName);
        if (!targetNode.isObject()) {
            return attributes;
        }

        for (Iterator<String> it = targetNode.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            JsonNode valueNode = expectCollectedSubNode
                    ? targetNode.get(key).path("COLLECTED")
                    : targetNode.get(key);

            if (valueNode.isMissingNode()) continue;

            IInterrogationDataAttributeValue<?> parsedValue = parseJsonValue(valueNode);
            if (parsedValue != null) {
                attributes.put(key, parsedValue);
            }
        }

        return attributes;
    }

    private static IInterrogationDataAttributeValue<?> parseJsonValue(JsonNode valueNode) {
        if (valueNode.isArray()) {
            return parseArrayValue((ArrayNode) valueNode);
        } else if (valueNode.isTextual()) {
            return new InterrogationDataAttributeValue<>(valueNode.asText());
        } else if (valueNode.isNumber()) {
            return new InterrogationDataAttributeValue<>(valueNode.numberValue());
        } else if (valueNode.isBoolean()) {
            return new InterrogationDataAttributeValue<>(valueNode.asBoolean());
        } else if (valueNode.isNull()) {
            return new InterrogationDataAttributeValue<>(null);
        }

        return null; // unsupported type
    }

    private static IInterrogationDataAttributeValue<?> parseArrayValue(ArrayNode arrayNode) {
        if (arrayNode.isEmpty()) {
            return new InterrogationDataAttributeValueList<>();
        }

        // On déduit le type du premier élément non null
        for (JsonNode item : arrayNode) {
            if (!item.isNull()) {
                if (item.isTextual()) {
                    return createTypedList(arrayNode, String.class);
                } else if (item.isNumber()) {
                    return createTypedList(arrayNode, Number.class);
                } else if (item.isBoolean()) {
                    return createTypedList(arrayNode, Boolean.class);
                } else if(item.isArray()) {
                    return  parseArrayOfArray(arrayNode);
                }
            }
        }
        // Si tout est null
        return new InterrogationDataAttributeValueList<>();
    }

    private static IInterrogationDataAttributeValue<?> parseArrayOfArray(ArrayNode outerArray) {
        if (outerArray.isEmpty()) {
            return new InterrogationDataAttributeValueListList<>();
        }
        // Détection du type du premier élément non null dans la première sous-liste
        for (JsonNode subArray : outerArray) {
            if (subArray.isArray()) {
                for (JsonNode item : subArray) {
                    if (item.isTextual()) {
                        return createTypedListOfLists(outerArray, String.class);
                    } else if (item.isNumber()) {
                        return createTypedListOfLists(outerArray, Number.class);
                    } else if (item.isBoolean()) {
                        return createTypedListOfLists(outerArray, Boolean.class);
                    }
                }
            }
        }

        return new InterrogationDataAttributeValueListList<>();
    }

    private static <T> T castNodeValue(JsonNode node, Class<T> clazz) {
        if (node.isNull()) {
            return null;
        } else if (clazz == String.class && node.isTextual()) {
            return clazz.cast(node.asText());
        } else if (clazz == Number.class && node.isNumber()) {
            return clazz.cast(node.numberValue());
        } else if (clazz == Boolean.class && node.isBoolean()) {
            return clazz.cast(node.asBoolean());
        }
        return null;
    }

    private static <T> IInterrogationDataAttributeValue<List<T>> createTypedList(ArrayNode arrayNode, Class<T> clazz) {
        InterrogationDataAttributeValueList<T> listValue = new InterrogationDataAttributeValueList<>();
        for (JsonNode node : arrayNode) {
            listValue.addValue(castNodeValue(node, clazz));
        }
        return listValue;
    }

    private static <T> IInterrogationDataAttributeValue<List<List<T>>> createTypedListOfLists(ArrayNode outerArray, Class<T> clazz) {
        InterrogationDataAttributeValueListList<T> listOfLists = new InterrogationDataAttributeValueListList<>();

        for (JsonNode subArray : outerArray) {
            if (subArray.isArray()) {
                ArrayNode innerArray = (ArrayNode) subArray;
                InterrogationDataAttributeValueList<T> innerList = new InterrogationDataAttributeValueList<>();
                for (JsonNode item : innerArray) {
                    innerList.addValue(castNodeValue(item, clazz));
                }
                listOfLists.addValue(innerList.getValue());
            } else {
                listOfLists.addValue(null);
            }
        }

        return listOfLists;
    }
}
