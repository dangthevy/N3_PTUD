package com.dao;

import com.entities.KhuyenMai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_KhuyenMai – stub với đầy đủ chữ ký hàm.
 * Implement SQL tùy schema thực tế của bạn.
 */
public class DAO_KhuyenMai {

    private final Connection conn;

    public DAO_KhuyenMai(Connection conn) {
        this.conn = conn;
    }

    // ---- READ ----
    public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY maKM";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public KhuyenMai getKhuyenMaiByID(String maKM) {
        String sql = "SELECT * FROM KhuyenMai WHERE maKM = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<KhuyenMai> findKhuyenMaiByTen(String ten) {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE tenKM LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + ten + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ---- CREATE ----
    public boolean insertKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai(tenKM,ngayBatDau,ngayKetThuc,trangThai,moTa) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, km);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- UPDATE ----
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM=?,ngayBatDau=?,ngayKetThuc=?,trangThai=?,moTa=? WHERE maKM=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setDate(2, km.getNgayBatDau() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
            ps.setDate(3, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayKetThuc().getTime()) : null);
            ps.setInt(4, km.isTrangThai() ? 1 : 0);
            ps.setString(5, km.getMoTa());
            ps.setString(6, km.getMaKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- DELETE ----
    public boolean setActiveKhuyenMai(String maKM, boolean active) {
        String sql = "UPDATE KhuyenMai SET trangThai = ? WHERE maKM = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, maKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- HELPER ----
    private void setParams(PreparedStatement ps, KhuyenMai km) throws SQLException {
//        ps.setString(1, km.getMaKM());
        ps.setString(1, km.getTenKM());
        ps.setDate(2, km.getNgayBatDau()  != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
        ps.setDate(3, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
        ps.setBoolean(4, km.isTrangThai());
        ps.setString(5, km.getMoTa());
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(rs.getString("maKM"));
        km.setTenKM(rs.getString("tenKM"));
        Date bd = rs.getDate("ngayBatDau");
        if (bd != null) km.setNgayBatDau(new java.util.Date(bd.getTime()));
        Date kt = rs.getDate("ngayKetThuc");
        if (kt != null) km.setNgayKetThuc(new java.util.Date(kt.getTime()));
        km.setTrangThai(rs.getBoolean("trangThai"));
        km.setMoTa(rs.getString("moTa"));
        return km;
    }
}