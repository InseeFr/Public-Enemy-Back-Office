package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Data
@NoArgsConstructor
public class SurveyUnitStringData implements ISurveyUnitObjectData<String> {
    private String value;

    public SurveyUnitStringData(String value) {
        this.value = value;
    }

    @Override
    public DataTypeValidation validate(VariableType variableType) {
        return variableType.dataType().validate(value);
    }
}
