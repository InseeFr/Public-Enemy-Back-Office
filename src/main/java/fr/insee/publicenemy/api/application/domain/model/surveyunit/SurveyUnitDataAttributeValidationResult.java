package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;

/**
 * Result from the validation of an attribute
 *
 * @param attributeName            attribute name that has been validated
 * @param dataTypeValidationResult result of the validation
 */
public record SurveyUnitDataAttributeValidationResult(String attributeName,
                                                      DataTypeValidationResult dataTypeValidationResult) {
}
