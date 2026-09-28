package com.periferia.social.auth.domain.model;

import com.periferia.social.shared.exception.BusinessRuleViolationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Agregado Usuario. Encapsula las invariantes de identidad; no conoce JPA ni Spring. */
public final class User {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._-]{3,50}$");
    private static final int MAX_DISPLAY_NAME = 100;

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final Instant createdAt;

    private User(UUID id, String username, String passwordHash, String displayName, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** Factory para un usuario nuevo: normaliza y valida sus datos. */
    public static User register(String username, String passwordHash, String displayName, Instant now) {
        String normalized = normalizeUsername(username);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new BusinessRuleViolationException(
                    "El usuario debe tener entre 3 y 50 caracteres [a-z, 0-9, '.', '_', '-']");
        }
        String name = displayName == null || displayName.isBlank() ? normalized : displayName.strip();
        if (name.length() > MAX_DISPLAY_NAME) {
            throw new BusinessRuleViolationException("El nombre visible no puede superar " + MAX_DISPLAY_NAME + " caracteres");
        }
        return new User(UUID.randomUUID(), normalized, passwordHash, name, now);
    }

    /** Reconstrucción desde persistencia (sin re-validar). */
    public static User restore(UUID id, String username, String passwordHash, String displayName, Instant createdAt) {
        return new User(id, username, passwordHash, displayName, createdAt);
    }

    public static String normalizeUsername(String username) {
        return username == null ? "" : username.strip().toLowerCase();
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String displayName() {
        return displayName;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
