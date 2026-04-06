package com.dao;

import com.entities.*;
import com.enums.LoaiKhuyenMai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_KhuyenMaiDetail – stub với đầy đủ chữ ký hàm.
 */
public class DAO_KhuyenMaiDetail {

    private final Connection conn;

    public DAO_KhuyenMaiDetail(Connection conn) {
        this.conn = conn;
    }

    // ---- READ ----
    public List<KhuyenMaiDetail> getKhuyenMaiDetailByMaKM(String maKM) {
        List<KhuyenMaiDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMaiDetail kmd " +
                "LEFT JOIN Tuyen t ON kmd.MaTuyen = t.MaTuyen " +
                "LEFT JOIN LoaiVe lv ON lv.MaLoai = kmd.MaLoai " +
                "LEFT JOIN LoaiToa lt ON lt.MaLoaiToa = kmd.MaLoaiToa " +
                "WHERE An = 0 AND maKM = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, maKM));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public KhuyenMaiDetail getKhuyenMaiDetailByID(String maKMDetail) {
        String sql = "SELECT * FROM KhuyenMaiDetail kmd " +
                "LEFT JOIN Tuyen t ON kmd.MaTuyen = t.MaTuyen " +
                "LEFT JOIN LoaiVe lv ON lv.MaLoai = kmd.MaLoai " +
                "LEFT JOIN LoaiToa lt ON lt.MaLoaiToa = kmd.MaLoaiToa " +
                "WHERE An = 0 AND maKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maKM = rs.getString("maKM");
                    return mapRow(rs, maKM);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Tuyen> getAllTuyen() {
        List<Tuyen> list = new ArrayList<Tuyen>();
        String sql = "SELECT maTuyen, tenTuyen FROM Tuyen ORDER BY maTuyen";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Tuyen(
                        rs.getString("maTuyen"),
                        rs.getString("tenTuyen")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ---- CREATE ----
    public boolean insertKhuyenMaiDetail(KhuyenMaiDetail kmd) {
        String sql = "INSERT INTO KhuyenMaiDetail(maKM,maTuyen,loaiKM,giaTri,maLoai,maLoaiToa,trangThai) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kmd.getKhuyenMai() != null ? kmd.getKhuyenMai().getMaKM() : null);
            ps.setString(2, kmd.getTuyen().getMaTuyen());
            ps.setString(3, kmd.getLoaiKM().name());
            ps.setDouble(4, kmd.getGiaTri());
            ps.setString(5, kmd.getLoaiVe().getMaLoai());
            ps.setString(6, kmd.getLoaiToa().getMaLoaiToa());
            ps.setInt(7, kmd.isTrangThai() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- UPDATE ----
    public boolean updateKhuyenMaiDetail(KhuyenMaiDetail kmd) {
        String sql = "UPDATE KhuyenMaiDetail SET maTuyen=?,loaiKM=?,giaTri=?,maLoai=?,maLoaiToa=?, trangThai=? WHERE maKMDetail=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kmd.getTuyen().getMaTuyen());
            ps.setString(2, kmd.getLoaiKM().name());
            ps.setDouble(3, kmd.getGiaTri());
            ps.setString(4, kmd.getLoaiVe().getMaLoai());
            ps.setString(5, kmd.getLoaiToa().getMaLoaiToa());
            ps.setInt(6, kmd.isTrangThai() ? 1 : 0);
            System.out.println(kmd.isTrangThai() ? 1 : 0);
            ps.setString(7,    kmd.getMaKMDetail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- DELETE ----
    public boolean setAnKMD(String maKMDetail) {
        String sql = "UPDATE KhuyenMaiDetail SET an = 1, trangThai=0 WHERE maKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- HELPER ----
    private KhuyenMaiDetail mapRow(ResultSet rs, String maKM) throws SQLException {
        KhuyenMaiDetail kmd = new KhuyenMaiDetail();
        kmd.setMaKMDetail(rs.getString("maKMDetail"));
        kmd.setTuyen(new Tuyen(
                rs.getString("maTuyen"),
                rs.getString("tenTuyen")
        ));
        kmd.setLoaiKM(LoaiKhuyenMai.fromString(rs.getString("loaiKM")));
        kmd.setGiaTri(rs.getDouble("giaTri"));
        LoaiVe loaiVe = new LoaiVe();
        loaiVe.setMaLoai(rs.getString("maLoai"));
        loaiVe.setTenLoai(rs.getString("tenLoai"));
        kmd.setLoaiVe(loaiVe);
        kmd.setLoaiToa( new LoaiToa(rs.getString("maLoaiToa"), rs.getString("tenLoaiToa")) );
        kmd.setTrangThai(rs.getBoolean("TrangThai"));
        // gán KhuyenMai stub chỉ chứa maKM (đủ để dùng trong UI)
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(maKM);
        kmd.setKhuyenMai(km);
        return kmd;
    }
}