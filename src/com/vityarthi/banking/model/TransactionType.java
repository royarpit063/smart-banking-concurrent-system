package com.vityarthi.banking.model;

/**
 * Enumeration representing categories of banking transactions.
 */
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_DEBIT("Transfer Sent"),
    TRANSFER_CREDIT("Transfer Received"),
    INTEREST_CREDIT("Interest Credit");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
