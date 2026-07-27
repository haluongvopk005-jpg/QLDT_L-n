
package com.phonemanager.model;

public class NhaCungCap {
    private int id;
    private String tenNhaCungCap;
    private String nguoiLienHe;
    private String sdt;
    private String email;
    private String diaChi;
    private String trangThai;

    public NhaCungCap() {}
    public NhaCungCap(int id, String tenNhaCungCap, String nguoiLienHe, String sdt, String email, String diaChi, String trangThai) {
        this.id = id; this.tenNhaCungCap = tenNhaCungCap; this.nguoiLienHe = nguoiLienHe;
        this.sdt = sdt; this.email = email; this.diaChi = diaChi; this.trangThai = trangThai;
    }
    public int getId() { return id; }
    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public String getNguoiLienHe() { return nguoiLienHe; }
    public String getSdt() { return sdt; }
    public String getEmail() { return email; }
    public String getDiaChi() { return diaChi; }
    public String getTrangThai() { return trangThai; }
    public void setId(int id) { this.id = id; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }
    public void setNguoiLienHe(String nguoiLienHe) { this.nguoiLienHe = nguoiLienHe; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public void setEmail(String email) { this.email = email; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    @Override public String toString() { return tenNhaCungCap; }
}

