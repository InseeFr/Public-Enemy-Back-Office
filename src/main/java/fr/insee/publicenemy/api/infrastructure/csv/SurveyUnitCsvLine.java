package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.processor.ConvertEmptyOrBlankStringsToNull;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MultiValuedMap;

import java.util.Objects;

@Getter
@Setter
public class SurveyUnitCsvLine {
    @CsvBindByName(column = "IdUe")
    private String idUnit;

    @CsvBindAndJoinByName(column = ".*", elementType = String.class)
    @PreAssignmentProcessor(processor = ConvertEmptyOrBlankStringsToNull.class)
    private MultiValuedMap<String, String> fields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SurveyUnitCsvLine that = (SurveyUnitCsvLine) o;
        return Objects.equals(idUnit, that.idUnit) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUnit, fields);
    }

    @Override
    public String toString() {
        return "SurveyUnitCsvModel{" +
                "idUnit='" + idUnit + '\'' +
                ", fields=" + fields +
                '}';
    }
}
