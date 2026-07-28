package com.phonemanager.dao;

import com.phonemanager.model.HoaDon;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HoaDonDAO {
    public List<HoaDon> getAll() throws SQLException {
        String sql = "SELECT hd.id,hd.ma_hoa_don,hd.ngay_ban,hd.tong_tien,hd.ghi_chu,hd.trang_thai," +
                "COALESCE(kh.ho_ten,N'Khách lẻ') khach,COALESCE(nd.hoten,nd.username,N'') nhanvien " +
                "FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.khach_hang_id=kh.id " +
                "LEFT JOIN NguoiDung nd ON hd.nguoi_dung_id=nd.id ORDER BY hd.id DESC";
        List<HoaDon> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(new HoaDon(r.getInt("id"), r.getString("ma_hoa_don"), r.getString("khach"), r.getString("nhanvien"), r.getTimestamp("ngay_ban"), r.getLong("tong_tien"), r.getString("ghi_chu"), r.getString("trang_thai")));
        }
        return list;
    }

    public List<Object[]> getDetails(int hoaDonId) throws SQLException {
        String sql = "SELECT dt.ten_may,dt.hang,ct.so_luong,ct.don_gia_ban,ct.don_gia_nhap,ct.thanh_tien " +
                "FROM ChiTietHoaDon ct JOIN DienThoai dt ON ct.dien_thoai_id=dt.id WHERE ct.hoa_don_id=? ORDER BY ct.id";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, hoaDonId);
            try (ResultSet r = ps.executeQuery()) {
                int stt = 1;
                while (r.next()) list.add(new Object[]{stt++, r.getString("ten_may"), r.getString("hang"), r.getInt("so_luong"), fmt(r.getLong("don_gia_ban")), fmt(r.getLong("thanh_tien"))});
            }
        }
        return list;
    }

    public String formatDate(java.util.Date d) {
        return d == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(d);
    }
    private String fmt(long v) { return String.format("%,d", v); }
}
