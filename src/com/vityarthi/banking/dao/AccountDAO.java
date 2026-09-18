package com.vityarthi.banking.dao;

import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Account and User persistence.
 * Demonstrates Unit 2: Java Interfaces, Unit 5: JDBC DAO Pattern.
 */
public interface AccountDAO {
    void saveUser(User user) throws SQLException;
    Optional<User> findUserById(String userId) throws SQLException;

    void saveAccount(Account account) throws SQLException;
    Optional<Account> findAccountByNumber(String accountNumber) throws SQLException;
    List<Account> findAllAccounts() throws SQLException;
    List<Account> findAccountsByUserId(String userId) throws SQLException;

    void updateAccountBalance(String accountNumber, double newBalance) throws SQLException;
    void updateAccountStatus(String accountNumber, boolean isActive) throws SQLException;
    void deleteAccount(String accountNumber) throws SQLException;
}
