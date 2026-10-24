package com.goatfarm.exception;

/** A request is well-formed but violates a domain rule or validation. Mapped to HTTP 400. */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
