package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class SurveyUnitStringData implements ISurveyUnitObjectData {
    private final String value;

    public SurveyUnitStringData(String value) {
        this.value = value;
    }

    @Override
    public DataTypeValidation validate(VariableType variableType) {
        return variableType.dataType().validate(value);
    }
}
