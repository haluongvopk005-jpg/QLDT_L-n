package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.KhachHangDAO;
import com.phonemanager.model.KhachHang;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class KhachHangPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JFrame owner;
    private final KhachHangDAO dao = new KhachHangDAO();
    private DefaultTableModel model;
    private JTable table;
    private JTextField search;
    private List<KhachHang> current;
    private static final String[] COLS = {"_ID", "STT", "Họ tên", "SĐT", "Email", "Địa chỉ", "Trạng thái"};

    public KhachHangPanel(JFrame owner) { this.owner = owner; setLayout(new BorderLayout()); setBackground(AppConfig.BG); setBorder(BorderFactory.createEmptyBorder(22,22,22,22)); build(); }
    private void build() {
        JLabel title = new JLabel("Quản lý khách hàng"); title.setFont(AppConfig.TITLE); title.setForeground(AppConfig.TEXT);
        JButton add = UIHelper.gradBtn("Thêm", AppConfig.SUCCESS, new Color(0x27AE60));
        JButton edit = UIHelper.gradBtn("Sửa", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton del = UIHelper.gradBtn("Khóa", AppConfig.DANGER, new Color(0xC0392B));
        JButton refresh = UIHelper.gradBtn("Tải lại", AppConfig.ACCENT, AppConfig.ACCENT2);
        search = UIHelper.field(); search.setPreferredSize(new Dimension(260,38)); search.setMaximumSize(new Dimension(260,38));
        JButton find = UIHelper.gradBtn("Tìm", new Color(0x8E44AD), new Color(0x6C3483));
        add.addActionListener(e -> new KhachHangFormDialog(owner, null, () -> { loadData(); info("Thêm khách hàng thành công!"); }).setVisible(true));
        edit.addActionListener(e -> doEdit()); del.addActionListener(e -> doDelete()); refresh.addActionListener(e -> loadData()); find.addActionListener(e -> searchData());
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(title, BorderLayout.WEST);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); tools.setOpaque(false); tools.add(search); tools.add(find); tools.add(add); tools.add(edit); tools.add(del); tools.add(refresh); top.add(tools, BorderLayout.EAST);
        model = new DefaultTableModel(COLS,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        table = new JTable(model); UIHelper.styleTable(table); hideCol(0); setWidth(1,55); serialRenderer();
        JScrollPane sp = new JScrollPane(table); sp.setBorder(BorderFactory.createLineBorder(AppConfig.BORDER)); sp.getViewport().setBackground(AppConfig.ROW_ODD);
        add(top, BorderLayout.NORTH); add(sp, BorderLayout.CENTER);
    }
    public void loadData(){
        UiTaskRunner.run(dao::getAll, list -> {
            fill(list);
            search.setText("");
        }, error -> warn("Lỗi tải khách hàng:\n" + error.getMessage()));
    }
    private void searchData(){
        String keyword = search.getText();
        UiTaskRunner.run(() -> dao.search(keyword), this::fill,
                error -> warn("Lỗi tìm kiếm:\n" + error.getMessage()));
    }
    private void fill(List<KhachHang> list){ current=list; model.setRowCount(0); for(KhachHang k:list) model.addRow(new Object[]{k.getId(),"",k.getHoTen(),k.getSdt(),k.getEmail(),k.getDiaChi(),k.getTrangThai()}); }
    private KhachHang selected(){ int v=table.getSelectedRow(); if(v<0){warn("Vui lòng chọn một khách hàng!"); return null;} int m=table.convertRowIndexToModel(v); int id=(int)model.getValueAt(m,0); return current.stream().filter(k -> k.getId() == id).findFirst().orElse(null); }
    private void doEdit(){ KhachHang k=selected(); if(k!=null) new KhachHangFormDialog(owner,k,()->{loadData(); info("Cập nhật khách hàng thành công!");}).setVisible(true); }
    private void doDelete(){ KhachHang k=selected(); if(k==null) return; if(JOptionPane.showConfirmDialog(this,"Khóa khách hàng: "+k.getHoTen()+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return; try{ dao.delete(k.getId()); loadData(); info("Đã khóa khách hàng!"); }catch(Exception e){ warn("Không thể khóa khách hàng.\n"+e.getMessage()); } }
    private void hideCol(int idx){ TableColumn c=findCol(idx); if(c!=null)table.getColumnModel().removeColumn(c); }
    private void setWidth(int idx,int w){ TableColumn c=findCol(idx); if(c!=null){c.setMinWidth(40);c.setPreferredWidth(w);c.setMaxWidth(w);} }
    private TableColumn findCol(int idx){ for(int i=0;i<table.getColumnModel().getColumnCount();i++){TableColumn c=table.getColumnModel().getColumn(i); if(c.getModelIndex()==idx)return c;} return null; }
    private void serialRenderer(){ TableColumn c=findCol(1); if(c==null)return; c.setCellRenderer(new DefaultTableCellRenderer(){@Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int col){JLabel l=(JLabel)super.getTableCellRendererComponent(t,String.valueOf(r+1),s,f,r,col); l.setHorizontalAlignment(SwingConstants.CENTER); return l;}}); }
    private void warn(String s){ JOptionPane.showMessageDialog(this,s,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private void info(String s){ JOptionPane.showMessageDialog(this,s,"Thành công",JOptionPane.INFORMATION_MESSAGE); }
}
