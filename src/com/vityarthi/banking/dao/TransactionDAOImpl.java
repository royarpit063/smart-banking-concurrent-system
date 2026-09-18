package com.vityarthi.banking.dao;

import com.vityarthi.banking.config.DatabaseConfig;
import com.vityarthi.banking.model.Transaction;
import com.vityarthi.banking.model.TransactionStatus;
import com.vityarthi.banking.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Implementation of TransactionDAO.
 * Demonstrates Unit 5: JDBC PreparedStatement, ResultSet handling.
 */
public class TransactionDAOImpl implements TransactionDAO {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    @Override
    public void saveTransaction(Transaction transaction) throws SQLException {
        String sql = """
            INSERT INTO transactions
            (transaction_id, source_account, destination_account, transaction_type, amount, balance_after, status, remarks, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transaction.getTransactionId());
            ps.setString(2, transaction.getSourceAccountNumber());
            ps.setString(3, transaction.getDestinationAccountNumber());
            ps.setString(4, transaction.getTransactionType().name());
            ps.setDouble(5, transaction.getAmount());
            ps.setDouble(6, transaction.getBalanceAfter());
            ps.setString(7, transaction.getStatus().name());
            ps.setString(8, transaction.getRemarks());
            ps.setString(9, transaction.getTimestamp().toString());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findTransactionsByAccount(String accountNumber) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = """
            SELECT * FROM transactions
            WHERE source_account = ? OR destination_account = ?
            ORDER BY timestamp DESC
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> findAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        }
        return list;
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("transaction_id"),
                rs.getString("source_account"),
                rs.getString("destination_account"),
                TransactionType.valueOf(rs.getString("transaction_type")),
                rs.getDouble("amount"),
                rs.getDouble("balance_after"),
                TransactionStatus.valueOf(rs.getString("status")),
                rs.getString("remarks"),
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }
}
