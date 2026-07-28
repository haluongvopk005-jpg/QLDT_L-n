package com.phonemanager.ui;

import com.phonemanager.config.AppConfig;
import com.phonemanager.dao.HoaDonDAO;
import com.phonemanager.model.HoaDon;
import com.phonemanager.util.UiTaskRunner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HoaDonPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final HoaDonDAO dao = new HoaDonDAO();
    private JTable tableHD;
    private DefaultTableModel modelHD, modelCT;
    private final List<HoaDon> invoices = new ArrayList<>();

    public HoaDonPanel() { setLayout(new BorderLayout(12,12)); setBackground(AppConfig.BG); setBorder(BorderFactory.createEmptyBorder(22,22,22,22)); build(); }
    private void build(){
        JLabel title = new JLabel("Lịch sử hóa đơn"); title.setFont(AppConfig.TITLE); title.setForeground(AppConfig.TEXT);
        JButton refresh = UIHelper.gradBtn("Tải lại", AppConfig.ACCENT, AppConfig.ACCENT2);
        JButton pdf = UIHelper.gradBtn("Xuất PDF hóa đơn", new Color(0x8E44AD), new Color(0x6C3483));
        refresh.addActionListener(e -> loadData()); pdf.addActionListener(e -> exportInvoice());
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(title, BorderLayout.WEST);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); tools.setOpaque(false); tools.add(pdf); tools.add(refresh); top.add(tools, BorderLayout.EAST);
        modelHD = new DefaultTableModel(new String[]{"STT","Mã hóa đơn","Ngày bán","Khách hàng","Nhân viên","Tổng tiền","Trạng thái"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        tableHD = new JTable(modelHD); UIHelper.styleTable(tableHD);
        // Khi chọn hóa đơn, chi tiết sẽ tự hiện ở bảng dưới.
        tableHD.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableHD.getSelectedRow() >= 0) {
                loadDetails();
            }
        });
        JScrollPane spHD = new JScrollPane(tableHD); spHD.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(AppConfig.BORDER),"Danh sách hóa đơn")); spHD.getViewport().setBackground(AppConfig.ROW_ODD);
        modelCT = new DefaultTableModel(new String[]{"STT","Tên máy","Hãng","SL","Đơn giá","Thành tiền"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tableCT = new JTable(modelCT); UIHelper.styleTable(tableCT);
        JScrollPane spCT = new JScrollPane(tableCT); spCT.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(AppConfig.BORDER),"Chi tiết hóa đơn")); spCT.getViewport().setBackground(AppConfig.ROW_ODD);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spHD, spCT); split.setResizeWeight(0.58); split.setBorder(null); split.setOpaque(false);
        add(top, BorderLayout.NORTH); add(split, BorderLayout.CENTER);
    }
    public void loadData(){
        UiTaskRunner.run(dao::getAll, loadedInvoices -> {
            invoices.clear();
            invoices.addAll(loadedInvoices);
            modelHD.setRowCount(0);
            int stt=1;
            for(HoaDon h:invoices){
                modelHD.addRow(new Object[]{stt++,h.getMaHoaDon(),dao.formatDate(h.getNgayBan()),h.getKhachHang(),h.getNhanVien(),fmt(h.getTongTien()),h.getTrangThai()});
            }
            modelCT.setRowCount(0);

            // Sau khi tải lại, tự chọn hóa đơn mới nhất để bảng chi tiết hiện luôn.
            if(modelHD.getRowCount() > 0){
                tableHD.setRowSelectionInterval(0,0);
            }
        }, error -> warn("Lỗi tải hóa đơn:\n" + error.getMessage()));
    }
    private HoaDon selected(){ int row=tableHD.getSelectedRow(); if(row<0){warn("Vui lòng chọn hóa đơn!"); return null;} int m=tableHD.convertRowIndexToModel(row); if(m<0||m>=invoices.size()) return null; return invoices.get(m); }
    private void loadDetails(){ HoaDon h=selected(); if(h==null)return; UiTaskRunner.run(() -> dao.getDetails(h.getId()), rows -> { modelCT.setRowCount(0); for(Object[] row:rows) modelCT.addRow(row); }, error -> warn("Lỗi tải chi tiết hóa đơn:\n" + error.getMessage())); }
    private void exportInvoice(){ HoaDon h=selected(); if(h==null)return; try{
        List<Object[]> details = dao.getDetails(h.getId());
        JFileChooser fc = new JFileChooser(); fc.setSelectedFile(new File("HoaDon_" + h.getMaHoaDon() + ".pdf")); if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File out=fc.getSelectedFile(); if(!out.getName().toLowerCase().endsWith(".pdf")) out=new File(out.getPath()+".pdf");
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36,36,36,36);
        com.itextpdf.text.pdf.PdfWriter.getInstance(doc,new FileOutputStream(out)); doc.open();
        com.itextpdf.text.Font titleF=PdfFonts.bold(16,new com.itextpdf.text.BaseColor(0x4F,0x8E,0xF7));
        com.itextpdf.text.Font normal=PdfFonts.regular(10); com.itextpdf.text.Font head=PdfFonts.bold(10, com.itextpdf.text.BaseColor.WHITE);
        doc.add(new com.itextpdf.text.Paragraph("HÓA ĐƠN BÁN HÀNG", titleF));
        doc.add(new com.itextpdf.text.Paragraph("Mã hóa đơn: " + h.getMaHoaDon(), normal));
        doc.add(new com.itextpdf.text.Paragraph("Ngày bán: " + dao.formatDate(h.getNgayBan()), normal));
        doc.add(new com.itextpdf.text.Paragraph("Khách hàng: " + h.getKhachHang(), normal));
        doc.add(new com.itextpdf.text.Paragraph("Nhân viên: " + h.getNhanVien(), normal));
        doc.add(new com.itextpdf.text.Paragraph(" "));
        com.itextpdf.text.pdf.PdfPTable tb = new com.itextpdf.text.pdf.PdfPTable(6); tb.setWidthPercentage(100); tb.setWidths(new float[]{1,4,2,1,2,2});
        for(String c:new String[]{"STT","Tên máy","Hãng","SL","Đơn giá","Thành tiền"}){ com.itextpdf.text.pdf.PdfPCell cell=new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(c,head)); cell.setBackgroundColor(new com.itextpdf.text.BaseColor(0x4F,0x8E,0xF7)); cell.setPadding(6); tb.addCell(cell); }
        for(Object[] r:details){ for(Object v:r){ com.itextpdf.text.pdf.PdfPCell cell=new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(v),normal)); cell.setPadding(5); tb.addCell(cell); } }
        doc.add(tb); doc.add(new com.itextpdf.text.Paragraph("Tổng tiền: " + fmt(h.getTongTien()) + " VNĐ", PdfFonts.bold(12))); doc.close();
        int open=JOptionPane.showConfirmDialog(this,"Xuất PDF thành công!\n"+out.getAbsolutePath()+"\nBạn có muốn mở file không?","Thành công",JOptionPane.YES_NO_OPTION); if(open==JOptionPane.YES_OPTION&&Desktop.isDesktopSupported()) Desktop.getDesktop().open(out);
    }catch(Exception e){warn("Lỗi xuất PDF hóa đơn:\n"+e.getMessage());}
    }
    private String fmt(long v){return String.format("%,d",v);} private void warn(String s){JOptionPane.showMessageDialog(this,s,"Thông báo",JOptionPane.WARNING_MESSAGE);} }
