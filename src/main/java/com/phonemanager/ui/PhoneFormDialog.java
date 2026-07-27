package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.DienThoaiDAO;
import com.phonemanager.model.DienThoai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// ============================================================
// PhoneFormDialog.java — Dialog Thêm / Sửa Điện Thoại
// Chỉ quản trị viên dùng form này để thêm/sửa điện thoại.
// Nhân viên bán hàng không được vào form vì giá nhập/lợi nhuận là thông tin nhạy cảm.
// ============================================================
public class PhoneFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final DienThoaiDAO dao = new DienThoaiDAO();
    private final DienThoai existing;
    private final Runnable onSaved;
    private final boolean isAdmin;

    private JTextField fTen, fHang, fModel;
    private JTextField fGiaNhap, fGiaBan, fKho, fDaBan, fRam, fMau;
    private JComboBox<String> fTT;
    private JLabel lbLai;

    // Hiển thị có dấu → lưu DB không dấu
    private static final String[] SHOW = {"Còn hàng", "Hết hàng", "Ngừng kinh doanh"};
    private static final String[] SAVE = {"Con hang", "Het hang", "Ngung KD"};

    /** Khởi tạo biểu mẫu với đầy đủ quyền quản trị. */
    public PhoneFormDialog(JFrame owner, DienThoai existing, Runnable onSaved) {
        this(owner, existing, onSaved, true);
    }

    public PhoneFormDialog(JFrame owner, DienThoai existing, Runnable onSaved, boolean isAdmin) {
        super(owner, existing == null ? "Thêm điện thoại" : "Sửa điện thoại", true);
        this.existing = existing;
        this.onSaved = onSaved;
        this.isAdmin = isAdmin;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, isAdmin ? 680 : 650);
        setMinimumSize(new Dimension(500, isAdmin ? 620 : 600));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(AppConfig.CARD);
        setLayout(new BorderLayout());
        buildUI();
        if (existing != null) fillForm();
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(AppConfig.SURFACE);
        hdr.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JLabel title = new JLabel(existing == null ? "Thêm điện thoại mới" : "Chỉnh sửa điện thoại");
        title.setFont(AppConfig.HEADER);
        title.setForeground(AppConfig.TEXT);
        hdr.add(title, BorderLayout.WEST);

        // ── Form ────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppConfig.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(16, 28, 10, 28));

        fTen = UIHelper.field();
        fHang = UIHelper.field();
        fModel = UIHelper.field();
        fGiaNhap = UIHelper.field();
        fGiaBan = UIHelper.field();
        fKho = UIHelper.field();
        fDaBan = UIHelper.field();
        fRam = UIHelper.field();
        fMau = UIHelper.field();
        fTT = UIHelper.combo(SHOW);
        lbLai = new JLabel("Lãi / SP: —");
        lbLai.setFont(AppConfig.SMALL);
        lbLai.setForeground(AppConfig.PROFIT);

        int row = 0;
        row = addFormRow(form, "Tên máy *", fTen, row);
        row = addFormRow(form, "Hãng *", fHang, row);
        row = addFormRow(form, "Model", fModel, row);

        // Giá nhập là tiền vốn, chỉ quản trị viên được xem và chỉnh trong luồng giao diện chính.
        row = addFormRow(form, "Giá nhập (VNĐ) — Tiền vốn", fGiaNhap, row);
        row = addFormRow(form, "Giá bán (VNĐ) — Bán cho khách", fGiaBan, row);
        row = addFormRow(form, "Tồn kho", fKho, row);
        row = addFormRow(form, "Đã bán", fDaBan, row);
        row = addFormRow(form, "RAM", fRam, row);
        row = addFormRow(form, "Màu sắc", fMau, row);
        row = addFormRow(form, "Trạng thái", fTT, row);

        if (isAdmin) {
            KeyAdapter ka = new KeyAdapter() {
                @Override public void keyReleased(KeyEvent e) { calcLai(); }
            };
            fGiaNhap.addKeyListener(ka);
            fGiaBan.addKeyListener(ka);

            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 0, 0, 0);
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            g.gridx = 0;
            g.gridy = row;
            form.add(lbLai, g);
        }

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppConfig.CARD);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ── Buttons ─────────────────────────────────────────────
        JButton btnLuu = UIHelper.gradBtn("Lưu", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton btnHuy = UIHelper.gradBtn("Hủy", new Color(0x444870), new Color(0x333660));
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bot.setBackground(AppConfig.CARD);
        bot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppConfig.BORDER));
        bot.add(btnHuy);
        bot.add(btnLuu);

        add(hdr, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bot, BorderLayout.SOUTH);
    }

    private int addFormRow(JPanel form, String label, JComponent input, int row) {
        UIHelper.formRow(form, label, input, row);
        return row + 2;
    }

    private void calcLai() {
        if (!isAdmin) return;
        try {
            long nhap = plong(fGiaNhap);
            long ban = plong(fGiaBan);
            long lai = ban - nhap;
            double pct = nhap > 0 ? lai * 100.0 / nhap : 0;
            lbLai.setText(String.format("Lãi / SP: %,d đ  (%.1f%%)", lai, pct));
            lbLai.setForeground(lai >= 0 ? AppConfig.PROFIT : AppConfig.DANGER);
        } catch (Exception ignored) {
            lbLai.setText("Lãi / SP: —");
        }
    }

    private void fillForm() {
        fTen.setText(existing.getTenMay());
        fHang.setText(existing.getHang());
        fModel.setText(existing.getModel());
        fGiaNhap.setText(String.valueOf(existing.getGiaNhap()));
        fGiaBan.setText(String.valueOf(existing.getGiaBan()));
        fKho.setText(String.valueOf(existing.getTonKho()));
        fDaBan.setText(String.valueOf(existing.getDaBan()));
        fRam.setText(existing.getRam());
        fMau.setText(existing.getMauSac());
        String tt = DienThoai.luuTrangThai(existing.getTrangThai());
        for (int i = 0; i < SAVE.length; i++) {
            if (SAVE[i].equalsIgnoreCase(tt)) {
                fTT.setSelectedIndex(i);
                break;
            }
        }
        calcLai();
    }

    private void save() {
        try {
            String ten = fTen.getText().trim();
            String hang = fHang.getText().trim();
            if (ten.isEmpty()) {
                msg("Tên máy không được để trống!");
                return;
            }
            if (hang.isEmpty()) {
                msg("Hãng không được để trống!");
                return;
            }

            DienThoai d = new DienThoai();
            d.setTenMay(ten);
            d.setHang(hang);
            d.setModel(fModel.getText().trim());

            long giaNhap = plong(fGiaNhap);
            long giaBan = plong(fGiaBan);
            long tonKho = plong(fKho);
            long daBan = plong(fDaBan);

            if (giaNhap < 0 || giaBan < 0 || tonKho < 0 || daBan < 0) {
                msg("Giá nhập, giá bán, tồn kho và đã bán không được âm!");
                return;
            }
            if (giaBan <= 0) {
                msg("Giá bán phải lớn hơn 0!");
                return;
            }
            if (giaBan < giaNhap) {
                msg("Giá bán không được nhỏ hơn giá nhập để tránh bán lỗ!");
                return;
            }

            d.setGiaNhap(giaNhap);
            d.setGiaBan(giaBan);
            d.setTonKho((int) tonKho);
            d.setDaBan((int) daBan);
            d.setRam(fRam.getText().trim());
            d.setMauSac(fMau.getText().trim());
            d.setTrangThai(SAVE[fTT.getSelectedIndex()]);

            boolean ok;
            if (existing == null) {
                ok = dao.insert(d);
            } else {
                d.setId(existing.getId());
                ok = dao.update(d);
            }

            if (ok) {
                dispose();
                onSaved.run();
                JOptionPane.showMessageDialog(this, "Lưu thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            msg("Giá nhập, giá bán, tồn kho và đã bán phải là số nguyên!");
        } catch (Exception e) {
            msg("Lỗi: " + e.getMessage());
        }
    }

    private long plong(JTextField field) {
        String s = field.getText().replaceAll("[^\\d]", "");
        return s.isEmpty() ? 0 : Long.parseLong(s);
    }

    private void msg(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
}
