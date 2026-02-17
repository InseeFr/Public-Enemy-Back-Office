package fr.insee.publicenemy.api.infrastructure.json;

import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InterrogationStateDataAttributeParserTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testParseStateData_validJson() throws Exception {
        String json = "{ \"stateData\": { \"currentPage\": \"3\", \"date\": 1721900000000, \"state\": \"COMPLETED\" } }";
        JsonNode fields = mapper.readTree(json);

        InterrogationJsonLine line = mock(InterrogationJsonLine.class);
        when(line.getFields()).thenReturn(fields);

        InterrogationStateData result = InterrogationStateDataAttributeParser.parseStateData(line);

        assertEquals("3", result.currentPage());
        assertEquals(1721900000000L, result.date());
        assertEquals("COMPLETED", result.state());
    }

    @Test
    void testParseStateData_missingFields_shouldUseDefaults() throws Exception {
        String json = "{ \"stateData\": { } }";
        JsonNode fields = mapper.readTree(json);

        InterrogationJsonLine line = mock(InterrogationJsonLine.class);
        when(line.getFields()).thenReturn(fields);

        InterrogationStateData result = InterrogationStateDataAttributeParser.parseStateData(line);

        assertEquals("1", result.currentPage());
        assertEquals("INIT", result.state());
        assertTrue(result.date() <= Instant.now().toEpochMilli());
    }

    @Test
    void testParseStateData_nullStateData_shouldReturnInitialState() throws Exception {
        String json = "{ }"; // no stateData
        JsonNode fields = mapper.readTree(json);

        InterrogationJsonLine line = mock(InterrogationJsonLine.class);
        when(line.getFields()).thenReturn(fields);

        InterrogationStateData result = InterrogationStateDataAttributeParser.parseStateData(line);

        assertNull(result);
    }

    @Test
    void testParseStateData_wrongFieldTypes_shouldUseDefaults() throws Exception {
        String json = "{ \"stateData\": { \"currentPage\": 999, \"date\": \"not-a-timestamp\", \"state\": null } }";
        JsonNode fields = mapper.readTree(json);

        InterrogationJsonLine line = mock(InterrogationJsonLine.class);
        when(line.getFields()).thenReturn(fields);

        InterrogationStateData result = InterrogationStateDataAttributeParser.parseStateData(line);

        assertEquals("1", result.currentPage()); // fallback
        assertEquals("INIT", result.state());    // fallback
        assertTrue(result.date() <= Instant.now().toEpochMilli()); // fallback
    }
}
