package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValueList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterrogationDataAttributeParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InterrogationJsonLine createInterrogationJsonLine(String jsonContent) throws Exception {
        JsonNode node = objectMapper.readTree(jsonContent);
        return new InterrogationJsonLine(node);
    }

    @Test
    void testParseCollectedAttributes_withMixedTypes() throws Exception {
        String json = """
        {
          "data": {
            "COLLECTED": {
              "NAME": { "COLLECTED": "Alice" },
              "AGE": { "COLLECTED": 30 },
              "IS_STUDENT": { "COLLECTED": true },
              "NOTHING": { "COLLECTED": null },
              "TAGS": { "COLLECTED": ["a", "b", "c"] },
              "NUMBERS": { "COLLECTED": [1, 2, 3] },
              "FLAGS": { "COLLECTED": [true, false, true] },
              "ALL_NULLS": { "COLLECTED": [null, null] },
              "EMPTY_ARRAY": { "COLLECTED": [] }
            }
          }
        }
        """;

        InterrogationJsonLine line = createInterrogationJsonLine(json);
        Map<String, IInterrogationDataAttributeValue> result = InterrogationDataAttributeParser.parseCollectedAttributes(line);

        assertEquals("Alice", ((InterrogationDataAttributeValue<?>) result.get("NAME")).getValue());
        assertEquals(30, ((InterrogationDataAttributeValue<?>) result.get("AGE")).getValue());
        assertEquals(true, ((InterrogationDataAttributeValue<?>) result.get("IS_STUDENT")).getValue());
        assertNull(((InterrogationDataAttributeValue<?>) result.get("NOTHING")).getValue());

        // Vérifie les listes
        assertEquals(List.of("a", "b", "c"), ((InterrogationDataAttributeValueList<?>) result.get("TAGS")).getValue());
        assertEquals(List.of(1, 2, 3), ((InterrogationDataAttributeValueList<?>) result.get("NUMBERS")).getValue());
        assertEquals(List.of(true, false, true), ((InterrogationDataAttributeValueList<?>) result.get("FLAGS")).getValue());

        assertEquals(2,((List<?>) result.get("ALL_NULLS").getValue()).size());
        assertNull(((List<?>) result.get("ALL_NULLS").getValue()).get(0));
        assertNull(((List<?>) result.get("ALL_NULLS").getValue()).get(1));
        assertTrue(((InterrogationDataAttributeValueList<?>) result.get("EMPTY_ARRAY")).getValue().isEmpty());
    }

    @Test
    void testParseExternalAttributes_withSimpleValues() throws Exception {
        String json = """
        {
          "data": {
            "EXTERNAL": {
              "SOURCE": "INSEE",
              "VERSION": 3,
              "ENABLED": false,
              "NULLABLE": null,
              "FLAGS": [false, true]
            }
          }
        }
        """;

        InterrogationJsonLine line = createInterrogationJsonLine(json);
        Map<String, IInterrogationDataAttributeValue> result = InterrogationDataAttributeParser.parseExternalAttributes(line);

        assertEquals("INSEE", ((InterrogationDataAttributeValue<?>) result.get("SOURCE")).getValue());
        assertEquals(3, ((InterrogationDataAttributeValue<?>) result.get("VERSION")).getValue());
        assertEquals(false, ((InterrogationDataAttributeValue<?>) result.get("ENABLED")).getValue());
        assertNull(((InterrogationDataAttributeValue<?>) result.get("NULLABLE")).getValue());
        assertEquals(List.of(false, true), result.get("FLAGS").getValue());
    }

    @Test
    void testParseCollectedAttributes_withEmptyObject() throws Exception {
        String json = """
        {
          "data": {
            "COLLECTED": {}
          }
        }
        """;

        InterrogationJsonLine line = createInterrogationJsonLine(json);
        Map<String, IInterrogationDataAttributeValue> result = InterrogationDataAttributeParser.parseCollectedAttributes(line);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseExternalAttributes_whenExternalIsMissing() throws Exception {
        String json = """
        {
          "data": {}
        }
        """;

        InterrogationJsonLine line = createInterrogationJsonLine(json);
        Map<String, IInterrogationDataAttributeValue> result = InterrogationDataAttributeParser.parseExternalAttributes(line);
        assertTrue(result.isEmpty());
    }
}
