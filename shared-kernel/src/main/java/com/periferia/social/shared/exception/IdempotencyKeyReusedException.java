package com.periferia.social.shared.exception;

/** La misma Idempotency-Key se reutilizó con un cuerpo de petición distinto. */
public class IdempotencyKeyReusedException extends ConflictException {

    public IdempotencyKeyReusedException(String key) {
        super(ErrorCode.IDEMPOTENCY_KEY_REUSED,
                "La Idempotency-Key '%s' ya fue usada con una petición diferente".formatted(key));
    }
}
