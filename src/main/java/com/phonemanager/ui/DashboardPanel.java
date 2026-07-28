package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.DienThoaiDAO;
import com.phonemanager.dao.BaoCaoDAO;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import java.awt.*;

// ============================================================
// DashboardPanel.java — Bảng điều khiển tổng quan
// ============================================================
public class DashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final DienThoaiDAO dao = new DienThoaiDAO();
    private final BaoCaoDAO baoCaoDAO = new BaoCaoDAO();
    private JLabel lbTong, lbCon, lbHet, lbKho, lbDT, lbVon, lbLai;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG);
        setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Bảng điều khiển tổng quan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(AppConfig.TEXT);

        JButton btnLamMoi = UIHelper.gradBtn("Làm mới", AppConfig.ACCENT, AppConfig.ACCENT2);
        btnLamMoi.addActionListener(e -> refresh());

        header.add(title, BorderLayout.WEST);
        header.add(btnLamMoi, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel khoHang = sectionLabel("Kho hàng");
        JPanel hangKho = row(4);
        lbTong = numberLabel();
        lbCon = numberLabel();
        lbHet = numberLabel();
        lbKho = numberLabel();

        hangKho.add(card("Tổng sản phẩm", lbTong, AppConfig.ACCENT));
        hangKho.add(card("Còn hàng", lbCon, AppConfig.SUCCESS));
        hangKho.add(card("Hết hàng", lbHet, AppConfig.DANGER));
        hangKho.add(card("Tổng tồn kho", lbKho, AppConfig.WARNING));

        JLabel doanhThu = sectionLabel("Doanh thu và lợi nhuận");
        JPanel hangDoanhThu = row(3);
        lbDT = numberLabel();
        lbVon = numberLabel();
        lbLai = numberLabel();

        hangDoanhThu.add(card("Tổng doanh thu", lbDT, AppConfig.REVENUE));
        hangDoanhThu.add(card("Tổng vốn nhập", lbVon, AppConfig.COST));
        hangDoanhThu.add(card("Tổng lợi nhuận", lbLai, AppConfig.PROFIT));

        body.add(khoHang);
        body.add(Box.createVerticalStrut(10));
        body.add(hangKho);
        body.add(Box.createVerticalStrut(22));
        body.add(doanhThu);
        body.add(Box.createVerticalStrut(10));
        body.add(hangDoanhThu);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void refresh() {
        UiTaskRunner.run(
                () -> new DashboardData(dao.getStatistics(), baoCaoDAO.getTongHopTuHoaDon()),
                data -> {
                    int[] statistics = data.statistics();
                    long[] doanhThu = data.revenue();
                    lbTong.setText(String.valueOf(statistics[0]));
                    lbCon.setText(String.valueOf(statistics[1]));
                    lbHet.setText(String.valueOf(statistics[2]));
                    lbKho.setText(String.format("%,d", statistics[3]));
                    lbDT.setText(dinhDangTien(doanhThu[0]));
                    lbVon.setText(dinhDangTien(doanhThu[1]));
                    lbLai.setText(dinhDangTien(doanhThu[2]));
                },
                error -> lbTong.setText("Lỗi")
        );
    }

    private JPanel card(String title, JLabel value, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppConfig.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(accent);
                g2.fillRect(0, 0, 4, getHeight());
                g2.setColor(AppConfig.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(22, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppConfig.SMALL);
        titleLabel.setForeground(AppConfig.MUTED);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(value);
        return card;
    }

    private JPanel row(int columns) {
        JPanel panel = new JPanel(new GridLayout(1, columns, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(9999, 120));
        return panel;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConfig.HEADER);
        label.setForeground(AppConfig.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JLabel numberLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(AppConfig.TEXT);
        return label;
    }

    private String dinhDangTien(long value) {
        if (value >= 1_000_000_000) return String.format("%.1f tỷ", value / 1_000_000_000.0);
        if (value >= 1_000_000) return String.format("%.0f triệu", value / 1_000_000.0);
        return String.format("%,d", value);
    }

    private record DashboardData(int[] statistics, long[] revenue) {
    }
}
