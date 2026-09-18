package com.vityarthi.banking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Entity representing a financial ledger entry.
 * Demonstrates Unit 2: Immutable POJO design, Unit 4: Formatted string manipulation.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String transactionId;
    private final String sourceAccountNumber;
    private final String destinationAccountNumber;
    private final TransactionType transactionType;
    private final double amount;
    private final double balanceAfter;
    private final TransactionStatus status;
    private final String remarks;
    private final LocalDateTime timestamp;

    public Transaction(String sourceAccountNumber, String destinationAccountNumber,
                       TransactionType transactionType, double amount, double balanceAfter,
                       TransactionStatus status, String remarks) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.remarks = remarks;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String transactionId, String sourceAccountNumber, String destinationAccountNumber,
                       TransactionType transactionType, double amount, double balanceAfter,
                       TransactionStatus status, String remarks, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.remarks = remarks;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-12s | %-16s | Amount: ₹%10.2f | BalAfter: ₹%10.2f | Status: %-7s | Remarks: %s",
                getFormattedTimestamp(), transactionId, transactionType.getDescription(),
                amount, balanceAfter, status, remarks);
    }
}
