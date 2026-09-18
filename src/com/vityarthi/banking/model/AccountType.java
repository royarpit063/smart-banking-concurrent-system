package com.vityarthi.banking.model;

/**
 * Enumeration representing different types of bank accounts.
 * Demonstrates Unit 2: Java enum Class and Enum concepts.
 */
public enum AccountType {
    SAVINGS("Savings Account", 0.04, 500.0),
    CURRENT("Current Account", 0.00, 1000.0);

    private final String displayName;
    private final double annualInterestRate;
    private final double minimumBalance;

    AccountType(String displayName, double annualInterestRate, double minimumBalance) {
        this.displayName = displayName;
        this.annualInterestRate = annualInterestRate;
        this.minimumBalance = minimumBalance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    @Override
    public String toString() {
        return displayName + " [Interest: " + (annualInterestRate * 100) + "%, Min Bal: ₹" + minimumBalance + "]";
    }
}
