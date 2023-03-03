package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VariableType(
        VariableTypeEnum type,
        String name,
        @JsonProperty("datatype")
        DataType dataType) {

}
