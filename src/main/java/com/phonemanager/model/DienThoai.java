package com.phonemanager.model;

import java.util.Locale;

// ============================================================
//  DienThoai.java — Model ánh xạ bảng DienThoai
// ============================================================
public class DienThoai {

    private int    id;
    private String tenMay;
    private String hang;
    private String model;
    private long   giaNhap;
    private long   giaBan;
    private int    tonKho;
    private int    daBan;
    private String ram;
    private String mauSac;
    private String trangThai;  // Lưu DB: "Con hang"/"Het hang"/"Ngung KD"

    public DienThoai(int id, String tenMay, String hang, String model,
                     long giaNhap, long giaBan, int tonKho, int daBan,
                     String ram, String mauSac, String trangThai) {
        this.id = id; this.tenMay = tenMay; this.hang = hang;
        this.model = model; this.giaNhap = giaNhap; this.giaBan = giaBan;
        this.tonKho = tonKho; this.daBan = daBan; this.ram = ram;
        this.mauSac = mauSac; this.trangThai = trangThai;
    }

    public DienThoai() {}

    // Getters
    public int    getId()         { return id; }
    public String getTenMay()     { return tenMay; }
    public String getHang()       { return hang; }
    public String getModel()      { return model; }
    public long   getGiaNhap()    { return giaNhap; }
    public long   getGiaBan()     { return giaBan; }
    public int    getTonKho()     { return tonKho; }
    public int    getDaBan()      { return daBan; }
    public String getRam()        { return ram; }
    public String getMauSac()     { return mauSac; }
    public String getTrangThai()  { return trangThai; }

    // Setters
    public void setId(int v)            { id = v; }
    public void setTenMay(String v)     { tenMay = v; }
    public void setHang(String v)       { hang = v; }
    public void setModel(String v)      { model = v; }
    public void setGiaNhap(long v)      { giaNhap = v; }
    public void setGiaBan(long v)       { giaBan = v; }
    public void setTonKho(int v)        { tonKho = v; }
    public void setDaBan(int v)         { daBan = v; }
    public void setRam(String v)        { ram = v; }
    public void setMauSac(String v)     { mauSac = v; }
    public void setTrangThai(String v)  { trangThai = v; }

    // Tính lãi
    public long getLaiMoiSp()  { return giaBan - giaNhap; }
    public long getDoanhThu()  { return giaBan * daBan; }
    public long getTongLai()   { return getLaiMoiSp() * daBan; }

    public static String hienThiTrangThai(String value) {
        if (value == null) return "";
        String s = value.trim();
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "con hang", "còn hàng" -> "Còn hàng";
            case "het hang", "hết hàng" -> "Hết hàng";
            case "ngung kd", "ngừng kd", "ngung kinh doanh", "ngừng kinh doanh" -> "Ngừng kinh doanh";
            default -> s;
        };
    }

    public static String luuTrangThai(String value) {
        if (value == null) return "Con hang";
        String s = value.trim();
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "còn hàng", "con hang" -> "Con hang";
            case "hết hàng", "het hang" -> "Het hang";
            case "ngừng kd", "ngung kd", "ngừng kinh doanh", "ngung kinh doanh" -> "Ngung KD";
            default -> s;
        };
    }

    @Override public String toString() { return tenMay + " - " + hang + " - Tồn: " + tonKho; }

    // Đổ vào hàng JTable
    public Object[] toRow() {
        return new Object[]{
                id, tenMay, hang, model,
                String.format("%,d", giaNhap),
                String.format("%,d", giaBan),
                tonKho, daBan, ram, mauSac, hienThiTrangThai(trangThai)
        };
    }
}
