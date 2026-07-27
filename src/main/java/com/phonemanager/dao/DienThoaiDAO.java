package com.phonemanager.dao;

import com.phonemanager.model.DienThoai;
import java.sql.*;
import java.util.*;

// ============================================================
//  DienThoaiDAO.java — Toàn bộ SQL cho bảng DienThoai
//  trang_thai trong DB: "Con hang" / "Het hang" / "Ngung KD"
// ============================================================
public class DienThoaiDAO {

    public static final String[] COLUMNS = {
            "ID", "Tên máy", "Hãng", "Model",
            "Giá nhập (VNĐ)", "Giá bán (VNĐ)",
            "Tồn kho", "Đã bán", "RAM", "Màu sắc", "Trạng thái"
    };

    public static final String[] REPORT_COLUMNS = {
            "ID", "Tên máy", "Hãng", "Giá nhập", "Giá bán",
            "Lãi/SP", "Tồn kho", "Đã bán", "Doanh thu", "Tổng vốn", "Tổng lãi", "Tỷ lệ lãi"
    };

    // Lấy tất cả
    public List<DienThoai> getAll() throws SQLException {
        String sql = "SELECT id,ten_may,hang,model," +
                "COALESCE(gia_nhap,0) gia_nhap,COALESCE(gia_ban,0) gia_ban," +
                "ton_kho,COALESCE(da_ban,0) da_ban,ram,mau_sac,trang_thai " +
                "FROM DienThoai ORDER BY id";
        List<DienThoai> list = new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    // Lấy 1 theo id
    public DienThoai getById(int id) throws SQLException {
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(
                     "SELECT id,ten_may,hang,model," +
                             "COALESCE(gia_nhap,0) gia_nhap,COALESCE(gia_ban,0) gia_ban," +
                             "ton_kho,COALESCE(da_ban,0) da_ban,ram,mau_sac,trang_thai " +
                             "FROM DienThoai WHERE id=?")) {
            ps.setInt(1,id);
            try (ResultSet r=ps.executeQuery()) { return r.next()?map(r):null; }
        }
    }

