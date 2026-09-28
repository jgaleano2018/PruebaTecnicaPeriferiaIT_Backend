package com.periferia.social.platform.security;

import com.periferia.social.shared.security.JwtClaimNames;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/** Identidad del usuario autenticado extraída del JWT (el {@code sub} es el id del usuario). */
public record AuthenticatedUser(UUID id, String username, String displayName) {

    public static AuthenticatedUser from(Jwt jwt) {
        String username = jwt.getClaimAsString(JwtClaimNames.USERNAME);
        String displayName = jwt.getClaimAsString(JwtClaimNames.DISPLAY_NAME);
        return new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                username,
                displayName == null ? username : displayName);
    }
}
