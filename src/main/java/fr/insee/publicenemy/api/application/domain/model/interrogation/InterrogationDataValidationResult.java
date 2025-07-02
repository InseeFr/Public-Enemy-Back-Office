package fr.insee.publicenemy.api.application.domain.model.interrogation;

import java.io.Serializable;
import java.util.List;

/**
 * Result from the validation of a Interrogation
 *
 * @param InterrogationId         Interrogation id that has been validated
 * @param attributesValidation validation result list from attributes of this Interrogation
 */
public record InterrogationDataValidationResult(String InterrogationId,
                                                List<InterrogationDataAttributeValidationResult> attributesValidation) implements Serializable {
}
