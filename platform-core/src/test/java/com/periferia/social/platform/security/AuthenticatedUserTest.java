package com.periferia.social.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.periferia.social.shared.security.JwtClaimNames;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthenticatedUserTest {

    @Test
    void extractsIdentityFromJwtClaims() {
        UUID id = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256")
                .subject(id.toString())
                .claim(JwtClaimNames.USERNAME, "alice")
                .claim(JwtClaimNames.DISPLAY_NAME, "Alice")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertThat(AuthenticatedUser.from(jwt)).isEqualTo(new AuthenticatedUser(id, "alice", "Alice"));
    }

    @Test
    void fallsBackToUsernameWhenDisplayNameIsMissing() {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .claim(JwtClaimNames.USERNAME, "bob")
                .build();

        assertThat(AuthenticatedUser.from(jwt).displayName()).isEqualTo("bob");
    }
}
