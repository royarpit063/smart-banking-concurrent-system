package com.vityarthi.banking.dao;

import com.vityarthi.banking.model.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for financial transaction records.
 * Demonstrates Unit 2: Interfaces, Unit 4: Collections (List), Unit 5: JDBC.
 */
public interface TransactionDAO {
    void saveTransaction(Transaction transaction) throws SQLException;
    List<Transaction> findTransactionsByAccount(String accountNumber) throws SQLException;
    List<Transaction> findAllTransactions() throws SQLException;
}
