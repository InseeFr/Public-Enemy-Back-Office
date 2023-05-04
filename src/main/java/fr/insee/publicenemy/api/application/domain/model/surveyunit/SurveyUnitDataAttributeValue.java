package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Object containing a survey unit attribute value as a string and its validation object
 * Used to validate the attribute value against its corresponding variable type (variable type coming from a questionnaire model)
 */
@EqualsAndHashCode
@ToString
@Data
@NoArgsConstructor
public class SurveyUnitDataAttributeValue implements ISurveyUnitDataAttributeValue<String> {
    private String value;

    public SurveyUnitDataAttributeValue(String value) {
        this.value = value;
    }

    @Override
    public DataTypeValidationResult validate(VariableType variableType) {
        return variableType.dataType().validate(value);
    }
}
