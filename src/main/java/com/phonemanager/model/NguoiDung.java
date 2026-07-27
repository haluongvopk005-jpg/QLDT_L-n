package com.phonemanager.model;

import java.util.Locale;

// ============================================================
//  NguoiDung.java — Model ánh xạ bảng NguoiDung
// ============================================================
public class NguoiDung {

    private int    id;
    private String username;
    private String matKhau;
    private String hoTen;
    private String vaiTro;      // "admin" hoặc "nhanvien"
    private String email;
    private String sdt;
    private String trangThai;   // "Hoat dong" hoặc "Khoa"

    public NguoiDung(int id, String username, String matKhau,
                     String hoTen, String vaiTro, String email, String trangThai) {
        this(id, username, matKhau, hoTen, vaiTro, email, "", trangThai);
    }

    public NguoiDung(int id, String username, String matKhau,
                     String hoTen, String vaiTro, String email, String sdt, String trangThai) {
        this.id = id; this.username = username; this.matKhau = matKhau;
        this.hoTen = hoTen; this.vaiTro = vaiTro;
        this.email = email; this.sdt = sdt; this.trangThai = trangThai;
    }

    public NguoiDung() {}

    public int    getId()         { return id; }
    public String getUsername()   { return username; }
    public String getMatKhau()    { return matKhau; }
    public String getHoTen()      { return hoTen; }
    public String getVaiTro()     { return vaiTro; }
    public String getEmail()      { return email; }
    public String getSdt()        { return sdt; }
    public String getTrangThai()  { return trangThai; }

    public void setId(int v)               { id = v; }
    public void setUsername(String v)      { username = v; }
    public void setMatKhau(String v)       { matKhau = v; }
    public void setHoTen(String v)         { hoTen = v; }
    public void setVaiTro(String v)        { vaiTro = v; }
    public void setEmail(String v)         { email = v; }
    public void setSdt(String v)           { sdt = v; }
    public void setTrangThai(String v)     { trangThai = v; }

    public static String hienThiVaiTro(String value) {
        if (value == null) return "";
        String s = value.trim();
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "admin" -> "Quản trị viên";
            case "nhanvien", "nhân viên" -> "Nhân viên";
            default -> s;
        };
    }

    public static String hienThiTrangThai(String value) {
        if (value == null) return "";
        String s = value.trim();
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "hoat dong", "hoạt động" -> "Hoạt động";
            case "khoa", "khóa" -> "Khóa";
            default -> s;
        };
    }

    public Object[] toRow() {
        return new Object[]{
            id, username, hoTen, hienThiVaiTro(vaiTro), email, sdt, hienThiTrangThai(trangThai)
        };
    }
}
