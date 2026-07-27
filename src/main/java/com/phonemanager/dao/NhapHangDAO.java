package com.phonemanager.dao;

import com.phonemanager.util.BusinessCodeGenerator;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class NhapHangDAO {
    public int nhapHang(Integer nhaCungCapId, Integer nguoiDungId, int dienThoaiId, int soLuong, long donGiaNhap, String ghiChu) throws SQLException {
        if (soLuong <= 0) throw new SQLException("Số lượng nhập phải lớn hơn 0");
        if (donGiaNhap < 0) throw new SQLException("Giá nhập không được âm");
        String maPhieu = BusinessCodeGenerator.create("PN");
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);
            int phieuId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO PhieuNhap(ma_phieu,nha_cung_cap_id,nguoi_dung_id,ghi_chu,trang_thai) VALUES(?,?,?,?, 'Hoan thanh')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, maPhieu);
                if (nhaCungCapId == null || nhaCungCapId <= 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, nhaCungCapId);
                if (nguoiDungId == null || nguoiDungId <= 0) ps.setNull(3, Types.INTEGER); else ps.setInt(3, nguoiDungId);
                ps.setString(4, ghiChu);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được ID phiếu nhập");
                    phieuId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ChiTietPhieuNhap(phieu_nhap_id,dien_thoai_id,so_luong,don_gia_nhap) VALUES(?,?,?,?)")) {
                ps.setInt(1, phieuId); ps.setInt(2, dienThoaiId); ps.setInt(3, soLuong); ps.setLong(4, donGiaNhap);
                ps.executeUpdate();
            }
            c.commit();
            return phieuId;
        } catch (SQLException e) {
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (c != null) { try { c.setAutoCommit(true); } catch (SQLException ignored) {} try { c.close(); } catch (SQLException ignored) {} }
        }
    }

    public List<Object[]> getHistory() throws SQLException {
        String sql = "SELECT TOP 100 pn.id,pn.ma_phieu,pn.ngay_nhap,COALESCE(ncc.ten_nha_cung_cap,N'Không chọn') ncc," +
                "COALESCE(nd.hoten,nd.username,N'') nv,dt.ten_may,ct.so_luong,ct.don_gia_nhap,ct.thanh_tien " +
                "FROM PhieuNhap pn LEFT JOIN NhaCungCap ncc ON pn.nha_cung_cap_id=ncc.id " +
                "LEFT JOIN NguoiDung nd ON pn.nguoi_dung_id=nd.id " +
                "LEFT JOIN ChiTietPhieuNhap ct ON pn.id=ct.phieu_nhap_id " +
                "LEFT JOIN DienThoai dt ON ct.dien_thoai_id=dt.id ORDER BY pn.id DESC";
        List<Object[]> list = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            int stt = 1;
            while (r.next()) {
                Timestamp ts = r.getTimestamp("ngay_nhap");
                list.add(new Object[]{stt++, r.getString("ma_phieu"), ts == null ? "" : df.format(ts), r.getString("ncc"), r.getString("nv"), r.getString("ten_may"), r.getInt("so_luong"), fmt(r.getLong("don_gia_nhap")), fmt(r.getLong("thanh_tien"))});
            }
        }
        return list;
    }
    private String fmt(long v) { return String.format("%,d", v); }
}
