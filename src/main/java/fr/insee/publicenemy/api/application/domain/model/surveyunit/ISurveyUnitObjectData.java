package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;

public interface ISurveyUnitObjectData {
    /**
     * @param variableType variable type used for validation
     * @return a data validation object indicating if validation is successful (or not)
     */
    DataTypeValidation validate(VariableType variableType);
}
