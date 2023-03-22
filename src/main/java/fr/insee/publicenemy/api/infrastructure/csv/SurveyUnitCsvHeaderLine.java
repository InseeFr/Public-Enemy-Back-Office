package fr.insee.publicenemy.api.infrastructure.csv;

import java.util.Set;

/**
 * Attributes used for a survey unit
 * @param headers
 */
public record SurveyUnitCsvHeaderLine(Set<String> headers) {
}
