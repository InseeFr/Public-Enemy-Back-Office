package fr.insee.publicenemy.api.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.roles")
public record RoleProperties(
        String designer,
        String admin
) {
}