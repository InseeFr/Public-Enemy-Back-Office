package fr.insee.publicenemy.api.infrastructure.csv;

import java.util.Set;

/**
 * Line headers for Survey Units CSV File
 * @param headers
 */
public record SurveyUnitCsvHeaderLine(Set<String> headers) {
}
