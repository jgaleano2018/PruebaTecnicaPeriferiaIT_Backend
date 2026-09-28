package com.periferia.social.shared.exception;

/** Credenciales inválidas. */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }

    protected InvalidCredentialsException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
