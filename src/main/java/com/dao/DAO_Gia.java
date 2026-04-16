package com.dao;

import com.connectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho GiaHeader + GiaDetail
 *
 * Cấu trúc bảng (đã đơn giản hóa):
 *
 *   GiaHeader (maGia PK, tenGia, moTa, ngayApDung, ngayKetThuc)
 *
 *   GiaDetail (maGia FK, maLoaiToa, maTuyen, gia)
 *     PK tổng hợp: (maGia, maLoaiToa, maTuyen)
 *     - gia: giá cơ sở (áp dụng cho người lớn)
 *     - Các mức giảm theo loại vé (trẻ em, sinh viên) được xử lý qua KhuyenMai
 */
public class DAO_Gia {

    // =========================================================
    // INNER CLASS — GiaHeaderRow
    // =========================================================
    public static class GiaHeaderRow {
        public String maGia;
        public String moTa;
        public String ngayApDung;   // yyyy-MM-dd
        public String ngayKetThuc;  // yyyy-MM-dd

        public GiaHeaderRow(String maGia, String moTa,
                            String ngayApDung, String ngayKetThuc) {
            this.maGia       = maGia;
            this.moTa        = moTa;
            this.ngayApDung  = ngayApDung;
            this.ngayKetThuc = ngayKetThuc;
        }
    }

    // =========================================================
    // INNER CLASS — GiaDetailRow (bỏ maLoaiVe)
    // =========================================================
    public static class GiaDetailRow {
        public String maGia;
        public String maLoaiToa;
        public String maTuyen;
        public long   gia;

        public GiaDetailRow(String maGia, String maLoaiToa,
                            String maTuyen, long gia) {
            this.maGia     = maGia;
            this.maLoaiToa = maLoaiToa;
            this.maTuyen   = maTuyen;
            this.gia       = gia;
        }
    }

    // =========================================================
    // GIA HEADER — GET ALL
    // =========================================================
    public List<GiaHeaderRow> getAllHeader() {
        List<GiaHeaderRow> list = new ArrayList<>();
        String sql = "SELECT maGia, COALESCE(moTa, tenGia, maGia) AS moTa, " +
                "ngayApDung, ngayKetThuc " +
                "FROM GiaHeader ORDER BY ngayApDung DESC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new GiaHeaderRow(
                        rs.getString("maGia"),
                        rs.getString("moTa"),
                        rs.getString("ngayApDung"),
                        rs.getString("ngayKetThuc")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // GIA HEADER — INSERT
    // =========================================================
    public boolean insertHeader(String maGia, String moTa,
                                String ngayApDung, String ngayKetThuc) {
        String sql = "INSERT INTO GiaHeader (maGia, tenGia, moTa, ngayApDung, ngayKetThuc) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            ps.setString(2, moTa);
            ps.setString(3, moTa);
            ps.setString(4, ngayApDung);
            ps.setString(5, ngayKetThuc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GIA HEADER — UPDATE
    // =========================================================
    public boolean updateHeader(String maGia, String moTa,
                                String ngayApDung, String ngayKetThuc) {
        String sql = "UPDATE GiaHeader " +
                "SET tenGia = ?, moTa = ?, ngayApDung = ?, ngayKetThuc = ? " +
                "WHERE maGia = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, moTa);
            ps.setString(2, moTa);
            ps.setString(3, ngayApDung);
            ps.setString(4, ngayKetThuc);
            ps.setString(5, maGia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GIA HEADER — DELETE (xóa Detail trước, rồi xóa Header)
    // =========================================================
    public boolean deleteHeader(String maGia) {
        String sqlDelDetail = "DELETE FROM GiaDetail WHERE maGia = ?";
        String sqlDelHeader = "DELETE FROM GiaHeader WHERE maGia = ?";
        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDelDetail);
                 PreparedStatement ps2 = conn.prepareStatement(sqlDelHeader)) {
                ps1.setString(1, maGia);
                ps1.executeUpdate();
                ps2.setString(1, maGia);
                boolean ok = ps2.executeUpdate() > 0;
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GIA HEADER — COUNT ALL
    // =========================================================
    public int countAllHeader() {
        String sql = "SELECT COUNT(*) FROM GiaHeader";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // =========================================================
    // GIA DETAIL — GET BY maGia
    // =========================================================
    public List<GiaDetailRow> getDetailByMaGia(String maGia) {
        List<GiaDetailRow> list = new ArrayList<>();
        String sql = "SELECT maGia, maLoaiToa, maTuyen, gia " +
                "FROM GiaDetail " +
                "WHERE maGia = ? " +
                "ORDER BY maTuyen, maLoaiToa";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new GiaDetailRow(
                            rs.getString("maGia"),
                            rs.getString("maLoaiToa"),
                            rs.getString("maTuyen"),
                            rs.getLong("gia")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // GIA DETAIL — INSERT (bỏ maLoaiVe)
    // =========================================================
    public boolean insertDetail(String maGia, String maLoaiToa,
                                String maTuyen, long gia) {
        String sql = "INSERT INTO GiaDetail (maGia, maLoaiToa, maTuyen, gia) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            ps.setString(2, maLoaiToa);
            ps.setString(3, maTuyen);
            ps.setLong(4, gia);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GIA DETAIL — UPDATE gia
    // =========================================================
    public boolean updateDetail(String maGia, String maLoaiToa,
                                String maTuyen, long giaNew) {
        String sql = "UPDATE GiaDetail SET gia = ? " +
                "WHERE maGia = ? AND maLoaiToa = ? AND maTuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, giaNew);
            ps.setString(2, maGia);
            ps.setString(3, maLoaiToa);
            ps.setString(4, maTuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GIA DETAIL — DELETE 1 dòng
    // =========================================================
    public boolean deleteDetail(String maGia, String maLoaiToa,
                                String maTuyen) {
        String sql = "DELETE FROM GiaDetail " +
                "WHERE maGia = ? AND maLoaiToa = ? AND maTuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            ps.setString(2, maLoaiToa);
            ps.setString(3, maTuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}