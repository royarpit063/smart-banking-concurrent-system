package com.vityarthi.banking.model;

import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.exception.InvalidTransactionException;

import java.time.LocalDateTime;

/**
 * Represents a Commercial/Current Account with an approved Overdraft facility.
 * Demonstrates Unit 2: Inheritance, Overdraft credit limit handling, Method Overriding.
 */
public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private double overdraftLimit;

    public CurrentAccount(String accountNumber, String userId, String accountHolderName,
                          double initialBalance, String pinHash) {
        super(accountNumber, userId, accountHolderName, AccountType.CURRENT, initialBalance, pinHash);
        this.overdraftLimit = 25000.0; // Standard default overdraft credit line
    }

    public CurrentAccount(String accountNumber, String userId, String accountHolderName,
                          double balance, String pinHash, double overdraftLimit,
                          boolean isActive, LocalDateTime createdAt) {
        super(accountNumber, userId, accountHolderName, AccountType.CURRENT, balance, pinHash, isActive, createdAt);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double calculateInterest(int years) {
        // Current accounts do not earn interest
        return 0.0;
    }

    @Override
    public void validateWithdrawal(double amount) throws InsufficientFundsException, InvalidTransactionException {
        // Balance can go negative down to -overdraftLimit
        if ((balance + overdraftLimit) < amount) {
            throw new InsufficientFundsException(accountNumber, balance + overdraftLimit, amount);
        }
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(double overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public String toString() {
        return String.format("[%s] AccNo: %s | Holder: %-18s | Balance: ₹%10.2f | Overdraft Limit: ₹%.2f | Status: %s",
                accountType, accountNumber, accountHolderName, balance, overdraftLimit, (isActive ? "ACTIVE" : "INACTIVE"));
    }
}
