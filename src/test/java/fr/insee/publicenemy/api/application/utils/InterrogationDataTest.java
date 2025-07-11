package fr.insee.publicenemy.api.application.utils;

import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterrogationDataTest {

    @Test
    void testDetectJsonObject() {
        byte[] json = "{\"name\":\"Alice\"}".getBytes();
        InterrogationData.FormatType format = InterrogationData.getDataFormat(json);
        assertEquals(InterrogationData.FormatType.JSON, format);
    }

    @Test
    void testDetectJsonArray() {
        byte[] json = "[{\"name\":\"Alice\"}, {\"name\":\"Bob\"}]".getBytes();
        InterrogationData.FormatType format = InterrogationData.getDataFormat(json);
        assertEquals(InterrogationData.FormatType.JSON, format);
    }

    @Test
    void testDetectCsv() {
        byte[] csv = "name,age\nAlice,30\nBob,25".getBytes();
        InterrogationData.FormatType format = InterrogationData.getDataFormat(csv);
        assertEquals(InterrogationData.FormatType.CSV, format);
    }

    @Test
    void testEmptyDataThrowsException() {
        byte[] empty = new byte[0];
        assertThrows(IllegalArgumentException.class, () -> {
            InterrogationData.getDataFormat(empty);
        });
    }

    @Test
    void testNullDataThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            InterrogationData.getDataFormat(null);
        });
    }
}
