package com.vityarthi.banking;

import com.vityarthi.banking.exception.BankingException;
import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.AccountType;
import com.vityarthi.banking.model.Transaction;
import com.vityarthi.banking.model.User;
import com.vityarthi.banking.service.BankService;
import com.vityarthi.banking.service.BankServiceImpl;
import com.vityarthi.banking.service.ConcurrentTransferEngine;
import com.vityarthi.banking.util.AuditLogger;
import com.vityarthi.banking.util.StatementExporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class providing an interactive console user interface
 * and multi-threaded transaction demonstration.
 * Demonstrates Unit 1: Flow Control, Switch cases, Loops, Scanner I/O,
 * and unites all units into a full end-to-end working software product.
 */
public class Main {
    private static final BankService bankService = new BankServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        AuditLogger.info("SYSTEM_START", "Smart Banking & Concurrent Transaction System started.");
        System.out.println("=======================================================================");
        System.out.println("          WELCOME TO SMART BANKING & CONCURRENCY SYSTEM               ");
        System.out.println("=======================================================================");

        // Seed initial sample data if empty
        seedInitialDemoData();

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Select an option [0-11]: ");
            String choiceStr = scanner.nextLine().trim();

            try {
                switch (choiceStr) {
                    case "1" -> handleRegisterCustomer();
                    case "2" -> handleCreateAccount();
                    case "3" -> handleDeposit();
                    case "4" -> handleWithdraw();
                    case "5" -> handleTransfer();
                    case "6" -> handleCheckBalance();
                    case "7" -> handleViewTransactions();
                    case "8" -> handleExportStatement();
                    case "9" -> handleApplyInterest();
                    case "10" -> handleRunConcurrencySimulation();
                    case "11" -> handleViewAuditLogs();
                    case "0" -> {
                        System.out.println("\nThank you for using Smart Banking System. Exiting...");
                        AuditLogger.info("SYSTEM_STOP", "System shut down cleanly.");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please choose between 0 and 11.");
                }
            } catch (Exception e) {
                System.out.printf("[ERROR] %s\n", e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMainMenu() {
        System.out.println("\n-------------------------- MAIN MENU ---------------------------------");
        System.out.println(" [1]  Register New Customer Profile");
        System.out.println(" [2]  Open New Bank Account (Savings / Current)");
        System.out.println(" [3]  Deposit Funds");
        System.out.println(" [4]  Withdraw Funds (PIN Protected)");
        System.out.println(" [5]  Transfer Funds (Concurrent & Thread-Safe)");
        System.out.println(" [6]  Check Account Details & Balance");
        System.out.println(" [7]  View Account Transaction History");
        System.out.println(" [8]  Export Official Account Statement (Text / CSV File I/O)");
        System.out.println(" [9]  Apply Annual Interest (Polymorphic Calculation)");
        System.out.println(" [10] Run Multi-Threaded Concurrency Stress-Test Demo");
        System.out.println(" [11] View Real-Time Audit Logs");
        System.out.println(" [0]  Exit System");
        System.out.println("----------------------------------------------------------------------");
    }

    private static void seedInitialDemoData() {
        try {
            if (bankService.getAllAccounts().isEmpty()) {
                System.out.println("[Bootstrap] Seeding demo customer accounts...");
                User u1 = bankService.registerCustomer("Alice Sharma", "alice@example.com", "9876543210", "123 Park Street, Bangalore");
                User u2 = bankService.registerCustomer("Bob Varma", "bob@example.com", "9123456780", "456 Silicon Valley, Hyderabad");

                Account a1 = bankService.createAccount(u1.getUserId(), AccountType.SAVINGS, 50000.0, "1234");
                Account a2 = bankService.createAccount(u2.getUserId(), AccountType.CURRENT, 80000.0, "5678");

                System.out.printf("[Bootstrap] Initialized Demo Accounts: %s (Alice) and %s (Bob)\n",
                        a1.getAccountNumber(), a2.getAccountNumber());
            }
        } catch (Exception e) {
            // Silently continue if already seeded
        }
    }

    private static void handleRegisterCustomer() throws BankingException {
        System.out.println("\n--- [1] Register New Customer ---");
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine();
        System.out.print("Enter 10-digit Phone Number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Residential Address: ");
        String address = scanner.nextLine();

        User user = bankService.registerCustomer(name, email, phone, address);
        System.out.println("[SUCCESS] Customer registered successfully!");
        System.out.printf("Customer ID: %s | Name: %s\n", user.getUserId(), user.getFullName());
    }

    private static void handleCreateAccount() throws BankingException {
        System.out.println("\n--- [2] Open New Bank Account ---");
        System.out.print("Enter Customer User ID (e.g. USR-1234): ");
        String userId = scanner.nextLine().trim();

        System.out.println("Select Account Type:");
        System.out.println(" 1. Savings Account (4% Annual Interest, Min ₹500 balance)");
        System.out.println(" 2. Current Account (₹25,000 Overdraft facility, Min ₹1000 balance)");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();

        AccountType type = "2".equals(typeChoice) ? AccountType.CURRENT : AccountType.SAVINGS;

        System.out.print("Enter Initial Deposit Amount (₹): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Set a 4-Digit Security PIN: ");
        String pin = scanner.nextLine().trim();

        Account acc = bankService.createAccount(userId, type, amount, pin);
        System.out.println("[SUCCESS] Account created successfully!");
        System.out.printf("Account Number: %s | Type: %s | Balance: ₹%.2f\n",
                acc.getAccountNumber(), acc.getAccountType(), acc.getBalance());
    }

    private static void handleDeposit() throws BankingException {
        System.out.println("\n--- [3] Deposit Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter Deposit Amount (₹): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Remarks: ");
        String remarks = scanner.nextLine().trim();

        Transaction txn = bankService.deposit(accNo, amount, remarks);
        System.out.println("[SUCCESS] Deposit completed!");
        System.out.println(txn);
    }

    private static void handleWithdraw() throws BankingException {
        System.out.println("\n--- [4] Withdraw Funds ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();
        System.out.print("Enter 4-Digit PIN: ");
        String pin = scanner.nextLine().trim();
        System.out.print("Enter Withdrawal Amount (₹): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Remarks: ");
        String remarks = scanner.nextLine().trim();

        Transaction txn = bankService.withdraw(accNo, amount, pin, remarks);
        System.out.println("[SUCCESS] Withdrawal completed!");
        System.out.println(txn);
    }

    private static void handleTransfer() throws BankingException {
        System.out.println("\n--- [5] Transfer Funds ---");
        System.out.print("Enter Source Account Number: ");
        String srcAcc = scanner.nextLine().trim();
        System.out.print("Enter Source Account PIN: ");
        String pin = scanner.nextLine().trim();
        System.out.print("Enter Destination Account Number: ");
        String dstAcc = scanner.nextLine().trim();
        System.out.print("Enter Transfer Amount (₹): ");
        double amount = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Enter Remarks: ");
        String remarks = scanner.nextLine().trim();

        Transaction txn = bankService.transferFunds(srcAcc, dstAcc, amount, pin, remarks);
        System.out.println("[SUCCESS] Fund transfer completed atomically!");
        System.out.println(txn);
    }

    private static void handleCheckBalance() throws BankingException {
        System.out.println("\n--- [6] Check Account Details ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        Account acc = bankService.getAccount(accNo);
        System.out.println("\n" + acc);
        System.out.printf("  Projected 1-Year Interest: ₹%.2f\n", acc.calculateInterest(1));
    }

    private static void handleViewTransactions() throws BankingException {
        System.out.println("\n--- [7] View Transaction History ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        List<Transaction> txns = bankService.getAccountTransactions(accNo);
        System.out.printf("\nFound %d transaction(s) for Account %s:\n", txns.size(), accNo);
        for (Transaction t : txns) {
            System.out.println(t);
        }
    }

    private static void handleExportStatement() throws Exception {
        System.out.println("\n--- [8] Export Account Statement ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        Account acc = bankService.getAccount(accNo);
        List<Transaction> txns = bankService.getAccountTransactions(accNo);

        System.out.println("Select Export Format:");
        System.out.println(" 1. Formatted Text Report (.txt)");
        System.out.println(" 2. CSV Spreadsheet (.csv)");
        System.out.print("Choice: ");
        String format = scanner.nextLine().trim();

        String filePath;
        if ("2".equals(format)) {
            filePath = StatementExporter.exportToCsvFile(acc, txns);
        } else {
            filePath = StatementExporter.exportToTextFile(acc, txns);
        }

        System.out.println("[SUCCESS] Statement exported successfully to:");
        System.out.println("Path: " + filePath);
    }

    private static void handleApplyInterest() throws BankingException {
        System.out.println("\n--- [9] Apply Annual Interest ---");
        System.out.print("Enter Account Number: ");
        String accNo = scanner.nextLine().trim();

        double interest = bankService.applyAnnualInterest(accNo);
        if (interest > 0) {
            System.out.printf("[SUCCESS] Applied annual interest of ₹%.2f to account %s\n", interest, accNo);
        } else {
            System.out.println("[INFO] No interest applicable for this account type or balance.");
        }
    }

    private static void handleRunConcurrencySimulation() {
        System.out.println("\n--- [10] Run Multi-Threaded Concurrency Stress-Test Demo ---");
        try {
            List<Account> accounts = bankService.getAllAccounts();
            if (accounts.size() < 2) {
                System.out.println("[WARN] Please ensure at least 2 accounts exist to run concurrency simulation.");
                return;
            }

            Account a1 = accounts.get(0);
            Account a2 = accounts.get(1);

            System.out.printf("Using Account 1: %s (PIN: %s) and Account 2: %s (PIN: %s)\n",
                    a1.getAccountNumber(), a1.getPinHash(), a2.getAccountNumber(), a2.getPinHash());

            System.out.print("Enter number of concurrent threads to spawn (e.g. 50): ");
            String input = scanner.nextLine().trim();
            int threads = input.isEmpty() ? 50 : Integer.parseInt(input);

            ConcurrentTransferEngine engine = new ConcurrentTransferEngine(bankService);
            engine.runStressTest(a1.getAccountNumber(), a1.getPinHash(),
                                 a2.getAccountNumber(), a2.getPinHash(),
                                 threads, 100.0);

        } catch (Exception e) {
            System.err.println("Failed to run simulation: " + e.getMessage());
        }
    }

    private static void handleViewAuditLogs() {
        System.out.println("\n--- [11] System Audit Logs (Last 15 Lines) ---");
        try (BufferedReader reader = new BufferedReader(new FileReader("bank_audit.log"))) {
            List<String> lines = reader.lines().toList();
            int start = Math.max(0, lines.size() - 15);
            for (int i = start; i < lines.size(); i++) {
                System.out.println(lines.get(i));
            }
        } catch (Exception e) {
            System.out.println("No audit logs recorded yet or file not created.");
        }
    }
}
