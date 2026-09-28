package com.periferia.social.auth.domain.model;

import java.time.Instant;

/** Token de acceso emitido para un usuario autenticado. */
public record AccessToken(String value, String type, Instant expiresAt) {
}
