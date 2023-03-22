package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import fr.insee.publicenemy.api.application.domain.model.pogues.DataTypeValidation;

public record SurveyUnitAttributeValidation(String attributeName, DataTypeValidation dataTypeValidation) {
}
