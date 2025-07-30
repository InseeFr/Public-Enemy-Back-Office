package fr.insee.publicenemy.api.controllers.dto;

public record InterrogationErrors(int dataIndex, String attributeKey, String message) {
}
