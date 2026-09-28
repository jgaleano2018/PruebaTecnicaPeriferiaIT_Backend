package com.periferia.social.shared.exception;

/**
 * Raíz de las excepciones de negocio. Los adaptadores de entrada (web) la traducen
 * a respuestas HTTP RFC 7807 sin que el dominio conozca HTTP.
 */
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
