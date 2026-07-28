package com.phonemanager.dao;

import java.sql.*;
import java.util.*;

public class BaoCaoDAO {
    public long[] getTongHopTuHoaDon() throws SQLException {
        String sql = "SELECT " +
                "COALESCE(SUM(ct.thanh_tien),0) dt," +
                "COALESCE(SUM(ct.don_gia_nhap * ct.so_luong),0) von," +
                "COALESCE(SUM((ct.don_gia_ban - ct.don_gia_nhap) * ct.so_luong),0) lai," +
                "COALESCE(SUM(ct.so_luong),0) so_ban," +
                "(SELECT COALESCE(SUM(ton_kho),0) FROM DienThoai) ton_kho " +
                "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoa_don_id WHERE hd.trang_thai='Hoan thanh'";
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (r.next()) return new long[]{r.getLong("dt"), r.getLong("von"), r.getLong("lai"), r.getLong("so_ban"), r.getLong("ton_kho")};
        }
        return new long[]{0,0,0,0,0};
    }

    public List<Object[]> getBaoCaoChiTietTuHoaDon() throws SQLException {
        // Dùng LEFT JOIN từ bảng DienThoai để điện thoại vừa thêm vẫn hiện trong báo cáo,
        // kể cả khi sản phẩm đó chưa bán được hóa đơn nào. Các số bán/doanh thu sẽ là 0.
        String sql = "SELECT dt.id, dt.ten_may, dt.hang, " +
                "COALESCE(dt.gia_nhap,0) gn, COALESCE(dt.gia_ban,0) gb, " +
                "(COALESCE(dt.gia_ban,0)-COALESCE(dt.gia_nhap,0)) lai_sp, " +
                "COALESCE(dt.ton_kho,0) tk, " +
                "COALESCE(x.db,0) db, COALESCE(x.dtien,0) dtien, " +
                "COALESCE(x.tv,0) tv, COALESCE(x.tl,0) tl " +
                "FROM DienThoai dt " +
                "LEFT JOIN ( " +
                "   SELECT ct.dien_thoai_id, " +
                "          SUM(ct.so_luong) db, " +
                "          SUM(ct.thanh_tien) dtien, " +
                "          SUM(ct.don_gia_nhap * ct.so_luong) tv, " +
                "          SUM((ct.don_gia_ban - ct.don_gia_nhap) * ct.so_luong) tl " +
                "   FROM ChiTietHoaDon ct " +
                "   JOIN HoaDon hd ON hd.id = ct.hoa_don_id " +
                "   WHERE hd.trang_thai = 'Hoan thanh' " +
                "   GROUP BY ct.dien_thoai_id " +
                ") x ON x.dien_thoai_id = dt.id " +
                "ORDER BY COALESCE(x.tl,0) DESC, dt.id ASC";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            int stt = 1;
            while (r.next()) {
                long gn = r.getLong("gn"); long lai = r.getLong("lai_sp");
                double tyLe = gn > 0 ? lai * 100.0 / gn : 0;
                list.add(new Object[]{stt++, r.getString("ten_may"), r.getString("hang"), fmt(gn), fmt(r.getLong("gb")), fmt(lai), r.getInt("tk"), r.getInt("db"), fmt(r.getLong("dtien")), fmt(r.getLong("tv")), fmt(r.getLong("tl")), String.format("%.1f%%", tyLe)});
            }
        }
        return list;
    }

    public List<Object[]> getBaoCaoTheoHangTuHoaDon() throws SQLException {
        // Gom doanh thu theo từng điện thoại trước, rồi mới gom theo hãng.
        // Cách này tránh lỗi tồn kho bị nhân đôi khi một điện thoại có nhiều hóa đơn.
        String sql = "SELECT dt.hang, COUNT(*) so_sp, COALESCE(SUM(dt.ton_kho),0) ton_kho, " +
                "COALESCE(SUM(x.db),0) so_ban, COALESCE(SUM(x.dtien),0) dtien, " +
                "COALESCE(SUM(x.tv),0) tv, COALESCE(SUM(x.tl),0) tl " +
                "FROM DienThoai dt " +
                "LEFT JOIN ( " +
                "   SELECT ct.dien_thoai_id, " +
                "          SUM(ct.so_luong) db, " +
                "          SUM(ct.thanh_tien) dtien, " +
                "          SUM(ct.don_gia_nhap * ct.so_luong) tv, " +
                "          SUM((ct.don_gia_ban - ct.don_gia_nhap) * ct.so_luong) tl " +
                "   FROM ChiTietHoaDon ct " +
                "   JOIN HoaDon hd ON hd.id = ct.hoa_don_id " +
                "   WHERE hd.trang_thai = 'Hoan thanh' " +
                "   GROUP BY ct.dien_thoai_id " +
                ") x ON x.dien_thoai_id = dt.id " +
                "GROUP BY dt.hang ORDER BY COALESCE(SUM(x.tl),0) DESC, dt.hang ASC";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(new Object[]{r.getString("hang"), r.getInt("so_sp"), r.getInt("ton_kho"), r.getInt("so_ban"), fmt(r.getLong("dtien")), fmt(r.getLong("tv")), fmt(r.getLong("tl"))});
        }
        return list;
    }
    private String fmt(long v) { return String.format("%,d", v); }
}
