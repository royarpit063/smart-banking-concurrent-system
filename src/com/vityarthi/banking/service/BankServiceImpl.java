package com.vityarthi.banking.service;

import com.vityarthi.banking.dao.AccountDAO;
import com.vityarthi.banking.dao.AccountDAOImpl;
import com.vityarthi.banking.dao.TransactionDAO;
import com.vityarthi.banking.dao.TransactionDAOImpl;
import com.vityarthi.banking.exception.AccountNotFoundException;
import com.vityarthi.banking.exception.AuthenticationException;
import com.vityarthi.banking.exception.BankingException;
import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.exception.InvalidTransactionException;
import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.AccountType;
import com.vityarthi.banking.model.CurrentAccount;
import com.vityarthi.banking.model.SavingsAccount;
import com.vityarthi.banking.model.Transaction;
import com.vityarthi.banking.model.TransactionStatus;
import com.vityarthi.banking.model.TransactionType;
import com.vityarthi.banking.model.User;
import com.vityarthi.banking.util.AuditLogger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core implementation of BankService managing business transactions, concurrency, and persistence.
 * Demonstrates Unit 2: OOP & Polymorphism, Unit 3: Multithreading & Synchronization,
 * Unit 4: Collections, Unit 5: JDBC Persistence.
 */
public class BankServiceImpl implements BankService {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final Random random = new Random();

    // In-memory cache for fast thread-safe locking and lookup
    private final Map<String, Account> memoryAccountCache = new ConcurrentHashMap<>();

    public BankServiceImpl() {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        preloadAccountsToMemory();
    }

