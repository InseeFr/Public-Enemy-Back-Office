package fr.insee.publicenemy.api.application.domain.model.interrogation;

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
public class InterrogationDataAttributeValueListList<T> implements IInterrogationDataAttributeValue {
    private final List<List<T>> values;

    public InterrogationDataAttributeValueListList() {
        this.values = new ArrayList<>();
    }


    @Override
    public DataTypeValidationResult validate(VariableType variableType) {
        return DataTypeValidationResult.createOkDataTypeValidation();

    }

    /**
     * Add value to the values set
     *
     * @param value value to add
     */
    public void addValue(List<T> value) {
        values.add(value);
    }

    public List<List<T>> getValue() {
        return values;
    }
}
