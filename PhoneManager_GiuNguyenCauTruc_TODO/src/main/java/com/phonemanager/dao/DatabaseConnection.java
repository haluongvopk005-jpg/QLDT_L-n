package com.phonemanager.dao;

import com.phonemanager.config.AppConfig;
import java.sql.*;

// ============================================================
//  DatabaseConnection.java — Kết nối SQL Server
// ============================================================
public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Thiếu driver mssql-jdbc trong pom.xml!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.DB_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }

    public static boolean isConnected() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
