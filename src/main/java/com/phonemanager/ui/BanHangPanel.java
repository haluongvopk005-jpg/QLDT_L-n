package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.*;
import com.phonemanager.model.*;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BanHangPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final KhachHangDAO khDao = new KhachHangDAO();
    private final DienThoaiDAO dtDao = new DienThoaiDAO();
    private final BanHangDAO dao = new BanHangDAO();
    private final Integer userId;
    private JComboBox<KhachHang> cbKh;
    private JComboBox<DienThoai> cbDt;
    private JTextField fSoLuong, fDonGia, fGhiChu;
    private JLabel lbTong;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private final List<BanHangDAO.SaleLine> cart = new ArrayList<>();

    public BanHangPanel(Integer userId) {
        this.userId = userId;
        setLayout(new BorderLayout(16,16)); setBackground(AppConfig.BG); setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        build();
    }
    private void build(){
        JLabel title = new JLabel("Bán hàng / Tạo hóa đơn"); title.setFont(AppConfig.TITLE); title.setForeground(AppConfig.TEXT);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false); form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppConfig.BORDER), BorderFactory.createEmptyBorder(18,18,18,18)));
        cbKh = new JComboBox<>(); cbDt = new JComboBox<>(); styleCombo(cbKh); styleCombo(cbDt);
        fSoLuong = UIHelper.field(); fDonGia = UIHelper.field(); fGhiChu = UIHelper.field(); lbTong = new JLabel("0 VNĐ"); lbTong.setFont(new Font("Segoe UI", Font.BOLD, 22)); lbTong.setForeground(AppConfig.SUCCESS);
        JButton add = UIHelper.gradBtn("Thêm vào giỏ", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton del = UIHelper.gradBtn("Xóa dòng", AppConfig.DANGER, new Color(0xC0392B));
        JButton pay = UIHelper.gradBtn("Thanh toán", AppConfig.SUCCESS, new Color(0x27AE60));
        JButton reload = UIHelper.gradBtn("Làm mới", AppConfig.ACCENT, AppConfig.ACCENT2);
        add.addActionListener(e -> addCart()); del.addActionListener(e -> removeCart()); pay.addActionListener(e -> checkout()); reload.addActionListener(e -> loadCombos());
        int r=0; UIHelper.formRow(form,"Khách hàng",cbKh,r); r+=2; UIHelper.formRow(form,"Điện thoại",cbDt,r); r+=2;
        UIHelper.formRow(form,"Số lượng bán *",fSoLuong,r); r+=2; UIHelper.formRow(form,"Đơn giá bán (bỏ trống lấy giá hiện tại)",fDonGia,r); r+=2; UIHelper.formRow(form,"Ghi chú hóa đơn",fGhiChu,r); r+=2;
        GridBagConstraints g=new GridBagConstraints();
        g.gridx=0;
        g.gridy=r;
        g.fill=GridBagConstraints.HORIZONTAL;
        g.weightx=1;

        // Dùng GridLayout 2x2 để nút Thanh toán luôn hiện rõ,
        // tránh lỗi bị khuất nút khi màn hình/form hẹp.
        JPanel buttons=new JPanel(new GridLayout(2,2,8,8));
        buttons.setOpaque(false);
        buttons.add(reload);
        buttons.add(add);
        buttons.add(del);
        buttons.add(pay);
        form.add(buttons,g);
        cartModel = new DefaultTableModel(new String[]{"STT","Tên máy","Số lượng","Đơn giá","Thành tiền"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        cartTable = new JTable(cartModel); cartTable.setName("cart"); UIHelper.styleTable(cartTable);
        JScrollPane sp = new JScrollPane(cartTable); sp.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER)); sp.getViewport().setBackground(AppConfig.ROW_ODD);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10)); south.setOpaque(false); JLabel l=new JLabel("Tổng tiền: "); l.setFont(AppConfig.HEADER); l.setForeground(AppConfig.TEXT); south.add(l); south.add(lbTong);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(title, BorderLayout.WEST);
        JPanel west = new JPanel(new BorderLayout()); west.setOpaque(false); west.setPreferredSize(new Dimension(430,0)); west.add(form, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout()); center.setOpaque(false); center.add(sp, BorderLayout.CENTER); center.add(south, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH); add(west, BorderLayout.WEST); add(center, BorderLayout.CENTER);
    }
    private <T> void styleCombo(JComboBox<T> cb){ cb.setFont(AppConfig.BODY); cb.setForeground(AppConfig.TEXT); cb.setBackground(AppConfig.BG); cb.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER)); cb.setPreferredSize(new Dimension(9999,38)); }
    public void loadCombos(){
        UiTaskRunner.run(
                () -> new SaleOptions(
                        khDao.getAll().stream()
                                .filter(customer -> "Hoat dong".equals(customer.getTrangThai()))
                                .toList(),
                        dtDao.getAll().stream()
                                .filter(phone -> !"Ngung KD".equals(phone.getTrangThai()) && phone.getTonKho() > 0)
                                .toList()),
                options -> {
                    cbKh.removeAllItems();
                    for(KhachHang k:options.customers()) cbKh.addItem(k);
                    cbDt.removeAllItems();
                    for(DienThoai d:options.phones()) cbDt.addItem(d);
                },
                error -> warn("Lỗi tải dữ liệu bán hàng:\n" + error.getMessage())
        );
    }
    private void addCart(){
        DienThoai d=(DienThoai)cbDt.getSelectedItem(); if(d==null){warn("Chưa chọn điện thoại!");return;}
        try{
            int sl=Integer.parseInt(fSoLuong.getText().trim());
            if(sl<=0){warn("Số lượng bán phải lớn hơn 0!");return;}
            if(sl>d.getTonKho()){warn("Tồn kho hiện chỉ còn "+d.getTonKho()+" máy!");return;}
            long gia=fDonGia.getText().trim().isEmpty()?d.getGiaBan():parseMoney(fDonGia.getText());
            if(gia<=0){warn("Đơn giá bán phải lớn hơn 0!");return;}
            if(gia < d.getGiaNhap()){
                warn("Đơn giá bán đang thấp hơn giá vốn. Không thể bán để tránh lỗ.\nVui lòng báo quản trị viên kiểm tra lại giá bán.");
                return;
            }
            BanHangDAO.SaleLine existing = cart.stream()
                    .filter(line -> line.dienThoaiId == d.getId())
                    .findFirst()
                    .orElse(null);
            if(existing != null){
                if(existing.donGiaBan != gia){
                    warn("Điện thoại này đã có trong giỏ với đơn giá khác.\n"
                            + "Hãy xóa dòng cũ trước khi đổi đơn giá.");
                    return;
                }
                long totalQuantity = (long) existing.soLuong + sl;
                if(totalQuantity > d.getTonKho()){
                    warn("Tổng số lượng trong giỏ vượt tồn kho hiện tại (" + d.getTonKho() + " máy)!");
                    return;
                }
                existing.soLuong = (int) totalQuantity;
            }else{
                cart.add(new BanHangDAO.SaleLine(d.getId(), d.getTenMay(), sl, gia, d.getGiaNhap()));
            }
            refreshCart();
            fSoLuong.setText("");
            fDonGia.setText("");
        }catch(NumberFormatException e){warn("Số lượng hoặc đơn giá không hợp lệ!");}
    }
    private void removeCart(){ int row = cartTable.getSelectedRow(); if(row<0){warn("Vui lòng chọn dòng cần xóa trong giỏ hàng!");return;} cart.remove(cartTable.convertRowIndexToModel(row)); refreshCart(); }
    private void refreshCart(){ cartModel.setRowCount(0); long tong=0; int i=1; for(BanHangDAO.SaleLine l:cart){ tong+=l.thanhTien(); cartModel.addRow(new Object[]{i++, l.tenMay, l.soLuong, fmt(l.donGiaBan), fmt(l.thanhTien())}); } lbTong.setText(fmt(tong)+" VNĐ"); }
    private void checkout(){
        if(cart.isEmpty()){
            warn("Giỏ hàng đang trống!\nBạn cần bấm Thêm vào giỏ trước khi thanh toán.");
            return;
        }
        KhachHang kh=(KhachHang)cbKh.getSelectedItem();
        try{
            int id=dao.banHang(kh==null?null:kh.getId(), userId, cart, fGhiChu.getText().trim());
            info("Thanh toán thành công! Mã hóa đơn ID: "+id+
                    "\nHóa đơn đã được lưu vào mục Hóa đơn."
                    + "\nTồn kho và số đã bán đã được cập nhật.");
            cart.clear();
            refreshCart();
            fGhiChu.setText("");
            loadCombos();
        }catch(Exception e){
            warn("Lỗi bán hàng:\n"+e.getMessage());
        }
    }
    private long parseMoney(String s){ return Long.parseLong(s.replaceAll("[^0-9]", "")); }
    private String fmt(long v){ return String.format("%,d",v); }
    private void warn(String s){ JOptionPane.showMessageDialog(this,s,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private void info(String s){ JOptionPane.showMessageDialog(this,s,"Thành công",JOptionPane.INFORMATION_MESSAGE); }

    private record SaleOptions(List<KhachHang> customers, List<DienThoai> phones) {
    }
}
