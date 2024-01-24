package fr.insee.publicenemy.api.configuration.swagger.role;

import fr.insee.publicenemy.api.configuration.auth.AuthorityRole;

public enum RoleUIMapper {
    ADMIN(AuthorityRole.HAS_ADMIN_PRIVILEGES),
    AUTHENTICATED(AuthorityRole.HAS_ANY_ROLE),
    INTERVIEWER(AuthorityRole.HAS_ROLE_DESIGNER);

    private final String roleExpression;

    RoleUIMapper(String roleExpression) {
        this.roleExpression = roleExpression;
    }

    public String getRoleExpression() {
        return roleExpression;
    }
}