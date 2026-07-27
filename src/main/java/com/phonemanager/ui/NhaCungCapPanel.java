package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.NhaCungCapDAO;
import com.phonemanager.model.NhaCungCap;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class NhaCungCapPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JFrame owner;
    private final NhaCungCapDAO dao = new NhaCungCapDAO();
    private DefaultTableModel model;
    private JTable table;
    private JTextField search;
    private List<NhaCungCap> current;
    private static final String[] COLS = {"_ID", "STT", "Tên nhà cung cấp", "Người liên hệ", "SĐT", "Email", "Địa chỉ", "Trạng thái"};

    public NhaCungCapPanel(JFrame owner) { this.owner = owner; setLayout(new BorderLayout()); setBackground(AppConfig.BG); setBorder(BorderFactory.createEmptyBorder(22,22,22,22)); build(); }
    private void build() {
        JLabel title = new JLabel("Quản lý nhà cung cấp"); title.setFont(AppConfig.TITLE); title.setForeground(AppConfig.TEXT);
        JButton add = UIHelper.gradBtn("Thêm", AppConfig.SUCCESS, new Color(0x27AE60));
        JButton edit = UIHelper.gradBtn("Sửa", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton del = UIHelper.gradBtn("Ngừng hợp tác", AppConfig.DANGER, new Color(0xC0392B));
        JButton refresh = UIHelper.gradBtn("Tải lại", AppConfig.ACCENT, AppConfig.ACCENT2);
        search = UIHelper.field(); search.setPreferredSize(new Dimension(260,38)); search.setMaximumSize(new Dimension(260,38));
        JButton find = UIHelper.gradBtn("Tìm", new Color(0x8E44AD), new Color(0x6C3483));
        add.addActionListener(e -> new NhaCungCapFormDialog(owner, null, () -> { loadData(); info("Thêm nhà cung cấp thành công!"); }).setVisible(true));
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
        }, error -> warn("Lỗi tải nhà cung cấp:\n" + error.getMessage()));
    }
    private void searchData(){
        String keyword = search.getText();
        UiTaskRunner.run(() -> dao.search(keyword), this::fill,
                error -> warn("Lỗi tìm kiếm:\n" + error.getMessage()));
    }
    private void fill(List<NhaCungCap> list){ current=list; model.setRowCount(0); for(NhaCungCap n:list) model.addRow(new Object[]{n.getId(),"",n.getTenNhaCungCap(),n.getNguoiLienHe(),n.getSdt(),n.getEmail(),n.getDiaChi(),n.getTrangThai()}); }
    private NhaCungCap selected(){ int v=table.getSelectedRow(); if(v<0){warn("Vui lòng chọn một nhà cung cấp!"); return null;} int m=table.convertRowIndexToModel(v); int id=(int)model.getValueAt(m,0); return current.stream().filter(n -> n.getId() == id).findFirst().orElse(null); }
    private void doEdit(){ NhaCungCap n=selected(); if(n!=null) new NhaCungCapFormDialog(owner,n,()->{loadData(); info("Cập nhật nhà cung cấp thành công!");}).setVisible(true); }
    private void doDelete(){ NhaCungCap n=selected(); if(n==null) return; if(JOptionPane.showConfirmDialog(this,"Ngừng hợp tác với nhà cung cấp: "+n.getTenNhaCungCap()+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return; try{ dao.delete(n.getId()); loadData(); info("Đã chuyển nhà cung cấp sang trạng thái Ngừng hợp tác!"); }catch(Exception e){ warn("Không thể cập nhật nhà cung cấp.\n"+e.getMessage()); } }
    private void hideCol(int idx){ TableColumn c=findCol(idx); if(c!=null)table.getColumnModel().removeColumn(c); }
    private void setWidth(int idx,int w){ TableColumn c=findCol(idx); if(c!=null){c.setMinWidth(40);c.setPreferredWidth(w);c.setMaxWidth(w);} }
    private TableColumn findCol(int idx){ for(int i=0;i<table.getColumnModel().getColumnCount();i++){TableColumn c=table.getColumnModel().getColumn(i); if(c.getModelIndex()==idx)return c;} return null; }
    private void serialRenderer(){ TableColumn c=findCol(1); if(c==null)return; c.setCellRenderer(new DefaultTableCellRenderer(){@Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int col){JLabel l=(JLabel)super.getTableCellRendererComponent(t,String.valueOf(r+1),s,f,r,col); l.setHorizontalAlignment(SwingConstants.CENTER); return l;}}); }
    private void warn(String s){ JOptionPane.showMessageDialog(this,s,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private void info(String s){ JOptionPane.showMessageDialog(this,s,"Thành công",JOptionPane.INFORMATION_MESSAGE); }
}
