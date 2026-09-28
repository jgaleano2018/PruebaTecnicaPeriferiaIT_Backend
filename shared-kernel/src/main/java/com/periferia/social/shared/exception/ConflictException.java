package com.periferia.social.shared.exception;

/** Conflicto con el estado actual del recurso. */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

    protected ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
