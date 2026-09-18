package com.vityarthi.banking.exception;

/**
 * Thrown when an account has insufficient balance to complete a debit/transfer.
 */
public class InsufficientFundsException extends BankingException {
    private final String accountNumber;
    private final double availableBalance;
    private final double requestedAmount;

    public InsufficientFundsException(String accountNumber, double availableBalance, double requestedAmount) {
        super(String.format("Insufficient funds in account %s. Available: ₹%.2f, Requested: ₹%.2f",
                accountNumber, availableBalance, requestedAmount), "ERR_INSUFFICIENT_FUNDS");
        this.accountNumber = accountNumber;
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}
