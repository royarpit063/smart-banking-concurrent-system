package com.vityarthi.banking.service;

import com.vityarthi.banking.exception.AccountNotFoundException;
import com.vityarthi.banking.exception.AuthenticationException;
import com.vityarthi.banking.exception.BankingException;
import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.exception.InvalidTransactionException;
import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.AccountType;
import com.vityarthi.banking.model.Transaction;
import com.vityarthi.banking.model.User;

import java.util.List;

/**
 * Service interface declaring core banking business operations.
 * Demonstrates Unit 2: Java Interfaces, Unit 3: Exception signatures.
 */
public interface BankService {
    User registerCustomer(String fullName, String email, String phone, String address) throws BankingException;

    Account createAccount(String userId, AccountType accountType, double initialDeposit, String pin) throws BankingException;

    Account getAccount(String accountNumber) throws AccountNotFoundException, BankingException;

    List<Account> getAllAccounts() throws BankingException;

    List<Account> getCustomerAccounts(String userId) throws BankingException;

    Transaction deposit(String accountNumber, double amount, String remarks) throws BankingException;

    Transaction withdraw(String accountNumber, double amount, String pin, String remarks)
            throws InsufficientFundsException, AuthenticationException, BankingException;

    Transaction transferFunds(String sourceAccountNo, String destAccountNo, double amount, String pin, String remarks)
            throws InsufficientFundsException, AuthenticationException, BankingException;

    double applyAnnualInterest(String accountNumber) throws AccountNotFoundException, BankingException;

    List<Transaction> getAccountTransactions(String accountNumber) throws BankingException;

    List<Transaction> getAllTransactions() throws BankingException;
}
