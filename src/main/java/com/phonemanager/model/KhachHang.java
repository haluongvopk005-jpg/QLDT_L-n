package com.phonemanager.model;

public class KhachHang {
    private int id;
    private String hoTen;
    private String sdt;
    private String email;
    private String diaChi;
    private String trangThai;

    public KhachHang() {}
    public KhachHang(int id, String hoTen, String sdt, String email, String diaChi, String trangThai) {
        this.id = id; this.hoTen = hoTen; this.sdt = sdt; this.email = email; this.diaChi = diaChi; this.trangThai = trangThai;
    }
    public int getId() { return id; }
    public String getHoTen() { return hoTen; }
    public String getSdt() { return sdt; }
    public String getEmail() { return email; }
    public String getDiaChi() { return diaChi; }
    public String getTrangThai() { return trangThai; }
    public void setId(int id) { this.id = id; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setEmail(String email) { this.email = email; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    @Override public String toString() { return hoTen + " - " + sdt; }
}
