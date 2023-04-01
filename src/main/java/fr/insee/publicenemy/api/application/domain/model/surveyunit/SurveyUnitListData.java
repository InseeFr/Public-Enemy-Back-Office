package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@ToString
@Getter
public class SurveyUnitListData implements ISurveyUnitObjectData<List<String>> {
    private final List<String> values;

    public SurveyUnitListData() {
        this.values = new ArrayList<>();
    }


    @Override
    public DataTypeValidation validate(VariableType variableType) {
        if(values.isEmpty()) {
            return DataTypeValidation.createOkDataTypeValidation();
        }

        boolean isValid = true;
        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        for(String value : values) {
            DataTypeValidation validationObject = variableType.dataType().validate(value);
            if(!validationObject.isValid()) {
                isValid = false;
            }
            errorMessages.addAll(validationObject.errorMessages());
        }

        return new DataTypeValidation(isValid, errorMessages);
    }

    /**
     * Add value to the values set
     * @param value value to add
     */
    public void addValue(String value) {
        values.add(value);
    }

    public List<String> getValue() {
        return values;
    }
}
