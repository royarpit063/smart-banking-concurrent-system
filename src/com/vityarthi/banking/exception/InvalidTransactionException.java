package com.vityarthi.banking.exception;

/**
 * Thrown when transaction inputs, amounts, or states violate business rules.
 */
public class InvalidTransactionException extends BankingException {
    public InvalidTransactionException(String message) {
        super(message, "ERR_INVALID_TRANSACTION");
    }

    public InvalidTransactionException(String message, String errorCode) {
        super(message, errorCode);
    }
}
