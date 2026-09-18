package com.vityarthi.banking.util;

import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting formatted account statements to Text and CSV files.
 * Demonstrates Unit 4: Character-oriented I/O Streams, Buffer handling, String operations.
 */
public class StatementExporter {
    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Exports a comprehensive account statement to a formatted text file.
     * @param account The account entity
     * @param transactions List of transactions for this account
     * @return Path to generated statement file
     * @throws IOException on I/O failure
     */
    public static String exportToTextFile(Account account, List<Transaction> transactions) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FMT);
        String fileName = String.format("Statement_%s_%s.txt", account.getAccountNumber(), timestamp);
        File file = new File(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("========================================================================================\n");
            writer.write("                              SMART BANKING SYSTEM                                     \n");
            writer.write("                           OFFICIAL ACCOUNT STATEMENT                                  \n");
            writer.write("========================================================================================\n");
            writer.write(String.format(" Generated On      : %s\n", LocalDateTime.now()));
            writer.write(String.format(" Account Number    : %s\n", account.getAccountNumber()));
            writer.write(String.format(" Account Holder    : %s\n", account.getAccountHolderName()));
            writer.write(String.format(" Account Type      : %s\n", account.getAccountType().getDisplayName()));
            writer.write(String.format(" Current Balance   : ₹%,.2f\n", account.getBalance()));
            writer.write(String.format(" Account Status    : %s\n", account.isActive() ? "ACTIVE" : "FROZEN/CLOSED"));
            writer.write("----------------------------------------------------------------------------------------\n");
            writer.write(String.format(" %-20s | %-12s | %-16s | %12s | %12s | %s\n",
                    "Timestamp", "Txn ID", "Type", "Amount (₹)", "Balance (₹)", "Remarks"));
            writer.write("----------------------------------------------------------------------------------------\n");

            if (transactions.isEmpty()) {
                writer.write("                           No transaction records found.                                \n");
            } else {
                for (Transaction t : transactions) {
                    writer.write(String.format(" %-20s | %-12s | %-16s | %12.2f | %12.2f | %s\n",
                            t.getFormattedTimestamp(),
                            t.getTransactionId(),
                            t.getTransactionType().getDescription(),
                            t.getAmount(),
                            t.getBalanceAfter(),
                            t.getRemarks()));
                }
            }

            writer.write("========================================================================================\n");
            writer.write("              Thank you for banking with Smart Banking System!                          \n");
            writer.write("========================================================================================\n");
        }

        AuditLogger.info("STATEMENT_EXPORT", "Generated text statement for " + account.getAccountNumber() + " -> " + fileName);
        return file.getAbsolutePath();
    }

    /**
     * Exports transaction data to a standard CSV file for spreadsheet analysis.
     */
    public static String exportToCsvFile(Account account, List<Transaction> transactions) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FMT);
        String fileName = String.format("Statement_%s_%s.csv", account.getAccountNumber(), timestamp);
        File file = new File(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("TransactionID,Timestamp,SourceAccount,DestinationAccount,Type,Amount,BalanceAfter,Status,Remarks\n");
            for (Transaction t : transactions) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"\n",
                        t.getTransactionId(),
                        t.getFormattedTimestamp(),
                        t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "N/A",
                        t.getDestinationAccountNumber() != null ? t.getDestinationAccountNumber() : "N/A",
                        t.getTransactionType().name(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getStatus().name(),
                        t.getRemarks() != null ? t.getRemarks().replace("\"", "\"\"") : ""));
            }
        }

        AuditLogger.info("STATEMENT_CSV_EXPORT", "Generated CSV statement for " + account.getAccountNumber() + " -> " + fileName);
        return file.getAbsolutePath();
    }
}
