package com.vityarthi.banking.exception;

/**
 * Thrown when an operation is requested on an account number that does not exist.
 */
public class AccountNotFoundException extends BankingException {
    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super(String.format("Account '%s' was not found in the banking system.", accountNumber), "ERR_ACCOUNT_NOT_FOUND");
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
