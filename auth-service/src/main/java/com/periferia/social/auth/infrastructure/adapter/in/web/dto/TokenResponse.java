package com.periferia.social.auth.infrastructure.adapter.in.web.dto;

import com.periferia.social.auth.domain.model.AuthenticationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "JWT emitido tras un login exitoso")
public record TokenResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {

    public static TokenResponse from(AuthenticationResult result) {
        return new TokenResponse(result.token().value(), result.token().type(), result.token().expiresAt(),
                UserResponse.from(result.user()));
    }
}
