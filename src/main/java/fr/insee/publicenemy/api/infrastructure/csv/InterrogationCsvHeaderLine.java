package fr.insee.publicenemy.api.infrastructure.csv;

import java.util.Set;

/**
 * Attributes used for a survey unit
 * @param headers
 */
public record InterrogationCsvHeaderLine(Set<String> headers) {
}
