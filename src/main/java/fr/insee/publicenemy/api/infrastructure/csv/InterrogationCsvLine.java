package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.processor.ConvertEmptyOrBlankStringsToNull;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.Objects;

@Getter
@Setter
public class InterrogationCsvLine {

    @CsvBindAndJoinByName(column = ".*", elementType = String.class)
    @PreAssignmentProcessor(processor = ConvertEmptyOrBlankStringsToNull.class)
    private MultiValuedMap<String, String> fields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InterrogationCsvLine that = (InterrogationCsvLine) o;
        return Objects.equals(fields, that.fields);
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
