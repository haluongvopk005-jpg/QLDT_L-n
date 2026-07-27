package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.*;
import com.phonemanager.model.*;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;

public class NhapHangPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final NhaCungCapDAO nccDao = new NhaCungCapDAO();
    private final DienThoaiDAO dtDao = new DienThoaiDAO();
    private final NhapHangDAO dao = new NhapHangDAO();
    private final Integer userId;

    private JComboBox<NhaCungCap> cbNcc;
    private JComboBox<DienThoai> cbDt;
    private JTextField fSoLuong, fGiaNhap, fGhiChu;
    private JLabel lbTonKho, lbGiaBan, lbGoiY;
    private DefaultTableModel model;
    private JTable table;

    private final DecimalFormat moneyFmt = new DecimalFormat("#,###");

    public NhapHangPanel(Integer userId) {
        this.userId = userId;
        setLayout(new BorderLayout(18, 18));
        setBackground(AppConfig.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        build();
        updatePhoneInfo();
    }

    private void build() {
        JLabel title = new JLabel("Nhập hàng");
        title.setFont(AppConfig.TITLE);
        title.setForeground(AppConfig.TEXT);

        JLabel subTitle = new JLabel("Tạo phiếu nhập, cập nhật tồn kho và xem lịch sử nhập hàng");
        subTitle.setFont(AppConfig.BODY);
        subTitle.setForeground(AppConfig.MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subTitle);
        top.add(titleBox, BorderLayout.WEST);

        JPanel formCard = createCard();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConfig.BORDER, 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel formTitle = new JLabel("Thông tin phiếu nhập");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(AppConfig.TEXT);

        cbNcc = new JComboBox<>();
        cbDt = new JComboBox<>();
        styleCombo(cbNcc);
        styleCombo(cbDt);
        cbDt.addActionListener(e -> updatePhoneInfo());

        fSoLuong = UIHelper.field();
        fGiaNhap = UIHelper.field();
        fGhiChu = UIHelper.field();
        fSoLuong.setToolTipText("Nhập số lượng nguyên dương, ví dụ: 10");
        fGiaNhap.setToolTipText("Nhập giá nhập mới, ví dụ: 12000000");
        fGhiChu.setToolTipText("Ghi chú có thể bỏ trống");

        lbTonKho = infoLabel("Tồn kho: --");
        lbGiaBan = infoLabel("Giá bán: --");
        lbGoiY = new JLabel("Chọn điện thoại để xem tồn kho và giá bán hiện tại.");
        lbGoiY.setFont(AppConfig.SMALL);
        lbGoiY.setForeground(AppConfig.MUTED);

        JPanel phoneInfo = new JPanel(new GridLayout(1, 2, 10, 0));
        phoneInfo.setOpaque(false);
        phoneInfo.add(infoBox("Tồn kho hiện tại", lbTonKho));
        phoneInfo.add(infoBox("Giá bán hiện tại", lbGiaBan));

        JButton btnLamMoi = UIHelper.gradBtn("Làm mới", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton btnNhap = UIHelper.gradBtn("Nhập hàng", AppConfig.SUCCESS, new Color(0x27AE60));
        btnLamMoi.setPreferredSize(new Dimension(0, 42));
        btnNhap.setPreferredSize(new Dimension(0, 42));
        btnLamMoi.setMinimumSize(new Dimension(120, 42));
        btnNhap.setMinimumSize(new Dimension(120, 42));
        btnLamMoi.setToolTipText("Tải lại nhà cung cấp, điện thoại và lịch sử nhập hàng");
        btnNhap.setToolTipText("Lưu phiếu nhập và tự động tăng tồn kho");

        btnLamMoi.addActionListener(e -> {
            loadCombos();
            loadHistory();
            updatePhoneInfo();
            info("Đã làm mới danh sách nhập hàng!");
        });
        btnNhap.addActionListener(e -> doImport());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnNhap);

        int r = 0;
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = r++;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 14, 0);
        formCard.add(formTitle, g);

        addFormRow(formCard, "Nhà cung cấp *", cbNcc, r++);
        addFormRow(formCard, "Điện thoại *", cbDt, r++);

        g.gridy = r++;
        g.insets = new Insets(2, 0, 12, 0);
        formCard.add(phoneInfo, g);

        addFormRow(formCard, "Số lượng nhập *", fSoLuong, r++);
        addFormRow(formCard, "Giá nhập mới (VNĐ) *", fGiaNhap, r++);
        addFormRow(formCard, "Ghi chú", fGhiChu, r++);

        g.gridy = r++;
        g.insets = new Insets(4, 0, 14, 0);
        formCard.add(lbGoiY, g);

        g.gridy = r;
        g.insets = new Insets(8, 0, 0, 0);
        formCard.add(buttonPanel, g);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(435, 0));
        left.add(formCard, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"STT", "Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Người nhập", "Điện thoại", "SL", "Giá nhập", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model) {
            @Override public String getToolTipText(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row < 0) return null;
                return buildRowTooltip(row);
            }
        };
        UIHelper.styleTable(table);
        table.setRowHeight(40);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setToolTipText("Có thể kéo rộng cột hoặc kéo thanh ngang bên dưới bảng để xem thêm chữ");
        setColumnWidths(table);
        ToolTipManager.sharedInstance().registerComponent(table);
        ToolTipManager.sharedInstance().setInitialDelay(120);
        ToolTipManager.sharedInstance().setDismissDelay(12000);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        sp.getViewport().setBackground(AppConfig.ROW_ODD);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel historyTitle = new JLabel("Lịch sử nhập hàng");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyTitle.setForeground(AppConfig.TEXT);
        JLabel hint = new JLabel("Di chuột vào từng dòng để xem đầy đủ nội dung");
        hint.setFont(AppConfig.SMALL);
        hint.setForeground(AppConfig.MUTED);
        tableHeader.add(historyTitle, BorderLayout.WEST);
        tableHeader.add(hint, BorderLayout.EAST);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConfig.BORDER, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(sp, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(left, BorderLayout.WEST);
        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createCard() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(AppConfig.SURFACE);
        return p;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(AppConfig.TEXT);
        return l;
    }

    private JPanel infoBox(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(true);
        p.setBackground(AppConfig.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppConfig.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(title);
        t.setFont(AppConfig.SMALL);
        t.setForeground(AppConfig.MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void addFormRow(JPanel p, String label, JComponent comp, int row) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        JLabel lb = new JLabel(label);
        lb.setFont(AppConfig.SMALL);
        lb.setForeground(AppConfig.MUTED);
        box.add(lb, BorderLayout.NORTH);
        box.add(comp, BorderLayout.CENTER);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);
        p.add(box, g);
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setFont(AppConfig.BODY);
        cb.setForeground(AppConfig.TEXT);
        cb.setBackground(AppConfig.BG);
        cb.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        cb.setPreferredSize(new Dimension(9999, 38));
        cb.setMaximumSize(new Dimension(9999, 38));
    }

    private void setColumnWidths(JTable t) {
        int[] widths = {55, 150, 145, 230, 170, 280, 70, 130, 140};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = t.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
            col.setMinWidth(i == 0 ? 45 : 70);
        }
    }

    public void loadCombos() {
        UiTaskRunner.run(
                () -> new ImportOptions(
                        nccDao.getAll().stream()
                                .filter(supplier -> "Hoat dong".equals(supplier.getTrangThai()))
                                .toList(),
                        dtDao.getAll().stream()
                                .filter(phone -> !"Ngung KD".equals(phone.getTrangThai()))
                                .toList()),
                options -> {
                    cbNcc.removeAllItems();
                    for (NhaCungCap n : options.suppliers()) cbNcc.addItem(n);
                    cbDt.removeAllItems();
                    for (DienThoai d : options.phones()) cbDt.addItem(d);
                    updatePhoneInfo();
                },
                error -> warn("Lỗi tải danh sách nhập hàng:\n" + error.getMessage())
        );
    }

    public void loadHistory() {
        UiTaskRunner.run(dao::getHistory, rows -> {
            model.setRowCount(0);
            for (Object[] row : rows) model.addRow(row);
        }, error -> warn("Lỗi tải lịch sử nhập hàng:\n" + error.getMessage()));
    }

    private void updatePhoneInfo() {
        DienThoai d = (DienThoai) cbDt.getSelectedItem();
        if (d == null) {
            lbTonKho.setText("Tồn kho: --");
            lbGiaBan.setText("Giá bán: --");
            lbGoiY.setText("Chọn điện thoại để xem tồn kho và giá bán hiện tại.");
            lbGoiY.setForeground(AppConfig.MUTED);
            return;
        }
        lbTonKho.setText(d.getTonKho() + " máy");
        lbGiaBan.setText(moneyFmt.format(d.getGiaBan()) + " VNĐ");
        lbGoiY.setText("Lưu ý: giá nhập không được lớn hơn giá bán để tránh bán lỗ.");
        lbGoiY.setForeground(AppConfig.WARNING);
    }

    private void doImport() {
        NhaCungCap n = (NhaCungCap) cbNcc.getSelectedItem();
        DienThoai d = (DienThoai) cbDt.getSelectedItem();

        if (n == null) {
            warn("Chưa chọn nhà cung cấp!");
            return;
        }
        if (d == null) {
            warn("Chưa chọn điện thoại!");
            return;
        }

        try {
            String soLuongText = fSoLuong.getText().trim();
            String giaNhapText = fGiaNhap.getText().trim();
            if (soLuongText.isEmpty()) {
                warn("Vui lòng nhập số lượng nhập!");
                fSoLuong.requestFocus();
                return;
            }
            if (giaNhapText.isEmpty()) {
                warn("Vui lòng nhập giá nhập!");
                fGiaNhap.requestFocus();
                return;
            }

            int sl = Integer.parseInt(soLuongText);
            long gia = parseMoney(giaNhapText);

            if (sl <= 0) {
                warn("Số lượng nhập phải lớn hơn 0!");
                fSoLuong.requestFocus();
                return;
            }
            if (gia <= 0) {
                warn("Giá nhập phải lớn hơn 0!");
                fGiaNhap.requestFocus();
                return;
            }
            if (gia > d.getGiaBan()) {
                warn("Giá nhập đang lớn hơn giá bán hiện tại.\nHệ thống không cho nhập để tránh bán lỗ.\nVui lòng sửa lại giá bán hoặc giá nhập!");
                fGiaNhap.requestFocus();
                return;
            }

            dao.nhapHang(n.getId(), userId, d.getId(), sl, gia, fGhiChu.getText().trim());
            info("Nhập hàng thành công! Tồn kho đã được tăng.");
            fSoLuong.setText("");
            fGiaNhap.setText("");
            fGhiChu.setText("");
            loadCombos();
            loadHistory();
            updatePhoneInfo();
        } catch (NumberFormatException ex) {
            warn("Số lượng hoặc giá nhập không hợp lệ!\nSố lượng chỉ nhập số nguyên, giá nhập chỉ nhập số.");
        } catch (Exception ex) {
            warn("Lỗi nhập hàng:\n" + ex.getMessage());
        }
    }

    private long parseMoney(String s) {
        String number = s.replaceAll("[^0-9]", "");
        if (number.isEmpty()) throw new NumberFormatException("empty money");
        return Long.parseLong(number);
    }

    private String buildRowTooltip(int viewRow) {
        StringBuilder sb = new StringBuilder("<html><div style='padding:8px; max-width:520px;'>");
        sb.append("<b>Chi tiết phiếu nhập</b><br><br>");
        for (int c = 0; c < table.getColumnCount(); c++) {
            Object header = table.getColumnName(c);
            Object value = table.getValueAt(viewRow, c);
            sb.append("<b>").append(escape(String.valueOf(header))).append(":</b> ")
                    .append(escape(value == null ? "" : String.valueOf(value)))
                    .append("<br>");
        }
        sb.append("</div></html>");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void warn(String s) {
        JOptionPane.showMessageDialog(this, s, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String s) {
        JOptionPane.showMessageDialog(this, s, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private record ImportOptions(List<NhaCungCap> suppliers, List<DienThoai> phones) {
    }
}
