package com.periferia.social.post.domain.model;

import com.periferia.social.shared.exception.BusinessRuleViolationException;

/** Value Object: contenido de la publicación, siempre válido una vez construido. */
public record PostMessage(String value) {

    public static final int MAX_LENGTH = 280;

    public PostMessage {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException("El mensaje no puede estar vacío");
        }
        value = value.strip();
        if (value.length() > MAX_LENGTH) {
            throw new BusinessRuleViolationException("El mensaje no puede superar " + MAX_LENGTH + " caracteres");
        }
    }
}
