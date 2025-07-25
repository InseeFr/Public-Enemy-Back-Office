package fr.insee.publicenemy.api.application.domain.model;

public record PersonalizationMapping(String interrogationId, Long questionnaireId, Mode mode, int dataIndex) {
}
