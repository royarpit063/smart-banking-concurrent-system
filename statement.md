# Problem Statement & Scope Document

## Project Title
**Smart Banking & Concurrent Transaction System**

---

## 1. Problem Statement
Traditional digital banking systems face critical challenges when processing simultaneous transactions on identical accounts across distributed channels (e.g., ATMs, mobile banking apps, online point-of-sale systems, and automated scheduled payments). Without robust concurrency control and thread synchronization, race conditions arise, leading to:
- **Lost updates** (e.g., simultaneous deposits overriding each other).
- **Double-spending / Inconsistent balances** (e.g., simultaneous ATM withdrawals exceeding the actual balance).
- **Database deadlocks and non-atomic state failures** during multi-step fund transfers.

Furthermore, academic and enterprise systems require a decoupled architecture where core banking logic, account hierarchies (Savings, Current), exception safety, automated audit logging, and relational persistence (JDBC) operate reliably under high transactional load.

The goal of this project is to design and implement a **Smart Banking & Concurrent Transaction Processing System** in Java that solves race conditions, ensures atomic ACID transactions, enforces strict object-oriented business domain rules, generates verifiable audit trails, and provides real-time multi-threaded simulation.

---

## 2. Scope of the Project
The scope of this system encompasses:
- **Multi-tier Account Management:** Support for Savings Accounts (with annual interest calculation and minimum balance enforcement) and Current Accounts (with overdraft facilities and credit limits) using polymorphic class hierarchies.
- **Thread-Safe Concurrent Transaction Engine:** High-performance processing of concurrent fund transfers, deposits, and withdrawals using fine-grained locking and synchronized monitors to prevent race conditions.
- **ACID Transaction Management with JDBC:** Atomic execution of credit-debit operations with rollback mechanisms upon partial failures using standard JDBC `PreparedStatement` and connection transactions.
- **Custom Exception Handling:** Structured, domain-specific exception hierarchy (`InsufficientFundsException`, `AccountNotFoundException`, `InvalidTransactionException`, `AuthenticationException`).
- **Data Persistence & Audit Logging:** Persistent relational storage for accounts and ledger transactions, coupled with File I/O streaming for exporting account statements and immutable audit logs.
- **Simulation & Verification Suite:** Built-in multi-threaded stress-test harness simulating simultaneous transactions across parallel threads to verify zero balance discrepancy.

---

## 3. Target Users
1. **Bank Customers / Account Holders:** Individuals performing self-service operations (checking balances, executing fund transfers, viewing transaction history, and exporting monthly statements).
2. **Bank Tellers / Branch Managers:** Administrative staff creating accounts, managing customer profiles, and applying interest or overdraft updates.
3. **System Administrators & Auditors:** Personnel monitoring system health, verifying concurrency logs, tracking transaction audit trails, and ensuring regulatory compliance.

---

## 4. High-Level Features
- **Account Lifecycle & Profile Management:** Registration, KYC validation, account status toggling (`ACTIVE`, `FROZEN`, `CLOSED`), and PIN security.
- **Polymorphic Account Operations:** Dynamic dispatch for interest calculation, transaction fee evaluation, and overdraft limit checks.
- **Concurrent Fund Transfer Engine:** Thread-safe inter-account transfers with deadlock avoidance (ordered lock acquisition).
- **Atomic Database Operations (JDBC):** Parameterized queries preventing SQL injection with full transaction commit/rollback safety.
- **Automated Statement Exporter & Audit Trails:** Character/Byte stream file generation (`.txt`/`.csv`) summarizing date-filtered transactions.
- **Interactive Console Interface & Stress Test Harness:** User-friendly menu-driven CLI alongside a one-click multi-threaded concurrency demonstration.
