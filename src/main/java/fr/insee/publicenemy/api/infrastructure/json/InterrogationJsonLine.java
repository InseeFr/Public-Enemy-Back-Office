package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InterrogationJsonLine {

    private JsonNode fields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterrogationJsonLine that = (InterrogationJsonLine) o;
        return Objects.equals(fields.toString(), that.fields.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "InterrogationCsvModel{" +
                "fields=" + fields +
                '}';
    }
}
