package com.phonemanager.model;

import java.util.Date;

public class HoaDon {
    private int id;
    private String maHoaDon;
    private String khachHang;
    private String nhanVien;
    private Date ngayBan;
    private long tongTien;
    private String ghiChu;
    private String trangThai;

    public HoaDon(int id, String maHoaDon, String khachHang, String nhanVien, Date ngayBan, long tongTien, String ghiChu, String trangThai) {
        this.id = id; this.maHoaDon = maHoaDon; this.khachHang = khachHang; this.nhanVien = nhanVien;
        this.ngayBan = ngayBan; this.tongTien = tongTien; this.ghiChu = ghiChu; this.trangThai = trangThai;
    }
    public int getId() { return id; }
    public String getMaHoaDon() { return maHoaDon; }
    public String getKhachHang() { return khachHang; }
    public String getNhanVien() { return nhanVien; }
    public Date getNgayBan() { return ngayBan; }
    public long getTongTien() { return tongTien; }
    public String getGhiChu() { return ghiChu; }
    public String getTrangThai() { return trangThai; }
}
