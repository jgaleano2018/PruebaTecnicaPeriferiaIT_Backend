package com.periferia.social.shared.exception;

/** Recurso no encontrado. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    protected NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
