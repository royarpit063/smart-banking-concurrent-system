package com.vityarthi.banking.exception;

/**
 * Thrown when PIN verification or customer authentication fails.
 */
public class AuthenticationException extends BankingException {
    public AuthenticationException(String message) {
        super(message, "ERR_AUTH_FAILED");
    }
}
