package com.vityarthi.banking.service;

import com.vityarthi.banking.model.Account;
import com.vityarthi.banking.util.AuditLogger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency Test & Simulation Engine.
 * Demonstrates Unit 3: Multithreading, Thread Pools (ExecutorService),
 * Concurrency Synchronization, Race Condition Testing, and Thread Safety Verification.
 */
public class ConcurrentTransferEngine {
    private final BankService bankService;

    public ConcurrentTransferEngine(BankService bankService) {
        this.bankService = bankService;
    }

    /**
     * Executes simultaneous concurrent fund transfers between two accounts using multi-threading.
     * Verifies that no money is created or destroyed, proving complete thread safety and absence of race conditions.
     */
    public SimulationResult runStressTest(String acc1Number, String pin1,
                                          String acc2Number, String pin2,
                                          int numberOfThreads, double amountPerTransfer) {
        System.out.println("\n=======================================================");
        System.out.println("   STARTING MULTI-THREADED CONCURRENCY STRESS TEST     ");
        System.out.println("=======================================================");
        System.out.printf(" Threads: %d | Amount Per Txn: ₹%.2f\n", numberOfThreads, amountPerTransfer);

        try {
            Account acc1Before = bankService.getAccount(acc1Number);
            Account acc2Before = bankService.getAccount(acc2Number);
            double initialSum = acc1Before.getBalance() + acc2Before.getBalance();

            System.out.printf(" Initial Balance [%s]: ₹%.2f\n", acc1Number, acc1Before.getBalance());
            System.out.printf(" Initial Balance [%s]: ₹%.2f\n", acc2Number, acc2Before.getBalance());
            System.out.printf(" Initial Total Sum: ₹%.2f\n", initialSum);
            System.out.println("-------------------------------------------------------");

            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch endGate = new CountDownLatch(numberOfThreads);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);

            long startTime = System.currentTimeMillis();

            // Spawn concurrent transfer tasks alternating directions
            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        // Wait for all threads to align and start at the exact same millisecond
                        startGate.await();

                        if (index % 2 == 0) {
                            bankService.transferFunds(acc1Number, acc2Number, amountPerTransfer, pin1, "Concurrent Txn #" + index);
                        } else {
                            bankService.transferFunds(acc2Number, acc1Number, amountPerTransfer, pin2, "Concurrent Txn #" + index);
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failedCount.incrementAndGet();
                        AuditLogger.warn("CONCURRENCY_TEST", "Thread #" + index + " failed: " + e.getMessage());
                    } finally {
                        endGate.countDown();
                    }
                });
            }

            // Release all threads simultaneously
            startGate.countDown();

            // Wait for completion
            boolean finished = endGate.await(15, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;
            executor.shutdown();

            Account acc1After = bankService.getAccount(acc1Number);
            Account acc2After = bankService.getAccount(acc2Number);
            double finalSum = acc1After.getBalance() + acc2After.getBalance();
            double discrepancy = Math.abs(finalSum - initialSum);

            System.out.println("-------------------------------------------------------");
            System.out.println("               STRESS TEST EXECUTION RESULTS           ");
            System.out.println("-------------------------------------------------------");
            System.out.printf(" Completed in      : %d ms\n", duration);
            System.out.printf(" Successful Txns   : %d\n", successCount.get());
            System.out.printf(" Failed/Rejected   : %d\n", failedCount.get());
            System.out.printf(" Final Balance [%s]: ₹%.2f\n", acc1Number, acc1After.getBalance());
            System.out.printf(" Final Balance [%s]: ₹%.2f\n", acc2Number, acc2After.getBalance());
            System.out.printf(" Final Total Sum   : ₹%.2f\n", finalSum);
            System.out.printf(" Balance Delta     : ₹%.4f (%s)\n",
                    discrepancy, (discrepancy < 0.001 ? "PASSED - ZERO RACE CONDITIONS" : "FAILED - RACE CONDITION DETECTED"));
            System.out.println("=======================================================\n");

            return new SimulationResult(numberOfThreads, successCount.get(), failedCount.get(),
                    initialSum, finalSum, duration, discrepancy < 0.001);

        } catch (Exception e) {
            System.err.println("Concurrency simulation failed: " + e.getMessage());
            return new SimulationResult(numberOfThreads, 0, numberOfThreads, 0, 0, 0, false);
        }
    }

    public record SimulationResult(int totalAttempts, int successful, int failed,
                                   double initialSum, double finalSum,
                                   long executionTimeMs, boolean threadSafe) {}
}
