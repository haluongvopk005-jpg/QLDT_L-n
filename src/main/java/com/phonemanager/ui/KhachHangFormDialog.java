package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.KhachHangDAO;
import com.phonemanager.model.KhachHang;

import javax.swing.*;
import java.awt.*;

public class KhachHangFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final KhachHangDAO dao = new KhachHangDAO();
    private final KhachHang old;
    private final Runnable onSaved;
    private JTextField fTen, fSdt, fEmail, fDiaChi;
    private JComboBox<String> cbTrangThai;

    public KhachHangFormDialog(JFrame owner, KhachHang old, Runnable onSaved) {
        super(owner, old == null ? "Thêm khách hàng" : "Sửa khách hàng", true);
        this.old = old; this.onSaved = onSaved;
        setSize(520, 430); setLocationRelativeTo(owner); setLayout(new BorderLayout());
        getContentPane().setBackground(AppConfig.CARD);
        build(); fillOld();
    }
    private void build() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(18, 24, 10, 24));
        fTen = UIHelper.field(); fSdt = UIHelper.field(); fEmail = UIHelper.field(); fDiaChi = UIHelper.field();
        cbTrangThai = UIHelper.combo("Hoat dong", "Khoa");
        int r=0; UIHelper.formRow(p,"Họ tên *",fTen,r); r+=2; UIHelper.formRow(p,"Số điện thoại *",fSdt,r); r+=2;
        UIHelper.formRow(p,"Email",fEmail,r); r+=2; UIHelper.formRow(p,"Địa chỉ",fDiaChi,r); r+=2; UIHelper.formRow(p,"Trạng thái",cbTrangThai,r);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); bottom.setOpaque(false);
        JButton cancel = UIHelper.gradBtn("Hủy", new Color(0x4B4F7A), new Color(0x3A3D66));
        JButton save = UIHelper.gradBtn("Lưu", AppConfig.ACCENT, AppConfig.ACCENT2);
        cancel.addActionListener(e -> dispose()); save.addActionListener(e -> save()); bottom.add(cancel); bottom.add(save);
        add(p, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
    }
    private void fillOld() {
        if (old == null) return;
        fTen.setText(old.getHoTen()); fSdt.setText(old.getSdt()); fEmail.setText(old.getEmail()); fDiaChi.setText(old.getDiaChi());
        cbTrangThai.setSelectedItem(old.getTrangThai());
    }
    private void save() {
        String ten = fTen.getText().trim(), sdt = fSdt.getText().trim();
        if (ten.isEmpty() || sdt.isEmpty()) { warn("Vui lòng nhập họ tên và số điện thoại!"); return; }
        if (!isValidPhone(sdt)) { warn("Số điện thoại chỉ được nhập số và phải đúng 9 hoặc 10 chữ số!"); return; }
        try {
            KhachHang k = new KhachHang(old == null ? 0 : old.getId(), ten, sdt, fEmail.getText().trim(), fDiaChi.getText().trim(), String.valueOf(cbTrangThai.getSelectedItem()));
            boolean ok = old == null ? dao.insert(k) : dao.update(k);
            if (ok) { if (onSaved != null) onSaved.run(); dispose(); }
        } catch (Exception ex) { warn("Lỗi lưu khách hàng:\n" + ex.getMessage()); }
    }
    private boolean isValidPhone(String sdt) {
        return sdt != null && sdt.matches("\\d{9,10}");
    }

    private void warn(String s) { JOptionPane.showMessageDialog(this, s, "Thông báo", JOptionPane.WARNING_MESSAGE); }
}
