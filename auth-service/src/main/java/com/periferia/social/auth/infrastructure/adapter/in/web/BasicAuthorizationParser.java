package com.periferia.social.auth.infrastructure.adapter.in.web;

import com.periferia.social.auth.domain.port.in.AuthenticateUserUseCase.Credentials;
import com.periferia.social.shared.exception.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Extrae usuario/clave de una cabecera {@code Authorization: Basic base64(user:pass)}.
 * Permite ofrecer login por GET sin exponer credenciales en la URL (ni en logs de acceso).
 */
final class BasicAuthorizationParser {

    private static final String PREFIX = "Basic ";

    private BasicAuthorizationParser() {
    }

    static Credentials parse(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            throw new InvalidCredentialsException("Se requiere la cabecera Authorization: Basic");
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorizationHeader.substring(PREFIX.length()).trim()),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Cabecera Authorization mal formada");
        }
        int separator = decoded.indexOf(':');
        if (separator <= 0) {
            throw new InvalidCredentialsException("Cabecera Authorization mal formada");
        }
        return new Credentials(decoded.substring(0, separator), decoded.substring(separator + 1));
    }
}
