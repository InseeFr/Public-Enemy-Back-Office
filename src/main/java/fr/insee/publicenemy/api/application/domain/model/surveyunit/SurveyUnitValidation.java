package fr.insee.publicenemy.api.application.domain.model.surveyunit;

import java.io.Serializable;
import java.util.List;

public record SurveyUnitValidation(String surveyUnitId, List<SurveyUnitAttributeValidation> attributesValidation) implements Serializable {
}
