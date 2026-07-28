package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.BaoCaoDAO;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// ============================================================
// BaoCaoPanel.java — Báo cáo theo vai trò đăng nhập
// Quản trị viên: xem toàn bộ doanh thu, vốn và lợi nhuận.
// Nhân viên: chỉ xem thông tin sản phẩm đã bán, không có số liệu tài chính.
// ============================================================
public class BaoCaoPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String[] ADMIN_SP_COLUMNS = {
            "STT", "Tên máy", "Hãng", "Giá nhập", "Giá bán",
            "Lãi/SP", "Tồn kho", "Đã bán", "Doanh thu", "Tổng vốn", "Tổng lãi", "Tỷ lệ lãi"
    };

    // Nhân viên chỉ xem thông tin bán hàng cơ bản, không có giá vốn hoặc lãi/SP.
    private static final String[] EMPLOYEE_SP_COLUMNS = {
            "STT", "Tên máy", "Hãng", "Giá bán", "Đã bán"
    };

    private static final String[] ADMIN_BRAND_COLUMNS = {
            "Hãng", "Số SP", "Tồn kho", "Đã bán", "Doanh thu", "Tổng vốn", "Tổng Lợi Nhuận"
    };

    private static final String[] EMPLOYEE_BRAND_COLUMNS = {
            "Hãng", "Số SP", "Đã bán"
    };

    // Các vị trí dữ liệu được nhân viên phép xem từ kết quả DAO.
    // Bỏ Giá nhập (3), Lãi/SP (5), Tồn kho, Doanh thu, Tổng vốn, Tổng lãi, Tỷ lệ lãi.
    private static final int[] EMPLOYEE_SP_INDEXES = {0, 1, 2, 4, 7};
    private static final int[] EMPLOYEE_BRAND_INDEXES = {0, 1, 3};

    private final BaoCaoDAO dao = new BaoCaoDAO();
    private final boolean isAdmin;

    private JLabel lbDT, lbVon, lbLai, lbSoBan, lbTonKho;
    private DefaultTableModel modelSP;
    private DefaultTableModel modelHang;

    /**
     * @param isAdmin true: quản trị viên xem đầy đủ;
     *                false: nhân viên không xem doanh thu, vốn, lãi và tỷ lệ lãi.
     */
    public BaoCaoPanel(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel(isAdmin
                ? "Báo cáo doanh thu và lợi nhuận"
                : "Báo cáo sản phẩm đã bán");
        title.setFont(AppConfig.TITLE);
        title.setForeground(AppConfig.TEXT);

        JButton btnRef = UIHelper.gradBtn("Làm mới", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton btnPdf = UIHelper.gradBtn("Xuất PDF", new Color(0x8E44AD), new Color(0x6C3483));
        btnRef.addActionListener(e -> loadData());
        btnPdf.addActionListener(e -> exportPdf());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(btnRef);
        btns.add(btnPdf);
        top.add(title, BorderLayout.WEST);
        top.add(btns, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppConfig.BODY);
        tabs.addTab("  Chi tiết sản phẩm  ", buildTabSP());
        tabs.addTab("  Theo hãng  ", buildTabHang());

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(top);

        // Nhân viên không được hiển thị các thẻ tổng doanh thu, vốn, lợi nhuận, SP đã bán.
        if (isAdmin) {
            north.add(buildSummary());
        }

        add(north, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSummary() {
        JPanel summary = new JPanel(new GridLayout(1, 5, 14, 0));
        summary.setOpaque(false);
        summary.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        lbDT = numLbl();
        lbVon = numLbl();
        lbLai = numLbl();
        lbSoBan = numLbl();
        lbTonKho = numLbl();

        summary.add(sCard("Tổng doanh thu", lbDT, AppConfig.REVENUE));
        summary.add(sCard("Tổng vốn nhập", lbVon, AppConfig.COST));
        summary.add(sCard("Tổng lợi nhuận", lbLai, AppConfig.PROFIT));
        summary.add(sCard("Sản phẩm đã bán", lbSoBan, AppConfig.ACCENT));
        summary.add(sCard("Tồn kho", lbTonKho, new Color(0x26A69A)));
        return summary;
    }

    private JPanel buildTabSP() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConfig.BG);

        modelSP = new DefaultTableModel(isAdmin ? ADMIN_SP_COLUMNS : EMPLOYEE_SP_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(modelSP);
        styleTable(table, true);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        scrollPane.getViewport().setBackground(AppConfig.ROW_ODD);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTabHang() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppConfig.BG);

        modelHang = new DefaultTableModel(isAdmin ? ADMIN_BRAND_COLUMNS : EMPLOYEE_BRAND_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(modelHang);
        styleTable(table, false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        scrollPane.getViewport().setBackground(AppConfig.ROW_ODD);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void loadData() {
        UiTaskRunner.run(
                () -> new ReportData(
                        isAdmin ? dao.getTongHopTuHoaDon() : new long[0],
                        dao.getBaoCaoChiTietTuHoaDon(),
                        dao.getBaoCaoTheoHangTuHoaDon()),
                data -> {
                    // Chỉ quản trị viên mới lấy và hiển thị các số liệu doanh thu / vốn / lợi nhuận.
                    if (isAdmin) {
                        long[] d = data.summary();
                        lbDT.setText(fmtTien(d[0]));
                        lbVon.setText(fmtTien(d[1]));
                        lbLai.setText(fmtTien(d[2]));
                        lbSoBan.setText(String.format("%,d", d[3]));
                        lbTonKho.setText(String.format("%,d", d.length > 4 ? d[4] : 0));
                    }

                    modelSP.setRowCount(0);
                    for (Object[] row : data.productRows()) {
                        modelSP.addRow(isAdmin ? row : selectColumns(row, EMPLOYEE_SP_INDEXES));
                    }

                    modelHang.setRowCount(0);
                    for (Object[] row : data.brandRows()) {
                        modelHang.addRow(isAdmin ? row : selectColumns(row, EMPLOYEE_BRAND_INDEXES));
                    }
                },
                e -> {
                    JOptionPane.showMessageDialog(this, "Lỗi tải báo cáo:\n" + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
        );
    }

    private Object[] selectColumns(Object[] source, int[] indexes) {
        Object[] result = new Object[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = source[indexes[i]];
        }
        return result;
    }

    // ── Xuất PDF ───────────────────────────────────────────────
    private void exportPdf() {
        JFileChooser fc = new JFileChooser();
        String prefix = isAdmin ? "BaoCaoDoanhThu_" : "BaoCaoBanHang_";
        fc.setSelectedFile(new File(prefix + new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File out = fc.getSelectedFile();
        if (!out.getName().toLowerCase().endsWith(".pdf")) {
            out = new File(out.getPath() + ".pdf");
        }

        try {
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(
                    com.itextpdf.text.PageSize.A4.rotate(), 28, 28, 36, 28);
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new FileOutputStream(out));
            doc.open();

            com.itextpdf.text.Font fTitle = PdfFonts.bold(16,
                    new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7));
            com.itextpdf.text.Font fSub = PdfFonts.regular(8, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font fHead = PdfFonts.bold(8, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fData = PdfFonts.regular(8);
            com.itextpdf.text.Font fGreen = PdfFonts.bold(8,
                    new com.itextpdf.text.BaseColor(0x00, 0xD2, 0xA0));
            com.itextpdf.text.Font fWhite = PdfFonts.regular(8, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fWhiteBold = PdfFonts.bold(13, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fSection = PdfFonts.bold(11);

            String reportTitle = isAdmin
                    ? "BÁO CÁO DOANH THU & LỢI NHUẬN"
                    : "BÁO CÁO SẢN PHẨM ĐÃ BÁN";
            com.itextpdf.text.Paragraph pTitle =
                    new com.itextpdf.text.Paragraph(reportTitle, fTitle);
            pTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            doc.add(pTitle);
            doc.add(new com.itextpdf.text.Paragraph(
                    "Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), fSub));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            // Nhân viên không xuất bảng tổng doanh thu, vốn, lợi nhuận và SP đã bán.
            if (isAdmin) {
                addAdminSummary(doc, fWhite, fWhiteBold);
            }

            doc.add(new com.itextpdf.text.Paragraph("CHI TIẾT TỪNG SẢN PHẨM", fSection));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            addProductTableToPdf(doc, fHead, fData, fGreen);
            doc.add(new com.itextpdf.text.Paragraph(" "));

            doc.add(new com.itextpdf.text.Paragraph("PHÂN TÍCH THEO HÃNG", fSection));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            addBrandTableToPdf(doc, fHead, fData, fGreen);

            doc.close();

            int open = JOptionPane.showConfirmDialog(this,
                    "Xuất PDF thành công!\n" + out.getAbsolutePath() + "\nMở file?",
                    "Thành công", JOptionPane.YES_NO_OPTION);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(out);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất PDF:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addAdminSummary(com.itextpdf.text.Document doc,
                                 com.itextpdf.text.Font fWhite,
                                 com.itextpdf.text.Font fWhiteBold) throws Exception {
        com.itextpdf.text.pdf.PdfPTable sumTbl =
                new com.itextpdf.text.pdf.PdfPTable(5);
        sumTbl.setWidthPercentage(100);
        sumTbl.setSpacingAfter(14);

        String[] labels = {"Tổng doanh thu", "Tổng vốn nhập", "Tổng lợi nhuận", "SP đã bán", "Tồn kho"};
        String[] values = {lbDT.getText(), lbVon.getText(), lbLai.getText(), lbSoBan.getText(), lbTonKho.getText()};
        com.itextpdf.text.BaseColor[] colors = {
                new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7),
                new com.itextpdf.text.BaseColor(0xFF, 0xB7, 0x4D),
                new com.itextpdf.text.BaseColor(0x00, 0xD2, 0xA0),
                new com.itextpdf.text.BaseColor(0x7C, 0x5C, 0xFC),
                new com.itextpdf.text.BaseColor(0x26, 0xA6, 0x9A)
        };

        for (int i = 0; i < labels.length; i++) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell();
            cell.setBackgroundColor(colors[i]);
            cell.setPadding(10);
            cell.addElement(new com.itextpdf.text.Paragraph(labels[i], fWhite));
            cell.addElement(new com.itextpdf.text.Paragraph(values[i], fWhiteBold));
            sumTbl.addCell(cell);
        }
        doc.add(sumTbl);
    }

    private void addProductTableToPdf(com.itextpdf.text.Document doc,
                                      com.itextpdf.text.Font fHead,
                                      com.itextpdf.text.Font fData,
                                      com.itextpdf.text.Font fGreen) throws Exception {
        com.itextpdf.text.pdf.PdfPTable table =
                new com.itextpdf.text.pdf.PdfPTable(modelSP.getColumnCount());
        table.setWidthPercentage(100);
        table.setWidths(isAdmin
                ? new float[]{2, 8, 5, 7, 7, 6, 4, 4, 8, 8, 8, 5}
                : new float[]{2, 10, 7, 8, 5});

        com.itextpdf.text.BaseColor headerBg = new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7);
        for (int column = 0; column < modelSP.getColumnCount(); column++) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(modelSP.getColumnName(column), fHead));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (int row = 0; row < modelSP.getRowCount(); row++) {
            com.itextpdf.text.BaseColor rowBg = row % 2 == 0
                    ? com.itextpdf.text.BaseColor.WHITE
                    : new com.itextpdf.text.BaseColor(0xF0, 0xF4, 0xFF);
            for (int column = 0; column < modelSP.getColumnCount(); column++) {
                String header = modelSP.getColumnName(column);
                Object value = "STT".equals(header) ? row + 1 : modelSP.getValueAt(row, column);
                com.itextpdf.text.Font font = isProfitColumn(header) ? fGreen : fData;
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(value == null ? "" : value.toString(), font));
                cell.setBackgroundColor(rowBg);
                cell.setPadding(4);
                cell.setHorizontalAlignment(pdfAlignment(header));
                table.addCell(cell);
            }
        }
        doc.add(table);
    }

    private void addBrandTableToPdf(com.itextpdf.text.Document doc,
                                    com.itextpdf.text.Font fHead,
                                    com.itextpdf.text.Font fData,
                                    com.itextpdf.text.Font fGreen) throws Exception {
        com.itextpdf.text.pdf.PdfPTable table =
                new com.itextpdf.text.pdf.PdfPTable(modelHang.getColumnCount());
        table.setWidthPercentage(isAdmin ? 82 : 52);
        table.setWidths(isAdmin
                ? new float[]{10, 5, 6, 6, 10, 10, 11}
                : new float[]{12, 6, 7});

        com.itextpdf.text.BaseColor headerBg = new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7);
        for (int column = 0; column < modelHang.getColumnCount(); column++) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase(modelHang.getColumnName(column), fHead));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (int row = 0; row < modelHang.getRowCount(); row++) {
            com.itextpdf.text.BaseColor rowBg = row % 2 == 0
                    ? com.itextpdf.text.BaseColor.WHITE
                    : new com.itextpdf.text.BaseColor(0xF0, 0xF4, 0xFF);
            for (int column = 0; column < modelHang.getColumnCount(); column++) {
                String header = modelHang.getColumnName(column);
                Object value = modelHang.getValueAt(row, column);
                com.itextpdf.text.Font font = isProfitColumn(header) ? fGreen : fData;
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(value == null ? "" : value.toString(), font));
                cell.setBackgroundColor(rowBg);
                cell.setPadding(4);
                cell.setHorizontalAlignment(pdfAlignment(header));
                table.addCell(cell);
            }
        }
        doc.add(table);
    }

    private int pdfAlignment(String header) {
        if ("Tên máy".equals(header) || "Hãng".equals(header)) {
            return com.itextpdf.text.Element.ALIGN_LEFT;
        }
        if ("STT".equals(header) || "Đã bán".equals(header) || "Số SP".equals(header) || "Tồn kho".equals(header)) {
            return com.itextpdf.text.Element.ALIGN_CENTER;
        }
        return com.itextpdf.text.Element.ALIGN_RIGHT;
    }

    // ── Giao diện ──────────────────────────────────────────────
    private JPanel sCard(String title, JLabel value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel text = new JLabel(title);
        text.setFont(AppConfig.SMALL);
        text.setForeground(AppConfig.MUTED);
        text.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        card.add(text);
        card.add(Box.createVerticalStrut(3));
        card.add(value);
        return card;
    }

    private void styleTable(JTable table, boolean isProductTable) {
        table.setFont(AppConfig.BODY);
        table.setBackground(AppConfig.ROW_EVEN);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(AppConfig.ROW_SEL);
        table.setSelectionForeground(AppConfig.TEXT);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(AppConfig.HEADER);
        header.setForeground(AppConfig.TEXT);
        header.setBackground(AppConfig.CARD);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppConfig.ACCENT));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean selected, boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        tbl, value, selected, focused, row, column);
                component.setFont(AppConfig.BODY);
                String columnName = tbl.getColumnName(column);
                if ("STT".equals(columnName)) {
                    ((JLabel) component).setText(String.valueOf(row + 1));
                    ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
                }

                if (!selected) {
                    component.setBackground(row % 2 == 0 ? AppConfig.ROW_EVEN : AppConfig.ROW_ODD);
                    if (isProfitColumn(columnName)) {
                        component.setForeground(AppConfig.PROFIT);
                    } else if (isCostColumn(columnName)) {
                        component.setForeground(AppConfig.COST);
                    } else if ("Doanh thu".equals(columnName)) {
                        component.setForeground(AppConfig.REVENUE);
                    } else {
                        component.setForeground(AppConfig.TEXT);
                    }
                }
                ((JLabel) component).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return component;
            }
        });
    }

    private boolean isProfitColumn(String header) {
        return "Lãi/SP".equals(header)
                || "Tổng lãi".equals(header)
                || "Tỷ lệ lãi".equals(header)
                || "Tổng Lợi Nhuận".equals(header);
    }

    private boolean isCostColumn(String header) {
        return "Giá nhập".equals(header) || "Tổng vốn".equals(header);
    }

    private JLabel numLbl() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(AppConfig.TEXT);
        return label;
    }

    private String fmtTien(long value) {
        if (value >= 1_000_000_000) return String.format("%.1f tỷ", value / 1_000_000_000.0);
        if (value >= 1_000_000) return String.format("%.0f triệu", value / 1_000_000.0);
        return String.format("%,d", value);
    }

    private record ReportData(
            long[] summary,
            List<Object[]> productRows,
            List<Object[]> brandRows) {
    }
}
