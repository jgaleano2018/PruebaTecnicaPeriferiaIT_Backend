package com.periferia.social.shared.exception;

/**
 * Catálogo de códigos de error estables expuestos a los clientes.
 * <p>
 * Cada código sugiere un status HTTP (como número, para no acoplar el kernel a ningún
 * framework web). Los adaptadores web de cada servicio (MVC o WebFlux) lo reutilizan,
 * evitando duplicar el mapeo (DRY).
 */
public enum ErrorCode {
    VALIDATION_ERROR(400),
    INVALID_CREDENTIALS(401),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    RESOURCE_NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    CONFLICT(409),
    IDEMPOTENCY_KEY_REUSED(409),
    UNSUPPORTED_MEDIA_TYPE(415),
    BUSINESS_RULE_VIOLATION(422),
    SERVICE_UNAVAILABLE(503),
    INTERNAL_ERROR(500);

    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }

    /** Código por defecto para un status HTTP producido por el framework (no por el dominio). */
    public static ErrorCode fromHttpStatus(int status) {
        return switch (status) {
            case 400 -> VALIDATION_ERROR;
            case 401 -> UNAUTHORIZED;
            case 403 -> FORBIDDEN;
            case 404 -> RESOURCE_NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 409 -> CONFLICT;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 422 -> BUSINESS_RULE_VIOLATION;
            case 503 -> SERVICE_UNAVAILABLE;
            default -> status >= 500 ? INTERNAL_ERROR : VALIDATION_ERROR;
        };
    }
}
