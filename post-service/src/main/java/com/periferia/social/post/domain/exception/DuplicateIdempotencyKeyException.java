package com.periferia.social.post.domain.exception;

/**
 * Señal técnica: otra petición concurrente registró primero la misma Idempotency-Key.
 * El caso de uso la captura y responde con el resultado de la petición ganadora.
 */
public class DuplicateIdempotencyKeyException extends RuntimeException {

    public DuplicateIdempotencyKeyException(String key, Throwable cause) {
        super("Idempotency-Key concurrente: " + key, cause);
    }
}
