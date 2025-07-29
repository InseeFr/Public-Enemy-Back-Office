package fr.insee.publicenemy.api.application.domain.utils;

import java.nio.charset.StandardCharsets;

public class InterrogationData {

    public enum FormatType {
        JSON,
        CSV
    }

    /**
     * Detects the format type (CSV or JSON) from a byte array.
     * @param data the file content as a byte array
     * @return the detected format type (CSV or JSON)
     */
    public static FormatType getDataFormat(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        // Convert byte array to string (assuming UTF-8)
        String content = new String(data, StandardCharsets.UTF_8).trim();

        // Simple detection logic for JSON
        if (content.startsWith("{") || content.startsWith("[")) {
            return FormatType.JSON;
        } else {
            return FormatType.CSV;
        }
    }
}
