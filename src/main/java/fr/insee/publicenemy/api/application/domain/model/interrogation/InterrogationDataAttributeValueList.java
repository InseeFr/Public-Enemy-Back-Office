package fr.insee.publicenemy.api.application.domain.model.interrogation;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationMessage;
import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidationResult;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Object containing a survey unit attribute value as a list of string
 * Used to validate the attribute value against its corresponding variable type (variable type coming from a questionnaire model)
 */
@EqualsAndHashCode
@ToString
@Getter
public class InterrogationDataAttributeValueList implements IInterrogationDataAttributeValue<List<String>> {
    private final List<String> values;

    public InterrogationDataAttributeValueList() {
        this.values = new ArrayList<>();
    }


    @Override
    public DataTypeValidationResult validate(VariableType variableType) {
        if (values.isEmpty()) {
            return DataTypeValidationResult.createOkDataTypeValidation();
        }

        boolean isValid = true;
        List<DataTypeValidationMessage> errorMessages = new ArrayList<>();

        for (String value : values) {
            DataTypeValidationResult validationObject = variableType.dataType().validate(value);
            if (!validationObject.isValid()) {
                isValid = false;
            }
            errorMessages.addAll(validationObject.errorMessages());
        }

        return new DataTypeValidationResult(isValid, errorMessages);
    }

    /**
     * Add value to the values set
     *
     * @param value value to add
     */
    public void addValue(String value) {
        values.add(value);
    }

    public List<String> getValue() {
        return values;
    }
}
