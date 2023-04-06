package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;

/**
 * Used to create a survey unit attribute value of generic type, and validate it against a variable type
 * The purpose is to have different kind of attribute values for a survey unit data and being able to validate them
 * in a generic way
 *
 * @param <T> type of object stored as attribute value
 */
public interface ISurveyUnitDataAttributeValue<T> {
    /**
     * @param variableType variable type used for validation
     * @return a data validation object indicating if validation is successful (or not)
     */
    DataTypeValidationResult validate(VariableType variableType);

    /**
     * @return attribute value
     */
    T getValue();
}
