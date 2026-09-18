# Smart Banking & Concurrent Transaction System

> **A Thread-Safe, Modular Enterprise Banking & Transaction Processing System in Core Java with JDBC Persistence.**

---

## 📌 Project Overview
The **Smart Banking & Concurrent Transaction System** is an enterprise-grade Java software solution engineered to handle multi-tier banking accounts, high-frequency concurrent fund transfers, PIN-authenticated withdrawals, automated interest calculation, immutable audit logging, and relational persistence using JDBC.

This project is built from the ground up to solve fundamental race-condition challenges (lost updates, balance inconsistency, double spending) in concurrent banking environments while adhering to the highest standards of Object-Oriented Design and the entire Java course syllabus.

---

## 🌟 Key Features

### 1. Multi-Tier Account Hierarchy (OOP & Polymorphism)
- **Savings Account:** Enforces a minimum balance of ₹500 and provides annual compound interest calculation (4% p.a.).
- **Current Account:** Supports an overdraft facility up to ₹25,000, allowing commercial debits past zero balance without penalty.
- **Dynamic Method Dispatch:** Polymorphic behavior for interest projection, fee rules, and balance validation.

### 2. Thread-Safe Concurrency & Deadlock Prevention
- **Fine-Grained Account Locking:** Thread locks per account instance using `ReentrantLock` ensuring isolation during concurrent credit/debit operations.
- **Deadlock-Free Fund Transfers:** Deterministic lock ordering (lexicographical sorting of account numbers) guarantees deadlock-free transfers across simultaneous cross-account requests.
- **Stress-Test Simulation Engine:** Built-in multi-threaded harness simulating 40+ concurrent threads transferring funds simultaneously, verifying exact zero mathematical balance discrepancy.

### 3. Relational Persistence via JDBC (ACID Transactions)
- **DAO Pattern:** Decoupled data persistence layer (`AccountDAO`, `TransactionDAO`).
- **Parameterized SQL (`PreparedStatement`):** Comprehensive protection against SQL injection vulnerabilities.
- **External Configuration:** Database properties loaded via `db.properties` with fallback defaults.

### 4. Custom Domain Exception Hierarchy
- Explicit business exceptions for domain boundaries:
  - `InsufficientFundsException`
  - `AccountNotFoundException`
  - `InvalidTransactionException`
  - `AuthenticationException`
  - `BankingException` (Base Checked Exception)

### 5. File I/O Streams & Audit Logging
- **Official Account Statements:** Generates formatted `.txt` and `.csv` statements using character streams (`BufferedWriter`, `FileWriter`).
- **Thread-Aware Audit Logger:** Thread-safe logging with timestamps, thread IDs, action names, and payload details in `bank_audit.log`.

---

## 🛠️ Technologies & Tools Used
- **Language:** Java (JDK 17+)
- **Persistence:** JDBC API with SQLite Database Engine
- **Concurrency Utilities:** `java.util.concurrent` (`ExecutorService`, `CountDownLatch`, `ReentrantLock`, `AtomicInteger`)
- **I/O Streams:** `java.io` (`BufferedReader`, `BufferedWriter`, `FileWriter`, `FileInputStream`, `FileOutputStream`)
- **Version Control:** Git & GitHub

---

## 📂 Project Architecture & Package Structure

