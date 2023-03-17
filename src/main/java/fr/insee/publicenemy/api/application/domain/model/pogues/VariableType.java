package fr.insee.publicenemy.api.application.domain.model.pogues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VariableType(
        VariableTypeEnum type,
        String name,
        String scope,
        @JsonProperty("datatype")
        IDataType dataType) {

        /**
         * The scope is indicating if the current variable is an array
         * @return true if variable has multiple values, false otherwise
         */
        public boolean hasMultipleValues() {
                return this.scope != null && !this.scope.isEmpty();
        }
}
