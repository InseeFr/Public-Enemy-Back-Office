package fr.insee.publicenemy.api.utils;

import fr.insee.publicenemy.api.configuration.auth.AuthConstants;
import fr.insee.publicenemy.api.configuration.auth.AuthorityRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticatedUserTestHelper {

    public JwtAuthenticationToken getUser() {
        return getAuthenticatedUser(
                AuthorityRoleEnum.ADMIN, AuthorityRoleEnum.DESIGNER);
    }
    public JwtAuthenticationToken getAuthenticatedUser(AuthorityRoleEnum... roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (AuthorityRoleEnum role : roles) {
            authorities.add(new SimpleGrantedAuthority(AuthConstants.ROLE_PREFIX + role.name()));
        }

        Map<String, Object> headers = Map.of("typ", "JWT");
        Map<String, Object> claims = Map.of("preferred_username", "dupont-identifier", "name", "Jean Dupont");

        Jwt jwt = new Jwt("token-value", Instant.MIN, Instant.MAX, headers, claims);
        return new JwtAuthenticationToken(jwt, authorities, "dupont-identifier");
    }
}