    // Thống kê cho Dashboard
    // trang_thai dùng 'Con hang' / 'Het hang' (KHÔNG DẤU)
    public int[] getStatistics() throws SQLException {
        String sql =
                "SELECT COUNT(*) tong," +
                        "SUM(CASE WHEN trang_thai='Con hang' THEN 1 ELSE 0 END) con_hang," +
                        "SUM(CASE WHEN trang_thai='Het hang' THEN 1 ELSE 0 END) het_hang," +
                        "COALESCE(SUM(ton_kho),0) tong_kho FROM DienThoai";
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            if (r.next()) return new int[]{
                    r.getInt("tong"),r.getInt("con_hang"),
                    r.getInt("het_hang"),r.getInt("tong_kho")};
        }
        return new int[]{0,0,0,0};
    }

    // Doanh thu tổng hợp
    public long[] getDoanhThuTongHop() throws SQLException {
        String sql =
                "SELECT COALESCE(SUM(COALESCE(gia_ban,0)*COALESCE(da_ban,0)),0) dt," +
                        "COALESCE(SUM(COALESCE(gia_nhap,0)*COALESCE(da_ban,0)),0) von," +
                        "COALESCE(SUM((COALESCE(gia_ban,0)-COALESCE(gia_nhap,0))*COALESCE(da_ban,0)),0) lai," +
                        "COALESCE(SUM(COALESCE(da_ban,0)),0) so_ban," +
                        "COALESCE(SUM(COALESCE(ton_kho,0)),0) ton_kho FROM DienThoai";
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            if (r.next()) return new long[]{r.getLong("dt"),r.getLong("von"),
                    r.getLong("lai"),r.getLong("so_ban"),
                    r.getLong("ton_kho")};
        }
        return new long[]{0,0,0,0,0};
    }

    // Báo cáo chi tiết từng SP
    public List<Object[]> getBaoCaoChiTiet() throws SQLException {
        String sql =
                "SELECT id,ten_may,hang," +
                        "COALESCE(gia_nhap,0) gn,COALESCE(gia_ban,0) gb," +
                        "(COALESCE(gia_ban,0)-COALESCE(gia_nhap,0)) lai_sp," +
                        "COALESCE(ton_kho,0) tk," +
                        "COALESCE(da_ban,0) db," +
                        "(COALESCE(gia_ban,0)*COALESCE(da_ban,0)) dt," +
                        "(COALESCE(gia_nhap,0)*COALESCE(da_ban,0)) tv," +
                        "((COALESCE(gia_ban,0)-COALESCE(gia_nhap,0))*COALESCE(da_ban,0)) tl," +
                        "CASE WHEN COALESCE(gia_nhap,0)>0 " +
                        "THEN CAST(((COALESCE(gia_ban,0)-COALESCE(gia_nhap,0))*100.0/COALESCE(gia_nhap,1)) AS DECIMAL(5,1)) " +
                        "ELSE 0 END tl_lai " +
                        "FROM DienThoai WHERE COALESCE(da_ban,0)>0 ORDER BY tl DESC";
        List<Object[]> list=new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            while (r.next()) list.add(new Object[]{
                    r.getInt("id"), r.getString("ten_may"), r.getString("hang"),
                    fmt(r.getLong("gn")), fmt(r.getLong("gb")), fmt(r.getLong("lai_sp")),
                    r.getInt("tk"), r.getInt("db"), fmt(r.getLong("dt")), fmt(r.getLong("tv")),
                    fmt(r.getLong("tl")), r.getDouble("tl_lai")+"%"
            });
        }
        return list;
    }

    // Báo cáo theo hãng
    public List<Object[]> getBaoCaoTheoHang() throws SQLException {
        String sql =
                "SELECT hang,COUNT(*) so_sp,SUM(COALESCE(ton_kho,0)) ton_kho,SUM(COALESCE(da_ban,0)) so_ban," +
                        "SUM(COALESCE(gia_ban,0)*COALESCE(da_ban,0)) dt," +
                        "SUM(COALESCE(gia_nhap,0)*COALESCE(da_ban,0)) tv," +
                        "SUM((COALESCE(gia_ban,0)-COALESCE(gia_nhap,0))*COALESCE(da_ban,0)) tl " +
                        "FROM DienThoai GROUP BY hang ORDER BY tl DESC";
        List<Object[]> list=new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            while (r.next()) list.add(new Object[]{
                    r.getString("hang"),r.getInt("so_sp"),r.getInt("ton_kho"),r.getInt("so_ban"),
                    fmt(r.getLong("dt")),fmt(r.getLong("tv")),fmt(r.getLong("tl"))
            });
        }
        return list;
    }

    // Thêm mới
    public boolean insert(DienThoai dt) throws SQLException {
        String sql="INSERT INTO DienThoai(ten_may,hang,model,gia_nhap,gia_ban,ton_kho,da_ban,ram,mau_sac,trang_thai) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql)) {
            setP(ps,dt); return ps.executeUpdate()>0;
        }
    }

    // Cập nhật
    public boolean update(DienThoai dt) throws SQLException {
        String sql="UPDATE DienThoai SET ten_may=?,hang=?,model=?,gia_nhap=?,gia_ban=?,ton_kho=?,da_ban=?,ram=?,mau_sac=?,trang_thai=? WHERE id=?";
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql)) {
            setP(ps,dt); ps.setInt(11,dt.getId()); return ps.executeUpdate()>0;
        }
    }

    // Ngừng kinh doanh thay vì xóa cứng để không làm mất lịch sử nhập/bán hàng.
    public boolean delete(int id) throws SQLException {
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(
                     "UPDATE DienThoai SET trang_thai='Ngung KD' WHERE id=?")) {
            ps.setInt(1,id); return ps.executeUpdate()>0;
        }
    }

    private DienThoai map(ResultSet r) throws SQLException {
        return new DienThoai(r.getInt("id"),r.getString("ten_may"),r.getString("hang"),
                r.getString("model"),r.getLong("gia_nhap"),r.getLong("gia_ban"),
                r.getInt("ton_kho"),r.getInt("da_ban"),r.getString("ram"),
                r.getString("mau_sac"),r.getString("trang_thai"));
    }

    private void setP(PreparedStatement ps, DienThoai d) throws SQLException {
        ps.setString(1,d.getTenMay()); ps.setString(2,d.getHang());
        ps.setString(3,d.getModel());  ps.setLong(4,d.getGiaNhap());
        ps.setLong(5,d.getGiaBan());   ps.setInt(6,d.getTonKho());
        ps.setInt(7,d.getDaBan());     ps.setString(8,d.getRam());
        ps.setString(9,d.getMauSac()); ps.setString(10,d.getTrangThai());
    }

    private String fmt(long v) { return String.format("%,d",v); }
}
