package com.periferia.social.shared.exception;

/** Regla de negocio violada. */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    protected BusinessRuleViolationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