```text
├── lib/                                    # External JDBC and Logging Libraries
│   ├── sqlite-jdbc.jar
│   ├── slf4j-api.jar
│   └── slf4j-simple.jar
├── src/
│   ├── com/vityarthi/banking/
│   │   ├── config/
│   │   │   └── DatabaseConfig.java         # Singleton DB Connection Manager & Schema Init
│   │   ├── model/
│   │   │   ├── Account.java                # Abstract Base Account
│   │   │   ├── SavingsAccount.java         # Savings Account Implementation
│   │   │   ├── CurrentAccount.java         # Current Account Implementation
│   │   │   ├── User.java                   # Customer Entity
│   │   │   ├── Transaction.java            # Immutable Ledger POJO
│   │   │   ├── AccountType.java            # Enum (SAVINGS, CURRENT)
│   │   │   ├── TransactionType.java        # Enum (DEPOSIT, WITHDRAWAL, TRANSFER)
│   │   │   └── TransactionStatus.java      # Enum (SUCCESS, FAILED, PENDING)
│   │   ├── dao/
│   │   │   ├── AccountDAO.java             # Interface for Account CRUD
│   │   │   ├── AccountDAOImpl.java         # JDBC PreparedStatement Implementation
│   │   │   ├── TransactionDAO.java         # Interface for Transaction CRUD
│   │   │   └── TransactionDAOImpl.java     # JDBC Implementation
│   │   ├── service/
│   │   │   ├── BankService.java            # Business Service Interface
│   │   │   ├── BankServiceImpl.java        # Core Business Logic & Concurrency Control
│   │   │   └── ConcurrentTransferEngine.java # Multi-Threaded Stress Test Harness
│   │   ├── exception/
│   │   │   ├── BankingException.java       # Base Checked Exception
│   │   │   ├── InsufficientFundsException.java
│   │   │   ├── AccountNotFoundException.java
│   │   │   ├── InvalidTransactionException.java
│   │   │   └── AuthenticationException.java
│   │   ├── util/
│   │   │   ├── StatementExporter.java      # Text & CSV Stream File Exporter
│   │   │   └── AuditLogger.java            # Thread-Safe File Audit Logger
│   │   └── Main.java                       # Interactive Console Menu Driver
│   └── test/
│       └── BankSystemTest.java             # Automated Verification Test Suite
├── RUN_APP.bat                             # 1-Click Application Runner (Auto-compiles and launches)
├── RUN_TESTS.bat                           # 1-Click Automated Test Suite Runner
├── statement.md                            # Project Statement & Scope Document
├── PROJECT_REPORT.md                       # Comprehensive Submission Report & UML Diagrams
└── README.md                               # Project Guide & Documentation
```

---

## 🚀 How to Run (1-Click)

### 🔹 Option 1: Direct 1-Click Run (Easiest)
- Simply double-click **`RUN_APP.bat`** in File Explorer or VS Code. It will automatically compile all source files and open the interactive banking application.
- To run all automated unit and concurrency tests, double-click **`RUN_TESTS.bat`**.

### 🔹 Option 2: In VS Code
- Press <kbd>Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>B</kbd> (or go to `Terminal` → `Run Build Task...`) to launch the application directly.
- Or press <kbd>F5</kbd> to launch via the Debugger.

---

## 🧪 Testing Instructions

To run the automated verification test suite:
- Double click **`RUN_TESTS.bat`**


Run the automated test suite to verify OOP behavior, exception handling, file generation, and multi-threaded race-condition prevention:

```cmd
RUN_TESTS.bat
```

Or manually:
```bash
java -cp "out;lib/*" test.BankSystemTest
```

### Expected Test Output
```text
===============================================================
         RUNNING AUTOMATED BANKING SYSTEM TEST SUITE           
===============================================================
 [PASS] Test Customer & Account Creation
 [PASS] Test Savings Account 4% Interest Calculation
 [PASS] Test Savings Min Balance is 500
 [PASS] Test Current Account Overdraft Withdrawal
 [PASS] Test PIN Security & Authentication Exception
 [PASS] Test Atomic Fund Transfer Balances
 [PASS] Test InsufficientFundsException Enforcement
 [PASS] Test File I/O Statement (.txt & .csv) Generation

=======================================================
   STARTING MULTI-THREADED CONCURRENCY STRESS TEST     
=======================================================
 Threads: 40 | Amount Per Txn: ₹250.00
 Initial Balance [SB...]: ₹50000.00
 Initial Balance [SB...]: ₹50000.00
 Initial Total Sum: ₹100000.00
-------------------------------------------------------
               STRESS TEST EXECUTION RESULTS           
-------------------------------------------------------
 Completed in      : 1400 ms
 Successful Txns   : 40
 Failed/Rejected   : 0
 Final Total Sum   : ₹100000.00
 Balance Delta     : ₹0.0000 (PASSED - ZERO RACE CONDITIONS)
=======================================================

 [PASS] Test Multi-Threaded Concurrency Zero Race Condition
===============================================================
 TEST SUMMARY: Total: 9 | Passed: 9 | Failed: 0
===============================================================
```

---

## 🖥️ Interactive Console Demonstration

```text
-------------------------- MAIN MENU ---------------------------------
 [1]  Register New Customer Profile
 [2]  Open New Bank Account (Savings / Current)
 [3]  Deposit Funds
 [4]  Withdraw Funds (PIN Protected)
 [5]  Transfer Funds (Concurrent & Thread-Safe)
 [6]  Check Account Details & Balance
 [7]  View Account Transaction History
 [8]  Export Official Account Statement (Text / CSV File I/O)
 [9]  Apply Annual Interest (Polymorphic Calculation)
 [10] Run Multi-Threaded Concurrency Stress-Test Demo
 [11] View Real-Time Audit Logs
 [0]  Exit System
----------------------------------------------------------------------
```
