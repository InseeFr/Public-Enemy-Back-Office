package fr.insee.publicenemy.api.application.domain.model.pogues;

import java.util.Arrays;
import java.util.Objects;

public record DataTypeValidationMessage(String code, String[] arguments) {
    public static DataTypeValidationMessage createMessage(String code) {
        return new DataTypeValidationMessage(code, null);
    }

    public static DataTypeValidationMessage createMessage(String code, String... arguments) {
        return new DataTypeValidationMessage(code, arguments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataTypeValidationMessage that = (DataTypeValidationMessage) o;
        return code.equals(that.code) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        return "DataTypeValidationMessage{" +
                "code='" + code + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
