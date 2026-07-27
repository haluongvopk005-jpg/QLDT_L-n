package com.phonemanager.dao;

import com.phonemanager.model.NhaCungCap;
import java.sql.*;
import java.util.*;

public class NhaCungCapDAO {
    public List<NhaCungCap> getAll() throws SQLException {
        String sql = "SELECT id,ten_nha_cung_cap,nguoi_lien_he,sdt,email,dia_chi,trang_thai FROM NhaCungCap ORDER BY id";
        List<NhaCungCap> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(map(r));
        }
        return list;
    }
    public List<NhaCungCap> search(String keyword) throws SQLException {
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String sql = "SELECT id,ten_nha_cung_cap,nguoi_lien_he,sdt,email,dia_chi,trang_thai FROM NhaCungCap " +
                "WHERE ten_nha_cung_cap LIKE ? OR nguoi_lien_he LIKE ? OR sdt LIKE ? ORDER BY id";
        List<NhaCungCap> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) list.add(map(r)); }
        }
        return list;
    }
    public boolean insert(NhaCungCap n) throws SQLException {
        String sql = "INSERT INTO NhaCungCap(ten_nha_cung_cap,nguoi_lien_he,sdt,email,dia_chi,trang_thai) VALUES(?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setP(ps, n); return ps.executeUpdate() > 0;
        }
    }
    public boolean update(NhaCungCap n) throws SQLException {
        String sql = "UPDATE NhaCungCap SET ten_nha_cung_cap=?,nguoi_lien_he=?,sdt=?,email=?,dia_chi=?,trang_thai=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setP(ps, n); ps.setInt(7, n.getId()); return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE NhaCungCap SET trang_thai='Ngung hop tac' WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        }
    }
    private void setP(PreparedStatement ps, NhaCungCap n) throws SQLException {
        ps.setString(1, n.getTenNhaCungCap()); ps.setString(2, n.getNguoiLienHe()); ps.setString(3, n.getSdt());
        ps.setString(4, n.getEmail()); ps.setString(5, n.getDiaChi()); ps.setString(6, n.getTrangThai() == null ? "Hoat dong" : n.getTrangThai());
    }
    private NhaCungCap map(ResultSet r) throws SQLException {
        return new NhaCungCap(r.getInt("id"), r.getString("ten_nha_cung_cap"), r.getString("nguoi_lien_he"), r.getString("sdt"), r.getString("email"), r.getString("dia_chi"), r.getString("trang_thai"));
    }
}

