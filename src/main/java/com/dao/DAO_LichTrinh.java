package com.dao;

import com.connectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng LichTrinh
 * Các thao tác: getByMaChuyen, insert, update, delete
 *
 * Cấu trúc bảng LichTrinh:
 *   maLT          VARCHAR(10) PK
 *   ngayKhoiHanh  DATE NOT NULL
 *   gioKhoiHanh   TIME NOT NULL
 *   maChuyen      VARCHAR(10) FK → ChuyenTau
 */
public class DAO_LichTrinh {

    // =========================================================
    // MODEL đơn giản để truyền dữ liệu (inner class)
    // =========================================================
    public static class LichTrinhRow {
        public String maLT, ngayKhoiHanh, gioKhoiHanh, maChuyen;
        public LichTrinhRow(String maLT, String ngayKhoiHanh,
                            String gioKhoiHanh, String maChuyen) {
            this.maLT         = maLT;
            this.ngayKhoiHanh = ngayKhoiHanh;
            this.gioKhoiHanh  = gioKhoiHanh;
            this.maChuyen     = maChuyen;
        }
    }

    // =========================================================
    // LẤY LỊCH TRÌNH THEO MÃ CHUYẾN
    // =========================================================
    public List<LichTrinhRow> getByMaChuyen(String maChuyen) {
        List<LichTrinhRow> list = new ArrayList<>();
        String sql = "SELECT maLT, CONVERT(VARCHAR,ngayKhoiHanh,103) AS ngayKhoiHanh, " +
                "CONVERT(VARCHAR,gioKhoiHanh,108) AS gioKhoiHanh, maChuyen " +
                "FROM LichTrinh WHERE maChuyen = ? ORDER BY ngayKhoiHanh ASC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuyen);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LichTrinhRow(
                        rs.getString("maLT"),
                        rs.getString("ngayKhoiHanh"),
                        rs.getString("gioKhoiHanh"),
                        rs.getString("maChuyen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // LẤY TẤT CẢ LỊCH TRÌNH
    // =========================================================
    public List<LichTrinhRow> getAll() {
        List<LichTrinhRow> list = new ArrayList<>();
        String sql = "SELECT maLT, CONVERT(VARCHAR,ngayKhoiHanh,103) AS ngayKhoiHanh, " +
                "CONVERT(VARCHAR,gioKhoiHanh,108) AS gioKhoiHanh, maChuyen " +
                "FROM LichTrinh ORDER BY ngayKhoiHanh ASC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LichTrinhRow(
                        rs.getString("maLT"),
                        rs.getString("ngayKhoiHanh"),
                        rs.getString("gioKhoiHanh"),
                        rs.getString("maChuyen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // THÊM LỊCH TRÌNH
    // ngayKhoiHanh: định dạng "dd/MM/yyyy"
    // gioKhoiHanh:  định dạng "HH:mm"
    // =========================================================
    public boolean insert(String maLT, String ngayKhoiHanh,
                          String gioKhoiHanh, String maChuyen) {
        String sql = "INSERT INTO LichTrinh (maLT, ngayKhoiHanh, gioKhoiHanh, maChuyen) " +
                "VALUES (?, CONVERT(DATE,?,105), CONVERT(TIME,?), ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT);
            ps.setString(2, ngayKhoiHanh);   // dd/MM/yyyy → SQL style 105
            ps.setString(3, gioKhoiHanh);    // HH:mm
            ps.setString(4, maChuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // CẬP NHẬT LỊCH TRÌNH
    // =========================================================
    public boolean update(String maLT, String ngayKhoiHanh,
                          String gioKhoiHanh, String maChuyen) {
        String sql = "UPDATE LichTrinh " +
                "SET ngayKhoiHanh = CONVERT(DATE,?,105), " +
                "    gioKhoiHanh  = CONVERT(TIME,?), " +
                "    maChuyen     = ? " +
                "WHERE maLT = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ngayKhoiHanh);
            ps.setString(2, gioKhoiHanh);
            ps.setString(3, maChuyen);
            ps.setString(4, maLT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // XÓA LỊCH TRÌNH THEO maLT
    // =========================================================
    public boolean delete(String maLT) {
        String sql = "DELETE FROM LichTrinh WHERE maLT = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // XÓA TẤT CẢ LỊCH TRÌNH THEO MÃ CHUYẾN
    // =========================================================
    public boolean deleteByMaChuyen(String maChuyen) {
        String sql = "DELETE FROM LichTrinh WHERE maChuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuyen);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // KIỂM TRA MÃ LT ĐÃ TỒN TẠI CHƯA
    // =========================================================
    public boolean exists(String maLT) {
        String sql = "SELECT COUNT(*) FROM LichTrinh WHERE maLT = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // LẤY SỐ LƯỢNG LT HIỆN CÓ (để sinh mã tiếp theo)
    // =========================================================
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM LichTrinh";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}