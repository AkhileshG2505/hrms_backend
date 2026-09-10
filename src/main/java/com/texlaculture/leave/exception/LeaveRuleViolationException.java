package com.texlaculture.leave.exception;


public class LeaveRuleViolationException extends RuntimeException {
    public LeaveRuleViolationException(String message) {
        super(message);
    }
}
