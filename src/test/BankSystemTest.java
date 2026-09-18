package test;

import com.vityarthi.banking.exception.AuthenticationException;
import com.vityarthi.banking.exception.InsufficientFundsException;
import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.model.AccountType;
import com.vityarthi.banking.model.CurrentAccount;
import com.vityarthi.banking.model.SavingsAccount;
import com.vityarthi.banking.model.User;
import com.vityarthi.banking.service.BankService;
import com.vityarthi.banking.service.BankServiceImpl;
import com.vityarthi.banking.service.ConcurrentTransferEngine;
import com.vityarthi.banking.util.StatementExporter;

import java.io.File;
import java.util.List;

/**
 * Automated Verification & Unit Test Suite for Smart Banking System.
 * Tests OOP rules, Concurrency Thread-Safety, Exception handling, and File I/O.
 */
public class BankSystemTest {
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("         RUNNING AUTOMATED BANKING SYSTEM TEST SUITE           ");
        System.out.println("===============================================================");

        BankService service = new BankServiceImpl();

        testCustomerAndAccountCreation(service);
        testSavingsAccountRulesAndInterest(service);
        testCurrentAccountOverdraft(service);
        testAuthenticationAndPinSecurity(service);
        testAtomicFundTransfer(service);
        testInsufficientFundsException(service);
        testFileStatementExport(service);
        testMultithreadedConcurrencyStressTest(service);

        System.out.println("===============================================================");
        System.out.printf(" TEST SUMMARY: Total: %d | Passed: %d | Failed: %d\n",
                passedTests + failedTests, passedTests, failedTests);
        System.out.println("===============================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println(" [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println(" [FAIL] " + testName);
            failedTests++;
        }
    }

    private static void testCustomerAndAccountCreation(BankService service) {
        try {
            User u = service.registerCustomer("Test User One", "test1@bank.com", "9876543210", "Test City");
            Account acc = service.createAccount(u.getUserId(), AccountType.SAVINGS, 10000.0, "1111");

            assertTrue("Test Customer & Account Creation",
                    acc != null && acc.getBalance() == 10000.0 && acc.getAccountNumber().startsWith("SB"));
        } catch (Exception e) {
            assertTrue("Test Customer & Account Creation (Exception: " + e.getMessage() + ")", false);
        }
    }

    private static void testSavingsAccountRulesAndInterest(BankService service) {
        try {
            SavingsAccount sa = new SavingsAccount("SB9999", "USR-99", "Savings Tester", 10000.0, "1234");
            double interest1Year = sa.calculateInterest(1);
            // 4% of 10,000 = 400.0
            assertTrue("Test Savings Account 4% Interest Calculation", Math.abs(interest1Year - 400.0) < 0.01);
            assertTrue("Test Savings Min Balance is 500", sa.getMinimumBalance() == 500.0);
        } catch (Exception e) {
            assertTrue("Test Savings Account Rules", false);
        }
    }

    private static void testCurrentAccountOverdraft(BankService service) {
        try {
            CurrentAccount ca = new CurrentAccount("CA9999", "USR-99", "Current Tester", 5000.0, "1234");
            // Balance 5000 + Overdraft 25000 = total 30000 available
            ca.withdraw(20000.0);
            assertTrue("Test Current Account Overdraft Withdrawal", ca.getBalance() == -15000.0);
        } catch (Exception e) {
            assertTrue("Test Current Account Overdraft", false);
        }
    }

    private static void testAuthenticationAndPinSecurity(BankService service) {
        try {
            User u = service.registerCustomer("Security Tester", "sec@bank.com", "9998887770", "Sec City");
            Account acc = service.createAccount(u.getUserId(), AccountType.SAVINGS, 5000.0, "9876");

            boolean failedAsExpected = false;
            try {
                service.withdraw(acc.getAccountNumber(), 1000.0, "0000", "Wrong PIN test");
            } catch (AuthenticationException ae) {
                failedAsExpected = true;
            }

            assertTrue("Test PIN Security & Authentication Exception", failedAsExpected);
        } catch (Exception e) {
            assertTrue("Test Authentication Exception", false);
        }
    }

