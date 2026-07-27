package com.phonemanager.config;

import java.awt.*;

// ============================================================
//  AppConfig.java — Cấu hình toàn bộ ứng dụng
// ============================================================
public final class AppConfig {

    private AppConfig() {
    }

    // KẾT NỐI SQL SERVER
    public static final String DB_HOST     = env("PHONE_MANAGER_DB_HOST", "localhost");
    public static final String DB_PORT     = env("PHONE_MANAGER_DB_PORT", "1433");
    public static final String DB_NAME     = env("PHONE_MANAGER_DB_NAME", "PhoneManagerDB");
    public static final String DB_USER     = env("PHONE_MANAGER_DB_USER", "sa");
    public static final String DB_PASSWORD = env("PHONE_MANAGER_DB_PASSWORD", "123456");

    public static final String DB_URL =
        "jdbc:sqlserver://" + DB_HOST + ":" + DB_PORT
        + ";databaseName=" + DB_NAME
        + ";encrypt=false;trustServerCertificate=true"
        + ";applicationName=PhoneManager";

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    // BẢNG MÀU
    public static final Color BG       = new Color(0x0F1117);
    public static final Color SURFACE  = new Color(0x1A1D27);
    public static final Color CARD     = new Color(0x22263A);
    public static final Color BORDER   = new Color(0x2E3354);
    public static final Color ACCENT   = new Color(0x4F8EF7);
    public static final Color ACCENT2  = new Color(0x7C5CFC);
    public static final Color SUCCESS  = new Color(0x2ECC87);
    public static final Color DANGER   = new Color(0xFF5C7A);
    public static final Color WARNING  = new Color(0xFFB347);
    public static final Color TEXT     = new Color(0xE8EAF6);
    public static final Color MUTED    = new Color(0x7B82A8);
    public static final Color ROW_ODD  = new Color(0x1E2235);
    public static final Color ROW_EVEN = new Color(0x1A1D2A);
    public static final Color ROW_SEL  = new Color(0x2B3568);
    public static final Color PROFIT   = new Color(0x00D2A0);
    public static final Color REVENUE  = new Color(0x4FC3F7);
    public static final Color COST     = new Color(0xFFB74D);

    // FONT
    public static final Font TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font HEADER = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font BTN    = new Font("Segoe UI", Font.BOLD,  12);
}
