package com.periferia.social.auth.infrastructure.adapter.out.security;

import com.periferia.social.auth.domain.model.AccessToken;
import com.periferia.social.auth.domain.model.User;
import com.periferia.social.auth.domain.port.out.AccessTokenIssuer;
import com.periferia.social.platform.security.JwtProperties;
import com.periferia.social.shared.security.JwtClaimNames;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Emite JWT firmados con HS256. Secreto, issuer y vigencia vienen de variables de entorno. */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

    static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final Clock clock;

    JwtAccessTokenIssuer(JwtEncoder jwtEncoder, JwtProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public AccessToken issueFor(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.expiration());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(JwtClaimNames.USERNAME, user.username())
                .claim(JwtClaimNames.DISPLAY_NAME, user.displayName())
                .claim(JwtClaimNames.ROLES, List.of("USER"))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(value, TOKEN_TYPE, expiresAt);
    }
}
