package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NguoiDungDAO;
import com.phonemanager.model.NguoiDung;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

// ============================================================
// UserPanel.java — Quản lý Người Dùng (Admin)
// Khóa chính database được ẩn; cột STT luôn hiển thị 1,2,3...
// ============================================================
public class UserPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final int COL_INTERNAL_ID = 0;
    private static final int COL_STT = 1;
    private static final int COL_USERNAME = 2;
    private static final int COL_HOTEN = 3;
    private static final int COL_VAITRO = 4;
    private static final int COL_EMAIL = 5;
    private static final int COL_SDT = 6;
    private static final int COL_TRANGTHAI = 7;

    private static final String[] TABLE_COLUMNS = {
        "_ID", "STT", "Tên đăng nhập", "Họ tên", "Vai trò", "Email", "SĐT", "Trạng thái"
    };

    private final JFrame owner;
    private final String currentUser;
    private final NguoiDungDAO dao = new NguoiDungDAO();
    private DefaultTableModel model;
    private JTable table;

    public UserPanel(JFrame owner, String currentUser) {
        this.owner = owner;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppConfig.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Quản lý người dùng");
        title.setFont(AppConfig.HEADER);
        title.setForeground(AppConfig.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        model = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        removeTableColumn(COL_INTERNAL_ID);
        setMaxWidth(COL_STT, 55);
        setSerialNumberRenderer();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER));
        scroll.getViewport().setBackground(AppConfig.ROW_ODD);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setOpaque(false);
        JButton btnThem = UIHelper.gradBtn("Thêm người dùng", AppConfig.SUCCESS, new Color(0x27AE60));
        JButton btnXoa = UIHelper.gradBtn("Khóa tài khoản", AppConfig.DANGER, new Color(0xC0392B));
        JButton btnTai = UIHelper.gradBtn("Tải lại", AppConfig.ACCENT, AppConfig.ACCENT2);
        btnThem.addActionListener(e -> new UserFormDialog(owner, () -> {
            loadData();
            info("Thêm người dùng thành công!");
        }).setVisible(true));
        btnXoa.addActionListener(e -> doDelete());
        btnTai.addActionListener(e -> loadData());
        toolbar.add(btnThem);
        toolbar.add(btnXoa);
        toolbar.add(btnTai);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
    }

    private void setSerialNumberRenderer() {
        TableColumn sttColumn = findColumn(COL_STT);
        if (sttColumn == null) return;
        sttColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    tbl, String.valueOf(row + 1), selected, focused, row, column);
                label.setFont(AppConfig.BODY);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) {
                    label.setBackground(row % 2 == 0 ? AppConfig.ROW_EVEN : AppConfig.ROW_ODD);
                    label.setForeground(AppConfig.TEXT);
                }
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return label;
            }
        });
    }

    private void removeTableColumn(int modelIndex) {
        TableColumn column = findColumn(modelIndex);
        if (column != null) table.getColumnModel().removeColumn(column);
    }

    private void setMaxWidth(int modelIndex, int width) {
        TableColumn column = findColumn(modelIndex);
        if (column != null) {
            column.setMinWidth(40);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        }
    }

    private TableColumn findColumn(int modelIndex) {
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < columns.getColumnCount(); i++) {
            TableColumn column = columns.getColumn(i);
            if (column.getModelIndex() == modelIndex) return column;
        }
        return null;
    }

    public void loadData() {
        UiTaskRunner.run(dao::getAll, users -> {
            model.setRowCount(0);
            for (NguoiDung user : users) {
                model.addRow(new Object[]{
                    user.getId(), "", user.getUsername(), user.getHoTen(),
                    NguoiDung.hienThiVaiTro(user.getVaiTro()), user.getEmail(), user.getSdt(),
                    NguoiDung.hienThiTrangThai(user.getTrangThai())
                });
            }
        }, e -> {
            warn("Lỗi tải danh sách:\n" + e.getMessage());
        });
    }

    private void doDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            warn("Vui lòng chọn tài khoản cần khóa!");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) model.getValueAt(modelRow, COL_INTERNAL_ID);
        String username = String.valueOf(model.getValueAt(modelRow, COL_USERNAME));
        String fullName = String.valueOf(model.getValueAt(modelRow, COL_HOTEN));
        if (username.equals(currentUser)) {
            warn("Không thể khóa tài khoản đang đăng nhập!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Xác nhận khóa tài khoản ở dòng STT " + (viewRow + 1) + ":\n"
                + "Tài khoản: " + username + "\nHọ tên: " + fullName,
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            if (dao.delete(id)) {
                loadData();
                info("Đã khóa tài khoản \"" + fullName + "\" thành công!");
            }
        } catch (Exception e) {
            warn("Lỗi: " + e.getMessage());
        }
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }
}
