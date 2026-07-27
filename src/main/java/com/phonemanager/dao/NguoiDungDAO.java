package com.phonemanager.dao;

import com.phonemanager.model.NguoiDung;
import java.sql.*;
import java.util.*;

// ============================================================
//  NguoiDungDAO.java — Toàn bộ SQL cho bảng NguoiDung
//  trangthai lưu DB: "Hoat dong" (KHÔNG DẤU)
// ============================================================
public class NguoiDungDAO {

    public static final String[] COLUMNS = {
        "ID", "Tên đăng nhập", "Họ tên", "Vai trò", "Email", "SĐT", "Trạng thái"
    };

    // Đăng nhập — trangthai phải là 'Hoat dong' (không dấu)
    public NguoiDung login(String username, String matKhau, String vaiTro)
            throws SQLException {
        String sql = "SELECT * FROM NguoiDung WHERE username=? AND matkhau=? AND vaitro=? AND trangthai='Hoat dong'";
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,username); ps.setString(2,matKhau); ps.setString(3,vaiTro);
            try (ResultSet r=ps.executeQuery()) { return r.next()?map(r):null; }
        }
    }

    // Lấy tất cả
    public List<NguoiDung> getAll() throws SQLException {
        List<NguoiDung> list=new ArrayList<>();
        try (Connection c=DatabaseConnection.getConnection();
             Statement s=c.createStatement();
             ResultSet r=s.executeQuery("SELECT * FROM NguoiDung ORDER BY id")) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }

    // Kiểm tra tên đăng nhập đã tồn tại chưa
    public boolean existsUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM NguoiDung WHERE username=?";
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet r=ps.executeQuery()) {
                return r.next();
            }
        }
    }

    // Thêm mới
    public boolean insert(NguoiDung nd) throws SQLException {
        String sql = "INSERT INTO NguoiDung(username,matkhau,hoten,vaitro,email,sdt,trangthai) VALUES(?,?,?,?,?,?,'Hoat dong')";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nd.getUsername());
            ps.setString(2, nd.getMatKhau());
            ps.setString(3, nd.getHoTen());
            ps.setString(4, nd.getVaiTro());
            ps.setString(5, nd.getEmail());
            ps.setString(6, nd.getSdt());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            // Hỗ trợ schema tối thiểu trong trường hợp cột số điện thoại chưa được triển khai.
            if (!isMissingSdtColumn(ex)) throw ex;
            String fallback = "INSERT INTO NguoiDung(username,matkhau,hoten,vaitro,email,trangthai) VALUES(?,?,?,?,?,'Hoat dong')";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(fallback)) {
                ps.setString(1, nd.getUsername());
                ps.setString(2, nd.getMatKhau());
                ps.setString(3, nd.getHoTen());
                ps.setString(4, nd.getVaiTro());
                ps.setString(5, nd.getEmail());
                return ps.executeUpdate() > 0;
            }
        }
    }

    // Khóa tài khoản thay vì xóa cứng để giữ người lập hóa đơn/phiếu nhập trong lịch sử.
    public boolean delete(int id) throws SQLException {
        try (Connection c=DatabaseConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(
                "UPDATE NguoiDung SET trangthai='Khoa' WHERE id=?")) {
            ps.setInt(1,id); return ps.executeUpdate()>0;
        }
    }

    private NguoiDung map(ResultSet r) throws SQLException {
        return new NguoiDung(r.getInt("id"), r.getString("username"),
            r.getString("matkhau"), r.getString("hoten"), r.getString("vaitro"),
            r.getString("email"), getOptionalString(r, "sdt"), r.getString("trangthai"));
    }

    private String getOptionalString(ResultSet r, String columnName) throws SQLException {
        try {
            String value = r.getString(columnName);
            return value == null ? "" : value;
        } catch (SQLException ex) {
            if (isMissingSdtColumn(ex)) return "";
            throw ex;
        }
    }

    private boolean isMissingSdtColumn(SQLException ex) {
        String msg = ex.getMessage();
        return msg != null && msg.toLowerCase(Locale.ROOT).contains("sdt");
    }
}
