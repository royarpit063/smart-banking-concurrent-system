package com.vityarthi.banking.model;

import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.exception.InvalidTransactionException;

import java.time.LocalDateTime;

/**
 * Represents a Savings Account with compound interest and minimum balance enforcement.
 * Demonstrates Unit 2: Inheritance, Method Overriding, Polymorphism, and `super` keyword.
 */
public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final double minimumBalance;
    private final double annualInterestRate;

    public SavingsAccount(String accountNumber, String userId, String accountHolderName,
                          double initialBalance, String pinHash) {
        super(accountNumber, userId, accountHolderName, AccountType.SAVINGS, initialBalance, pinHash);
        this.minimumBalance = AccountType.SAVINGS.getMinimumBalance();
        this.annualInterestRate = AccountType.SAVINGS.getAnnualInterestRate();
    }

    public SavingsAccount(String accountNumber, String userId, String accountHolderName,
                          double balance, String pinHash, boolean isActive, LocalDateTime createdAt) {
        super(accountNumber, userId, accountHolderName, AccountType.SAVINGS, balance, pinHash, isActive, createdAt);
        this.minimumBalance = AccountType.SAVINGS.getMinimumBalance();
        this.annualInterestRate = AccountType.SAVINGS.getAnnualInterestRate();
    }

    @Override
    public double calculateInterest(int years) {
        // Compound interest: A = P(1 + r)^t - P
        if (years <= 0) return 0.0;
        return balance * (Math.pow(1 + annualInterestRate, years) - 1.0);
    }

    @Override
    public void validateWithdrawal(double amount) throws InsufficientFundsException, InvalidTransactionException {
        if ((balance - amount) < minimumBalance) {
            throw new InsufficientFundsException(accountNumber, balance, amount);
        }
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }
}