    private static void testAtomicFundTransfer(BankService service) {
        try {
            User u = service.registerCustomer("Transfer Tester", "tx@bank.com", "9112233445", "Transfer City");
            Account a1 = service.createAccount(u.getUserId(), AccountType.SAVINGS, 10000.0, "2222");
            Account a2 = service.createAccount(u.getUserId(), AccountType.SAVINGS, 5000.0, "2222");

            service.transferFunds(a1.getAccountNumber(), a2.getAccountNumber(), 2000.0, "2222", "Test Transfer");

            Account updatedA1 = service.getAccount(a1.getAccountNumber());
            Account updatedA2 = service.getAccount(a2.getAccountNumber());

            assertTrue("Test Atomic Fund Transfer Balances",
                    updatedA1.getBalance() == 8000.0 && updatedA2.getBalance() == 7000.0);
        } catch (Exception e) {
            assertTrue("Test Atomic Fund Transfer: " + e.getMessage(), false);
        }
    }

    private static void testInsufficientFundsException(BankService service) {
        try {
            User u = service.registerCustomer("Low Bal User", "low@bank.com", "9776655443", "Low City");
            Account acc = service.createAccount(u.getUserId(), AccountType.SAVINGS, 1000.0, "3333");

            boolean threwInsufficientFunds = false;
            try {
                // Minimum balance is 500, so withdrawing 800 from 1000 leaves 200 (< 500)
                service.withdraw(acc.getAccountNumber(), 800.0, "3333", "Exceed min balance");
            } catch (InsufficientFundsException ife) {
                threwInsufficientFunds = true;
            }

            assertTrue("Test InsufficientFundsException Enforcement", threwInsufficientFunds);
        } catch (Exception e) {
            assertTrue("Test InsufficientFundsException", false);
        }
    }

    private static void testFileStatementExport(BankService service) {
        try {
            User u = service.registerCustomer("Export Tester", "exp@bank.com", "9665544332", "Export City");
            Account acc = service.createAccount(u.getUserId(), AccountType.SAVINGS, 15000.0, "4444");
            service.deposit(acc.getAccountNumber(), 5000.0, "Salary Deposit");
            service.withdraw(acc.getAccountNumber(), 2000.0, "4444", "ATM Withdrawal");

            var txns = service.getAccountTransactions(acc.getAccountNumber());
            String txtPath = StatementExporter.exportToTextFile(acc, txns);
            String csvPath = StatementExporter.exportToCsvFile(acc, txns);

            File txtFile = new File(txtPath);
            File csvFile = new File(csvPath);

            assertTrue("Test File I/O Statement (.txt & .csv) Generation",
                    txtFile.exists() && txtFile.length() > 0 && csvFile.exists() && csvFile.length() > 0);
        } catch (Exception e) {
            assertTrue("Test File Statement Export: " + e.getMessage(), false);
        }
    }

    private static void testMultithreadedConcurrencyStressTest(BankService service) {
        try {
            User u = service.registerCustomer("Concurrency Tester", "conc@bank.com", "9554433221", "Conc City");
            Account a1 = service.createAccount(u.getUserId(), AccountType.SAVINGS, 50000.0, "5555");
            Account a2 = service.createAccount(u.getUserId(), AccountType.SAVINGS, 50000.0, "5555");

            ConcurrentTransferEngine engine = new ConcurrentTransferEngine(service);
            var result = engine.runStressTest(a1.getAccountNumber(), "5555",
                                              a2.getAccountNumber(), "5555",
                                              40, 250.0);

            assertTrue("Test Multi-Threaded Concurrency Zero Race Condition",
                    result.threadSafe() && result.successful() > 0);
        } catch (Exception e) {
            assertTrue("Test Multithreaded Concurrency: " + e.getMessage(), false);
        }
    }
}