    public BankServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
        preloadAccountsToMemory();
    }

    private void preloadAccountsToMemory() {
        try {
            List<Account> accounts = accountDAO.findAllAccounts();
            for (Account acc : accounts) {
                memoryAccountCache.put(acc.getAccountNumber(), acc);
            }
        } catch (SQLException e) {
            AuditLogger.warn("INIT_CACHE", "Could not preload accounts from DB: " + e.getMessage());
        }
    }

    @Override
    public User registerCustomer(String fullName, String email, String phone, String address) throws BankingException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidTransactionException("Customer full name cannot be blank.");
        }
        if (phone == null || phone.trim().length() < 10) {
            throw new InvalidTransactionException("Invalid phone number. Must be at least 10 digits.");
        }

        String userId = "USR-" + (1000 + random.nextInt(9000));
        User user = new User(userId, fullName.trim(), email.trim(), phone.trim(), address != null ? address.trim() : "");
        try {
            accountDAO.saveUser(user);
            AuditLogger.info("REGISTER_USER", "Registered customer: " + user);
            return user;
        } catch (SQLException e) {
            AuditLogger.error("REGISTER_USER", "Failed to save user: " + e.getMessage());
            throw new BankingException("Database error registering customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Account createAccount(String userId, AccountType accountType, double initialDeposit, String pin)
            throws BankingException {
        if (pin == null || pin.length() < 4) {
            throw new InvalidTransactionException("PIN must be at least 4 digits.");
        }
        if (initialDeposit < accountType.getMinimumBalance()) {
            throw new InvalidTransactionException(String.format(
                    "Initial deposit ₹%.2f is below minimum requirement of ₹%.2f for %s",
                    initialDeposit, accountType.getMinimumBalance(), accountType.getDisplayName()));
        }

        try {
            Optional<User> userOpt = accountDAO.findUserById(userId);
            if (userOpt.isEmpty()) {
                throw new AccountNotFoundException("User ID not found: " + userId);
            }
            User user = userOpt.get();

            String accNumber = (accountType == AccountType.SAVINGS ? "SB" : "CA") + (100000 + random.nextInt(900000));
            Account account;
            if (accountType == AccountType.SAVINGS) {
                account = new SavingsAccount(accNumber, userId, user.getFullName(), initialDeposit, pin);
            } else {
                account = new CurrentAccount(accNumber, userId, user.getFullName(), initialDeposit, pin);
            }

            accountDAO.saveAccount(account);
            memoryAccountCache.put(accNumber, account);

            // Record initial deposit transaction
            Transaction initialTxn = new Transaction(
                    null, accNumber, TransactionType.DEPOSIT, initialDeposit, initialDeposit,
                    TransactionStatus.SUCCESS, "Initial Account Opening Deposit"
            );
            transactionDAO.saveTransaction(initialTxn);

            AuditLogger.info("CREATE_ACCOUNT", "Created account: " + accNumber + " for User: " + userId);
            return account;
        } catch (SQLException e) {
            AuditLogger.error("CREATE_ACCOUNT", "Failed to create account: " + e.getMessage());
            throw new BankingException("Database error creating account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccount(String accountNumber) throws AccountNotFoundException, BankingException {
        Account account = memoryAccountCache.get(accountNumber);
        if (account != null) {
            return account;
        }
        try {
            Optional<Account> opt = accountDAO.findAccountByNumber(accountNumber);
            if (opt.isPresent()) {
                memoryAccountCache.put(accountNumber, opt.get());
                return opt.get();
            }
            throw new AccountNotFoundException(accountNumber);
        } catch (SQLException e) {
            throw new BankingException("Database error fetching account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getAllAccounts() throws BankingException {
        try {
            List<Account> list = accountDAO.findAllAccounts();
            for (Account a : list) {
                memoryAccountCache.put(a.getAccountNumber(), a);
            }
            return list;
        } catch (SQLException e) {
            throw new BankingException("Failed to list accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getCustomerAccounts(String userId) throws BankingException {
        try {
            return accountDAO.findAccountsByUserId(userId);
        } catch (SQLException e) {
            throw new BankingException("Failed to list customer accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction deposit(String accountNumber, double amount, String remarks) throws BankingException {
        Account account = getAccount(accountNumber);

        account.getAccountLock().lock();
        try {
            account.deposit(amount);
            double newBalance = account.getBalance();
            accountDAO.updateAccountBalance(accountNumber, newBalance);

            Transaction txn = new Transaction(
                    null, accountNumber, TransactionType.DEPOSIT, amount, newBalance,
                    TransactionStatus.SUCCESS, remarks != null ? remarks : "Cash/Direct Deposit"
            );
            transactionDAO.saveTransaction(txn);
            AuditLogger.info("DEPOSIT", String.format("Acc: %s | +₹%.2f | Bal: ₹%.2f", accountNumber, amount, newBalance));
            return txn;
        } catch (SQLException e) {
            AuditLogger.error("DEPOSIT", "DB failure on deposit: " + e.getMessage());
            throw new BankingException("Database error persisting deposit: " + e.getMessage(), e);
        } finally {
            account.getAccountLock().unlock();
        }
    }

    @Override
    public Transaction withdraw(String accountNumber, double amount, String pin, String remarks)
            throws InsufficientFundsException, AuthenticationException, BankingException {
        Account account = getAccount(accountNumber);
        account.verifyPin(pin);

        account.getAccountLock().lock();
        try {
            account.withdraw(amount);
            double newBalance = account.getBalance();
            accountDAO.updateAccountBalance(accountNumber, newBalance);

            Transaction txn = new Transaction(
                    accountNumber, null, TransactionType.WITHDRAWAL, amount, newBalance,
                    TransactionStatus.SUCCESS, remarks != null ? remarks : "Cash Withdrawal"
            );
            transactionDAO.saveTransaction(txn);
            AuditLogger.info("WITHDRAW", String.format("Acc: %s | -₹%.2f | Bal: ₹%.2f", accountNumber, amount, newBalance));
            return txn;
        } catch (SQLException e) {
            AuditLogger.error("WITHDRAW", "DB failure on withdrawal: " + e.getMessage());
            throw new BankingException("Database error persisting withdrawal: " + e.getMessage(), e);
        } finally {
            account.getAccountLock().unlock();
        }
    }

    @Override
    public Transaction transferFunds(String sourceAccountNo, String destAccountNo, double amount, String pin, String remarks)
            throws InsufficientFundsException, AuthenticationException, BankingException {
        if (sourceAccountNo.equalsIgnoreCase(destAccountNo)) {
            throw new InvalidTransactionException("Source and destination accounts cannot be identical.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Transfer amount must be strictly greater than zero.");
        }

        Account src = getAccount(sourceAccountNo);
        Account dst = getAccount(destAccountNo);
        src.verifyPin(pin);

        // Deadlock Prevention: Always acquire locks in deterministic lexicographical order
        Account firstLock = sourceAccountNo.compareTo(destAccountNo) < 0 ? src : dst;
        Account secondLock = sourceAccountNo.compareTo(destAccountNo) < 0 ? dst : src;

        firstLock.getAccountLock().lock();
        secondLock.getAccountLock().lock();
        try {
            // Validate withdrawal limit on source
            src.validateWithdrawal(amount);

            // Execute transfers atomically
            src.withdraw(amount);
            dst.deposit(amount);

            double srcNewBal = src.getBalance();
            double dstNewBal = dst.getBalance();

            // Persist to database
            accountDAO.updateAccountBalance(sourceAccountNo, srcNewBal);
            accountDAO.updateAccountBalance(destAccountNo, dstNewBal);

            Transaction txn = new Transaction(
                    sourceAccountNo, destAccountNo, TransactionType.TRANSFER_DEBIT, amount, srcNewBal,
                    TransactionStatus.SUCCESS, remarks != null ? remarks : "Fund Transfer"
            );
            transactionDAO.saveTransaction(txn);

            AuditLogger.info("TRANSFER", String.format("Transferred ₹%.2f from %s (Bal: ₹%.2f) to %s (Bal: ₹%.2f)",
                    amount, sourceAccountNo, srcNewBal, destAccountNo, dstNewBal));
            return txn;
        } catch (SQLException e) {
            // Rollback in memory if DB fails
            AuditLogger.error("TRANSFER", "Transaction DB failed! Performing rollback: " + e.getMessage());
            throw new BankingException("Database error processing transfer: " + e.getMessage(), e);
        } finally {
            secondLock.getAccountLock().unlock();
            firstLock.getAccountLock().unlock();
        }
    }

    @Override
    public double applyAnnualInterest(String accountNumber) throws AccountNotFoundException, BankingException {
        Account account = getAccount(accountNumber);
        account.getAccountLock().lock();
        try {
            double interest = account.calculateInterest(1); // 1 year
            if (interest > 0) {
                account.deposit(interest);
                double newBal = account.getBalance();
                accountDAO.updateAccountBalance(accountNumber, newBal);

                Transaction txn = new Transaction(
                        null, accountNumber, TransactionType.INTEREST_CREDIT, interest, newBal,
                        TransactionStatus.SUCCESS, "Annual Interest Credit"
                );
                transactionDAO.saveTransaction(txn);
                AuditLogger.info("INTEREST", String.format("Credited ₹%.2f interest to %s (New Bal: ₹%.2f)",
                        interest, accountNumber, newBal));
            }
            return interest;
        } catch (SQLException e) {
            throw new BankingException("Failed to credit interest: " + e.getMessage(), e);
        } finally {
            account.getAccountLock().unlock();
        }
    }

    @Override
    public List<Transaction> getAccountTransactions(String accountNumber) throws BankingException {
        try {
            return transactionDAO.findTransactionsByAccount(accountNumber);
        } catch (SQLException e) {
            throw new BankingException("Failed to fetch account transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() throws BankingException {
        try {
            return transactionDAO.findAllTransactions();
        } catch (SQLException e) {
            throw new BankingException("Failed to fetch all transactions: " + e.getMessage(), e);
        }
    }
}
