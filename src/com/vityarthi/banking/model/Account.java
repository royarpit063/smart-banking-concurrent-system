package com.vityarthi.banking.model;

import com.vityarthi.banking.exception.AuthenticationException;
import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.exception.InvalidTransactionException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class representing a generic Bank Account.
 * Demonstrates Unit 2: Abstract Classes & Methods, Encapsulation, Polymorphism,
 * and Unit 3: Thread-safe state locks for concurrency.
 */
public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String accountNumber;
    protected final String userId;
    protected final String accountHolderName;
    protected final AccountType accountType;
    protected double balance;
    protected String pinHash;
    protected boolean isActive;
    protected final LocalDateTime createdAt;

    // Concurrency lock for fine-grained account-level synchronization
    protected final transient ReentrantLock accountLock = new ReentrantLock(true);

    public Account(String accountNumber, String userId, String accountHolderName,
                   AccountType accountType, double initialBalance, String pinHash) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.pinHash = pinHash;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Account(String accountNumber, String userId, String accountHolderName,
                   AccountType accountType, double balance, String pinHash,
                   boolean isActive, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.balance = balance;
        this.pinHash = pinHash;
        this.isActive = isActive;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // --- Abstract Methods to be implemented by Subclasses (Polymorphism) ---

    /**
     * Calculates projected interest earned over a given duration in years.
     * @param years Duration in years
     * @return Projected interest amount
     */
    public abstract double calculateInterest(int years);

    /**
     * Checks if the proposed debit amount violates account-specific balance rules.
     * @param amount The amount to withdraw/transfer
     * @throws InsufficientFundsException if balance limits are breached
     * @throws InvalidTransactionException if account state is invalid
     */
    public abstract void validateWithdrawal(double amount)
            throws InsufficientFundsException, InvalidTransactionException;

    // --- Thread-Safe Synchronized Balance Operations ---

    /**
     * Deposits money into the account in a thread-safe manner.
     */
    public void deposit(double amount) throws InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Deposit amount must be strictly greater than zero.");
        }
        if (!isActive) {
            throw new InvalidTransactionException("Cannot deposit into an inactive/frozen account: " + accountNumber);
        }

        accountLock.lock();
        try {
            this.balance += amount;
        } finally {
            accountLock.unlock();
        }
    }

    /**
     * Withdraws money from the account in a thread-safe manner.
     */
    public void withdraw(double amount)
            throws InsufficientFundsException, InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be strictly greater than zero.");
        }
        if (!isActive) {
            throw new InvalidTransactionException("Cannot withdraw from an inactive/frozen account: " + accountNumber);
        }

        accountLock.lock();
        try {
            validateWithdrawal(amount);
            this.balance -= amount;
        } finally {
            accountLock.unlock();
        }
    }

    /**
     * Validates account PIN.
     */
    public void verifyPin(String enteredPin) throws AuthenticationException {
        if (enteredPin == null || !enteredPin.equals(this.pinHash)) {
            throw new AuthenticationException("Invalid security PIN for account: " + accountNumber);
        }
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public double getBalance() {
        accountLock.lock();
        try {
            return balance;
        } finally {
            accountLock.unlock();
        }
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReentrantLock getAccountLock() {
        return accountLock;
    }

    @Override
    public String toString() {
        return String.format("[%s] AccNo: %s | Holder: %-18s | Balance: ₹%10.2f | Status: %s",
                accountType, accountNumber, accountHolderName, balance, (isActive ? "ACTIVE" : "INACTIVE"));
    }
}
