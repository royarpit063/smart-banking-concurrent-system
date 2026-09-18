package com.vityarthi.banking.exception;

/**
 * Base checked exception for all banking system domain errors.
 * Demonstrates Unit 3: Java Exceptions and custom exception hierarchy.
 */
public class BankingException extends Exception {
    private final String errorCode;

    public BankingException(String message) {
        super(message);
        this.errorCode = "BANK_ERR_GENERIC";
    }

    public BankingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BANK_ERR_SYSTEM";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
