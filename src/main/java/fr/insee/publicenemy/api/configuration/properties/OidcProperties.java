package fr.insee.publicenemy.api.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "feature.oidc")
public record OidcProperties(
        boolean enabled,
        String authServerHost,
        String authServerUrl,
        String realm,
        String principalAttribute,
        String roleClaim,
        String clientId) {
}