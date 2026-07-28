package com.phonemanager.dao;

import com.phonemanager.util.BusinessCodeGenerator;
import java.sql.*;
import java.util.*;

public class BanHangDAO {
    public static class SaleLine {
        public int dienThoaiId;
        public String tenMay;
        public int soLuong;
        public long donGiaBan;
        public long donGiaNhap;
        public SaleLine(int dienThoaiId, String tenMay, int soLuong, long donGiaBan, long donGiaNhap) {
            this.dienThoaiId = dienThoaiId; this.tenMay = tenMay; this.soLuong = soLuong;
            this.donGiaBan = donGiaBan; this.donGiaNhap = donGiaNhap;
        }
        public long thanhTien() { return donGiaBan * soLuong; }
    }

    public int banHang(Integer khachHangId, Integer nguoiDungId, List<SaleLine> lines, String ghiChu) throws SQLException {
        List<SaleLine> normalizedLines = normalizeLines(lines);
        String maHoaDon = BusinessCodeGenerator.create("HD");
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);
            // UPDLOCK giữ khóa cập nhật đến khi commit/rollback, tránh hai giao dịch
            // cùng đọc một mức tồn kho rồi đồng thời bán vượt số lượng còn lại.
            for (SaleLine line : normalizedLines) {
                if (line.soLuong <= 0) throw new SQLException("Số lượng bán phải lớn hơn 0");
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT ten_may,ton_kho,gia_nhap,gia_ban " +
                                "FROM DienThoai WITH (UPDLOCK, ROWLOCK) WHERE id=?")) {
                    ps.setInt(1, line.dienThoaiId);
                    try (ResultSet r = ps.executeQuery()) {
                        if (!r.next()) throw new SQLException("Không tìm thấy điện thoại ID " + line.dienThoaiId);
                        int tonKho = r.getInt("ton_kho");
                        if (line.soLuong > tonKho) {
                            throw new SQLException("Sản phẩm " + r.getString("ten_may") + " chỉ còn " + tonKho + " máy, không thể bán " + line.soLuong);
                        }
                        line.donGiaNhap = r.getLong("gia_nhap");
                        if (line.donGiaBan <= 0) line.donGiaBan = r.getLong("gia_ban");
                        if (line.donGiaBan < line.donGiaNhap) {
                            throw new SQLException("Không thể bán sản phẩm " + r.getString("ten_may")
                                    + " vì giá bán thấp hơn giá nhập. Cần quản trị viên kiểm tra lại giá.");
                        }
                    }
                }
            }

            int hoaDonId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO HoaDon(ma_hoa_don,khach_hang_id,nguoi_dung_id,ghi_chu,trang_thai) VALUES(?,?,?,?, 'Hoan thanh')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, maHoaDon);
                if (khachHangId == null || khachHangId <= 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, khachHangId);
                if (nguoiDungId == null || nguoiDungId <= 0) ps.setNull(3, Types.INTEGER); else ps.setInt(3, nguoiDungId);
                ps.setString(4, ghiChu);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được ID hóa đơn");
                    hoaDonId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ChiTietHoaDon(hoa_don_id,dien_thoai_id,so_luong,don_gia_ban,don_gia_nhap) VALUES(?,?,?,?,?)")) {
                for (SaleLine line : normalizedLines) {
                    ps.setInt(1, hoaDonId); ps.setInt(2, line.dienThoaiId); ps.setInt(3, line.soLuong);
                    ps.setLong(4, line.donGiaBan); ps.setLong(5, line.donGiaNhap);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            return hoaDonId;
        } catch (SQLException e) {
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (c != null) { try { c.setAutoCommit(true); } catch (SQLException ignored) {} try { c.close(); } catch (SQLException ignored) {} }
        }
    }

    /**
     * Gộp các dòng trùng điện thoại trước khi kiểm tra tồn kho và ghi database.
     * LinkedHashMap giữ nguyên thứ tự người dùng đã thêm vào giỏ.
     */
    private List<SaleLine> normalizeLines(List<SaleLine> lines) throws SQLException {
        if (lines == null || lines.isEmpty()) {
            throw new SQLException("Chưa có sản phẩm trong hóa đơn");
        }

        Map<Integer, SaleLine> merged = new LinkedHashMap<>();
        for (SaleLine line : lines) {
            if (line == null || line.dienThoaiId <= 0) {
                throw new SQLException("Dòng sản phẩm không hợp lệ");
            }
            SaleLine existing = merged.get(line.dienThoaiId);
            if (existing == null) {
                merged.put(line.dienThoaiId, new SaleLine(
                        line.dienThoaiId, line.tenMay, line.soLuong,
                        line.donGiaBan, line.donGiaNhap));
                continue;
            }
            if (existing.donGiaBan > 0 && line.donGiaBan > 0
                    && existing.donGiaBan != line.donGiaBan) {
                throw new SQLException("Một sản phẩm không thể có hai đơn giá bán trong cùng hóa đơn");
            }
            long totalQuantity = (long) existing.soLuong + line.soLuong;
            if (totalQuantity > Integer.MAX_VALUE) {
                throw new SQLException("Số lượng bán vượt giới hạn cho phép");
            }
            existing.soLuong = (int) totalQuantity;
            if (existing.donGiaBan <= 0) existing.donGiaBan = line.donGiaBan;
            if (line.donGiaNhap > 0) existing.donGiaNhap = line.donGiaNhap;
        }
        return new ArrayList<>(merged.values());
    }
}
