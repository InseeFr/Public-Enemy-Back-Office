package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
@ToString
public class SurveyUnitListData implements ISurveyUnitObjectData {
    private final Set<String> values;

    public SurveyUnitListData() {
        this.values = new HashSet<>();
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
}
