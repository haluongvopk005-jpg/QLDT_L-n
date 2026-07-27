package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NhaCungCapDAO;
import com.phonemanager.model.NhaCungCap;

import javax.swing.*;
import java.awt.*;

public class NhaCungCapFormDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final NhaCungCapDAO dao = new NhaCungCapDAO();
    private final NhaCungCap old;
    private final Runnable onSaved;
    private JTextField fTen, fLienHe, fSdt, fEmail, fDiaChi;
    private JComboBox<String> cbTrangThai;

    public NhaCungCapFormDialog(JFrame owner, NhaCungCap old, Runnable onSaved) {
        super(owner, old == null ? "Thêm nhà cung cấp" : "Sửa nhà cung cấp", true);
        this.old = old; this.onSaved = onSaved;
        setSize(560, 500); setLocationRelativeTo(owner); setLayout(new BorderLayout());
        getContentPane().setBackground(AppConfig.CARD);
        build(); fillOld();
    }
    private void build() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(18, 24, 10, 24));
        fTen = UIHelper.field(); fLienHe = UIHelper.field(); fSdt = UIHelper.field(); fEmail = UIHelper.field(); fDiaChi = UIHelper.field();
        cbTrangThai = UIHelper.combo("Hoat dong", "Ngung hop tac");
        int r=0; UIHelper.formRow(p,"Tên nhà cung cấp *",fTen,r); r+=2; UIHelper.formRow(p,"Người liên hệ",fLienHe,r); r+=2;
        UIHelper.formRow(p,"Số điện thoại",fSdt,r); r+=2; UIHelper.formRow(p,"Email",fEmail,r); r+=2; UIHelper.formRow(p,"Địa chỉ",fDiaChi,r); r+=2; UIHelper.formRow(p,"Trạng thái",cbTrangThai,r);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12)); bottom.setOpaque(false);
        JButton cancel = UIHelper.gradBtn("Hủy", new Color(0x4B4F7A), new Color(0x3A3D66));
        JButton save = UIHelper.gradBtn("Lưu", AppConfig.ACCENT, AppConfig.ACCENT2);
        cancel.addActionListener(e -> dispose()); save.addActionListener(e -> save()); bottom.add(cancel); bottom.add(save);
        add(p, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
    }
    private void fillOld() {
        if (old == null) return;
        fTen.setText(old.getTenNhaCungCap()); fLienHe.setText(old.getNguoiLienHe()); fSdt.setText(old.getSdt());
        fEmail.setText(old.getEmail()); fDiaChi.setText(old.getDiaChi()); cbTrangThai.setSelectedItem(old.getTrangThai());
    }
    private void save() {
        String ten = fTen.getText().trim();
        String sdt = fSdt.getText().trim();
        if (ten.isEmpty()) { warn("Vui lòng nhập tên nhà cung cấp!"); return; }
        if (!sdt.isEmpty() && !isValidPhone(sdt)) { warn("Số điện thoại chỉ được nhập số và phải đúng 9 hoặc 10 chữ số!"); return; }
        try {
            NhaCungCap n = new NhaCungCap(old == null ? 0 : old.getId(), ten, fLienHe.getText().trim(), sdt, fEmail.getText().trim(), fDiaChi.getText().trim(), String.valueOf(cbTrangThai.getSelectedItem()));
            boolean ok = old == null ? dao.insert(n) : dao.update(n);
            if (ok) { if (onSaved != null) onSaved.run(); dispose(); }
        } catch (Exception ex) { warn("Lỗi lưu nhà cung cấp:\n" + ex.getMessage()); }
    }
    private boolean isValidPhone(String sdt) {
        return sdt != null && sdt.matches("\\d{9,10}");
    }

    private void warn(String s) { JOptionPane.showMessageDialog(this, s, "Thông báo", JOptionPane.WARNING_MESSAGE); }
}
