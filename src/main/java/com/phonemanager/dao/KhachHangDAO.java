package com.phonemanager.dao;

import com.phonemanager.model.KhachHang;
import java.sql.*;
import java.util.*;

public class KhachHangDAO {
    public List<KhachHang> getAll() throws SQLException {
        String sql = "SELECT id,ho_ten,sdt,email,dia_chi,trang_thai FROM KhachHang ORDER BY id";
        List<KhachHang> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }
    public List<KhachHang> search(String keyword) throws SQLException {
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT id,ho_ten,sdt,email,dia_chi,trang_thai FROM KhachHang " +
                "WHERE ho_ten LIKE ? OR sdt LIKE ? OR email LIKE ? ORDER BY id";
        List<KhachHang> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) list.add(map(r)); }
        }
        return list;
    }
    public boolean insert(KhachHang k) throws SQLException {
        String sql = "INSERT INTO KhachHang(ho_ten,sdt,email,dia_chi,trang_thai) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setP(ps, k); return ps.executeUpdate() > 0;
        }
    }
    public boolean update(KhachHang k) throws SQLException {
        String sql = "UPDATE KhachHang SET ho_ten=?,sdt=?,email=?,dia_chi=?,trang_thai=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setP(ps, k); ps.setInt(6, k.getId()); return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE KhachHang SET trang_thai='Khoa' WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        }
    }
    private void setP(PreparedStatement ps, KhachHang k) throws SQLException {
        ps.setString(1, k.getHoTen()); ps.setString(2, k.getSdt()); ps.setString(3, k.getEmail());
        ps.setString(4, k.getDiaChi()); ps.setString(5, k.getTrangThai() == null ? "Hoat dong" : k.getTrangThai());
    }
    private KhachHang map(ResultSet r) throws SQLException {
        return new KhachHang(r.getInt("id"), r.getString("ho_ten"), r.getString("sdt"), r.getString("email"), r.getString("dia_chi"), r.getString("trang_thai"));
    }
}
