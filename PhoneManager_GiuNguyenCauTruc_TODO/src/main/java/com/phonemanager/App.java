package com.phonemanager;

import com.phonemanager.dao.DatabaseConnection;
import com.phonemanager.ui.MainFrame;
import javax.swing.*;
import java.util.concurrent.CompletableFuture;

// ============================================================
//  App.java — Điểm khởi chạy ứng dụng
//  Khởi tạo giao diện và chuẩn bị kết nối cơ sở dữ liệu
// ============================================================
public class App {

    public static void main(String[] args) {

        // Đặt look-and-feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Mở cửa sổ chính trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // Chuẩn bị driver và SQL Server ở luồng nền, không chặn cửa sổ đăng nhập.
        CompletableFuture.runAsync(DatabaseConnection::isConnected);
    }
}
