package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.DienThoaiDAO;
import com.phonemanager.model.DienThoai;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

// ============================================================
// PhonePanel.java — Quản lý điện thoại theo vai trò đăng nhập
// Cột _ID chỉ giữ nội bộ để sửa/xóa. Người dùng chỉ thấy STT 1,2,3...
// ============================================================
public class PhonePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int COL_INTERNAL_ID = 0;
    private static final int COL_STT = 1;
    private static final int COL_TEN = 2;
    private static final int COL_HANG = 3;
    private static final int COL_MODEL = 4;
    private static final int COL_GIA_NHAP = 5;
    private static final int COL_GIA_BAN = 6;
    private static final int COL_TON_KHO = 7;
    private static final int COL_DA_BAN = 8;
    private static final int COL_RAM = 9;
    private static final int COL_MAU_SAC = 10;
    private static final int COL_TRANG_THAI = 11;

    private static final String[] TABLE_COLUMNS = {
            "_ID", "STT", "Tên máy", "Hãng", "Model", "Giá nhập (VNĐ)",
            "Giá bán (VNĐ)", "Tồn kho", "Đã bán", "RAM", "Màu sắc", "Trạng thái"
    };

    private static final int[] ADMIN_PDF_COLUMNS = {
            COL_STT, COL_TEN, COL_HANG, COL_MODEL, COL_GIA_NHAP, COL_GIA_BAN,
            COL_TON_KHO, COL_DA_BAN, COL_RAM, COL_MAU_SAC, COL_TRANG_THAI
    };

    private static final int[] EMPLOYEE_PDF_COLUMNS = {
            COL_STT, COL_TEN, COL_HANG, COL_MODEL, COL_GIA_BAN,
            COL_TON_KHO, COL_DA_BAN, COL_RAM, COL_MAU_SAC, COL_TRANG_THAI
    };

    private final boolean isAdmin;
    private final JFrame owner;
    private final DienThoaiDAO dao = new DienThoaiDAO();
    private DefaultTableModel model;
    private JTable table;
    private JTextField searchFld;

    public PhonePanel(JFrame owner, boolean isAdmin) {
        this.owner = owner;
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        buildUI();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        JLabel title = new JLabel(isAdmin ? "Danh sách điện thoại" : "Danh sách điện thoại (chỉ xem)");
        title.setFont(AppConfig.HEADER);
        title.setForeground(AppConfig.TEXT);
        top.add(title, BorderLayout.WEST);

        searchFld = UIHelper.field();
        searchFld.setPreferredSize(new Dimension(260, 36));
        JButton btnTim = UIHelper.gradBtn("Tìm kiếm", AppConfig.ACCENT, AppConfig.ACCENT2);
        btnTim.addActionListener(e -> search(searchFld.getText()));
        searchFld.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                search(searchFld.getText());
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(searchFld);
        right.add(btnTim);
        top.add(right, BorderLayout.EAST);

        model = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable();

        // Không hiển thị khóa chính database. STT được tính lại theo dòng đang thấy.
        removeTableColumn(COL_INTERNAL_ID);
        setMaxWidth(COL_STT, 55);
        setMaxWidth(COL_TON_KHO, 75);
        setMaxWidth(COL_DA_BAN, 75);

        // Nhân viên không xem giá nhập trong danh sách và trong PDF.
        if (!isAdmin) {
            removeTableColumn(COL_GIA_NHAP);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        scroll.getViewport().setBackground(AppConfig.ROW_ODD);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);
        JButton btnThem = UIHelper.gradBtn("Thêm", AppConfig.SUCCESS, new Color(0x27AE60));
        JButton btnSua = UIHelper.gradBtn("Sửa", new Color(0xF39C12), new Color(0xE67E22));
        JButton btnXoa = UIHelper.gradBtn("Ngừng kinh doanh", AppConfig.DANGER, new Color(0xC0392B));
        JButton btnTai = UIHelper.gradBtn("Tải lại", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton btnPdf = UIHelper.gradBtn("Xuất PDF", new Color(0x8E44AD), new Color(0x6C3483));

        btnThem.addActionListener(e -> new PhoneFormDialog(owner, null, this::loadData, isAdmin).setVisible(true));
        btnSua.addActionListener(e -> doEdit());
        btnXoa.addActionListener(e -> doDelete());
        btnTai.addActionListener(e -> loadData());
        btnPdf.addActionListener(e -> doPdf());

        // Quản trị viên được thêm/sửa/ngừng kinh doanh. Nhân viên chỉ xem, tìm kiếm và xuất danh sách không có giá nhập.
        if (isAdmin) {
            toolbar.add(btnThem);
            toolbar.add(btnSua);
            toolbar.add(btnXoa);
        }
        toolbar.add(btnTai);
        toolbar.add(btnPdf);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
    }

    private void styleTable() {
        table.setFont(AppConfig.BODY);
        table.setBackground(AppConfig.ROW_EVEN);
        table.setRowHeight(36);
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
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, selected, focused, row, column);
                label.setFont(AppConfig.BODY);
                int modelColumn = tbl.convertColumnIndexToModel(column);

                // STT luôn hiển thị 1,2,3... theo thứ tự/sắp xếp/lọc hiện tại.
                if (modelColumn == COL_STT) {
                    label.setText(String.valueOf(row + 1));
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                } else if (modelColumn == COL_TON_KHO || modelColumn == COL_DA_BAN) {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (!selected) {
                    label.setBackground(row % 2 == 0 ? AppConfig.ROW_EVEN : AppConfig.ROW_ODD);
                    String text = value == null ? "" : value.toString();
                    if (modelColumn == COL_GIA_NHAP) label.setForeground(AppConfig.COST);
                    else if (modelColumn == COL_GIA_BAN) label.setForeground(AppConfig.REVENUE);
                    else if (text.contains("Het") || text.contains("Hết")) label.setForeground(AppConfig.DANGER);
                    else if (text.contains("Con") || text.contains("Còn")) label.setForeground(AppConfig.SUCCESS);
                    else if (text.contains("Ngung") || text.contains("Ngừng")) label.setForeground(AppConfig.WARNING);
                    else label.setForeground(AppConfig.TEXT);
                }
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return label;
            }
        });
    }

    private void removeTableColumn(int modelIndex) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getModelIndex() == modelIndex) {
                columnModel.removeColumn(column);
                return;
            }
        }
    }

    private void setMaxWidth(int modelIndex, int width) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getModelIndex() == modelIndex) {
                column.setMinWidth(40);
                column.setMaxWidth(width);
                column.setPreferredWidth(width);
                return;
            }
        }
    }

    public void loadData() {
        UiTaskRunner.run(dao::getAll, phones -> {
            model.setRowCount(0);
            for (DienThoai d : phones) {
                model.addRow(toInternalRow(d));
            }
        }, e -> {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }

    private Object[] toInternalRow(DienThoai d) {
        return new Object[]{
                d.getId(), "", d.getTenMay(), d.getHang(), d.getModel(),
                String.format("%,d", d.getGiaNhap()), String.format("%,d", d.getGiaBan()),
                d.getTonKho(), d.getDaBan(), d.getRam(), d.getMauSac(),
                DienThoai.hienThiTrangThai(d.getTrangThai())
        };
    }

    private void search(String keyword) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        String term = keyword == null ? "" : keyword.trim();
        sorter.setRowFilter(term.isBlank() ? null : RowFilter.regexFilter("(?iu)" + Pattern.quote(term)));
    }

    private void doEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            warn("Vui lòng chọn dòng cần sửa!");
            return;
        }
        new PhoneFormDialog(owner, getDT(viewRow), this::loadData, isAdmin).setVisible(true);
    }

    private void doDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            warn("Vui lòng chọn điện thoại cần ngừng kinh doanh!");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = pint(modelRow, COL_INTERNAL_ID);
        int ok = JOptionPane.showConfirmDialog(this,
                "Chuyển điện thoại ở dòng STT " + (viewRow + 1) + " sang trạng thái Ngừng kinh doanh?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            if (dao.delete(id)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Đã chuyển sang trạng thái Ngừng kinh doanh!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            warn("Lỗi: " + e.getMessage());
        }
    }

    private void doPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(isAdmin ? "DanhSachDienThoai.pdf" : "DanhSachDienThoai_NhanVien.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = chooser.getSelectedFile();
        if (!out.getName().toLowerCase().endsWith(".pdf")) out = new File(out.getPath() + ".pdf");

        try {
            int[] exportColumns = isAdmin ? ADMIN_PDF_COLUMNS : EMPLOYEE_PDF_COLUMNS;
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document(
                    com.itextpdf.text.PageSize.A4.rotate(), 24, 24, 32, 24);
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new FileOutputStream(out));
            doc.open();

            com.itextpdf.text.Font titleFont = PdfFonts.bold(16,
                    new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7));
            com.itextpdf.text.Font subFont = PdfFonts.regular(8, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Font headFont = PdfFonts.bold(8, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font dataFont = PdfFonts.regular(8);

            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "DANH SÁCH ĐIỆN THOẠI", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new com.itextpdf.text.Paragraph(
                    "Ngày xuất: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), subFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable pdfTable =
                    new com.itextpdf.text.pdf.PdfPTable(exportColumns.length);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(isAdmin
                    ? new float[]{2.5f, 8f, 5f, 6f, 7f, 7f, 4.5f, 4.5f, 4f, 6f, 6f}
                    : new float[]{2.5f, 8f, 5f, 6f, 7f, 4.5f, 4.5f, 4f, 6f, 6f});

            com.itextpdf.text.BaseColor headerBg = new com.itextpdf.text.BaseColor(0x4F, 0x8E, 0xF7);
            for (int modelColumn : exportColumns) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(TABLE_COLUMNS[modelColumn], headFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                cell.setPadding(5);
                pdfTable.addCell(cell);
            }

            for (int row = 0; row < model.getRowCount(); row++) {
                com.itextpdf.text.BaseColor rowBg = row % 2 == 0
                        ? com.itextpdf.text.BaseColor.WHITE
                        : new com.itextpdf.text.BaseColor(0xF0, 0xF4, 0xFF);
                for (int modelColumn : exportColumns) {
                    Object value = modelColumn == COL_STT ? row + 1 : model.getValueAt(row, modelColumn);
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                            new com.itextpdf.text.Phrase(value == null ? "" : value.toString(), dataFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(4);
                    cell.setHorizontalAlignment(isCenterColumn(modelColumn)
                            ? com.itextpdf.text.Element.ALIGN_CENTER
                            : com.itextpdf.text.Element.ALIGN_LEFT);
                    pdfTable.addCell(cell);
                }
            }
            doc.add(pdfTable);
            doc.add(new com.itextpdf.text.Paragraph(
                    "\nTổng số: " + model.getRowCount() + " bản ghi", subFont));
            doc.close();

            int open = JOptionPane.showConfirmDialog(this,
                    "Xuất PDF thành công!\n" + out.getAbsolutePath() + "\nMở file?",
                    "Thành công", JOptionPane.YES_NO_OPTION);
            if (open == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(out);
            }
        } catch (Exception e) {
            warn("Lỗi PDF: " + e.getMessage());
        }
    }

    private boolean isCenterColumn(int modelColumn) {
        return modelColumn == COL_STT || modelColumn == COL_TON_KHO || modelColumn == COL_DA_BAN;
    }

    private DienThoai getDT(int viewRow) {
        int modelRow = table.convertRowIndexToModel(viewRow);
        DienThoai d = new DienThoai();
        d.setId(pint(modelRow, COL_INTERNAL_ID));
        d.setTenMay(str(modelRow, COL_TEN));
        d.setHang(str(modelRow, COL_HANG));
        d.setModel(str(modelRow, COL_MODEL));
        d.setGiaNhap(plng(modelRow, COL_GIA_NHAP));
        d.setGiaBan(plng(modelRow, COL_GIA_BAN));
        d.setTonKho(pint(modelRow, COL_TON_KHO));
        d.setDaBan(pint(modelRow, COL_DA_BAN));
        d.setRam(str(modelRow, COL_RAM));
        d.setMauSac(str(modelRow, COL_MAU_SAC));
        d.setTrangThai(str(modelRow, COL_TRANG_THAI));
        return d;
    }

    private String str(int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private long plng(int row, int column) {
        String value = str(row, column).replaceAll("[^\\d]", "");
        return value.isEmpty() ? 0 : Long.parseLong(value);
    }

    private int pint(int row, int column) {
        String value = str(row, column).replaceAll("[^\\d]", "");
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
}
