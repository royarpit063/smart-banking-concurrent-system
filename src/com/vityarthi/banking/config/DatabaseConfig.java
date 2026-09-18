package com.vityarthi.banking.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Thread-safe Singleton managing JDBC database configuration and connections.
 * Demonstrates Unit 2: Singleton Pattern, Static methods/initializers,
 * and Unit 5: JDBC Connection, Driver loading, externalized properties.
 */
public class DatabaseConfig {
    private static volatile DatabaseConfig instance;
    private static final Object LOCK = new Object();

    private static final String CONFIG_FILE = "db.properties";
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String driverClassName;

    private DatabaseConfig() {
        loadConfiguration();
        initializeDriver();
        initializeSchema();
    }

    public static DatabaseConfig getInstance() {
        DatabaseConfig result = instance;
        if (result == null) {
            synchronized (LOCK) {
                result = instance;
                if (result == null) {
                    instance = result = new DatabaseConfig();
                }
            }
        }
        return result;
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Warning: Failed to read " + CONFIG_FILE + ", using defaults.");
            }
        } else {
            // Write default externalized configuration for Unit 5 requirement
            props.setProperty("db.url", "jdbc:sqlite:banking_system.db");
            props.setProperty("db.user", "");
            props.setProperty("db.password", "");
            props.setProperty("db.driver", "org.sqlite.JDBC");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Smart Banking System Database Configuration");
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Warning: Could not create default db.properties");
            }
        }

        this.dbUrl = props.getProperty("db.url", "jdbc:sqlite:banking_system.db");
        this.dbUser = props.getProperty("db.user", "");
        this.dbPassword = props.getProperty("db.password", "");
        this.driverClassName = props.getProperty("db.driver", "org.sqlite.JDBC");
    }

    private void initializeDriver() {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConfig] JDBC Driver class not found: " + driverClassName +
                    ". Falling back to in-memory/standard SQLite if driver loaded at runtime.");
        }
    }

    public Connection getConnection() throws SQLException {
        if (dbUser.isEmpty() && dbPassword.isEmpty()) {
            return DriverManager.getConnection(dbUrl);
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private void initializeSchema() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id TEXT PRIMARY KEY,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                address TEXT
            );
        """;

        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                holder_name TEXT NOT NULL,
                account_type TEXT NOT NULL,
                balance REAL NOT NULL,
                pin_hash TEXT NOT NULL,
                overdraft_limit REAL DEFAULT 0.0,
                is_active INTEGER DEFAULT 1,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            );
        """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id TEXT PRIMARY KEY,
                source_account TEXT,
                destination_account TEXT,
                transaction_type TEXT NOT NULL,
                amount REAL NOT NULL,
                balance_after REAL NOT NULL,
                status TEXT NOT NULL,
                remarks TEXT,
                timestamp TEXT NOT NULL
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createAccountsTable);
            stmt.execute(createTransactionsTable);
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Note on Schema Init: " + e.getMessage());
        }
    }
}
