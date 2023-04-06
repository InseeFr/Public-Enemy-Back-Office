package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import java.io.Serializable;
import java.util.List;

/**
 * Result from the validation of a survey unit
 *
 * @param surveyUnitId         survey unit id that has been validated
 * @param attributesValidation validation result list from attributes of this survey unit
 */
public record SurveyUnitDataValidationResult(String surveyUnitId,
                                             List<SurveyUnitDataAttributeValidationResult> attributesValidation) implements Serializable {
}
