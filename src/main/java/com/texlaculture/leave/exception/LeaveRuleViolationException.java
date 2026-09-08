package com.texlaculture.leave.exception;

/**
 * Thrown when a leave request breaks a business rule -
 * e.g. insufficient balance, or overlapping dates.
 * Mapped to HTTP 400 by the global exception handler.
 */
public class LeaveRuleViolationException extends RuntimeException {
    public LeaveRuleViolationException(String message) {
        super(message);
    }
}
