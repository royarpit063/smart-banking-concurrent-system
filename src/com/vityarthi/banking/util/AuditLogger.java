package com.vityarthi.banking.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for persistent thread-safe file audit logging.
 * Demonstrates Unit 3: Multithreaded logging, Unit 4: Character-oriented File I/O Streams (FileWriter, BufferedWriter).
 */
public class AuditLogger {
    private static final String LOG_FILE = "bank_audit.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object FILE_LOCK = new Object();

    public static void log(String level, String action, String details) {
        String logEntry = String.format("[%s] [%s] [Thread: %-15s] [%-6s] %s%n",
                LocalDateTime.now().format(FORMATTER),
                level,
                Thread.currentThread().getName(),
                action,
                details);

        // Synchronized writing to prevent interleaved log lines from concurrent threads
        synchronized (FILE_LOCK) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(logEntry);
            } catch (IOException e) {
                System.err.println("[AuditLogger] Failed to write log: " + e.getMessage());
            }
        }
    }

    public static void info(String action, String details) {
        log("INFO", action, details);
    }

    public static void warn(String action, String details) {
        log("WARN", action, details);
    }

    public static void error(String action, String details) {
        log("ERROR", action, details);
    }
}
