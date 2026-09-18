package com.vityarthi.banking.dao;

import com.vityarthi.banking.config.DatabaseConfig;
import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.AccountType;
import com.vityarthi.banking.model.CurrentAccount;
import com.vityarthi.banking.model.SavingsAccount;
import com.vityarthi.banking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC Implementation of AccountDAO.
 * Demonstrates Unit 5: JDBC PreparedStatement, ResultSet, CRUD operations.
 */
public class AccountDAOImpl implements AccountDAO {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    @Override
    public void saveUser(User user) throws SQLException {
        String sql = "INSERT OR REPLACE INTO users (user_id, full_name, email, phone_number, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getAddress());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<User> findUserById(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone_number"),
                            rs.getString("address")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveAccount(Account account) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO accounts
            (account_number, user_id, holder_name, account_type, balance, pin_hash, overdraft_limit, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getUserId());
            ps.setString(3, account.getAccountHolderName());
            ps.setString(4, account.getAccountType().name());
            ps.setDouble(5, account.getBalance());
            ps.setString(6, account.getPinHash());

            double overdraft = 0.0;
            if (account instanceof CurrentAccount ca) {
                overdraft = ca.getOverdraftLimit();
            }
            ps.setDouble(7, overdraft);
            ps.setInt(8, account.isActive() ? 1 : 0);
            ps.setString(9, account.getCreatedAt().toString());

            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Account> findAccountByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Account> findAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_number ASC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }

    @Override
    public List<Account> findAccountsByUserId(String userId) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY account_number ASC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        }
        return accounts;
    }

    @Override
    public void updateAccountBalance(String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateAccountStatus(String accountNumber, boolean isActive) throws SQLException {
        String sql = "UPDATE accounts SET is_active = ? WHERE account_number = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isActive ? 1 : 0);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteAccount(String accountNumber) throws SQLException {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accNo = rs.getString("account_number");
        String userId = rs.getString("user_id");
        String name = rs.getString("holder_name");
        String typeStr = rs.getString("account_type");
        double balance = rs.getDouble("balance");
        String pin = rs.getString("pin_hash");
        double overdraft = rs.getDouble("overdraft_limit");
        boolean isActive = rs.getInt("is_active") == 1;
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        AccountType type = AccountType.valueOf(typeStr);
        if (type == AccountType.SAVINGS) {
            return new SavingsAccount(accNo, userId, name, balance, pin, isActive, createdAt);
        } else {
            return new CurrentAccount(accNo, userId, name, balance, pin, overdraft, isActive, createdAt);
        }
    }
}
