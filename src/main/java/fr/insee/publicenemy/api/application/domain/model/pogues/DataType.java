package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Objects;
@JsonTypeInfo(use = Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @Type(value = DateDatatypeType.class),
        @Type(value = BooleanDatatypeType.class),
        @Type(value = DurationDatatypeType.class),
        @Type(value = NumericDatatypeType.class),
        @Type(value = TextDatatypeType.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DataType {

    private String type;
    private String typeName;

    public DataType(String type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    abstract DataTypeValidation validate(String fieldValue);

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataType dataType = (DataType) o;
        return Objects.equals(type, dataType.type) && Objects.equals(typeName, dataType.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, typeName);
    }

    @Override
    public String toString() {
        return "DataType{" +
                "type='" + type + '\'' +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}