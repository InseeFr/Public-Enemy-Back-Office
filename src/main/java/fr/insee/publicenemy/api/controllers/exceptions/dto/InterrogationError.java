package fr.insee.publicenemy.api.controllers.exceptions.dto;

public record InterrogationError(int dataIndex, String attributeKey, String message) {
}
