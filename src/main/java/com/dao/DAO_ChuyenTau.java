package com.dao;

import com.connectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng ChuyenTau
 * Các thao tác: getAll, insert, update, delete
 *
 * Cấu trúc bảng ChuyenTau:
 *   maChuyen VARCHAR(10) PK
 *   tenChuyen NVARCHAR(100)
 *   maTau     VARCHAR(10) FK → Tau
 *   maTuyen   VARCHAR(10) FK → Tuyen
 */
public class DAO_ChuyenTau {

    // =========================================================
    // MODEL đơn giản để truyền dữ liệu (inner class)
    // =========================================================
    public static class ChuyenTauRow {
        public String maChuyen, tenChuyen, maTau, maTuyen;
        public ChuyenTauRow(String maChuyen, String tenChuyen,
                            String maTau,    String maTuyen) {
            this.maChuyen  = maChuyen;
            this.tenChuyen = tenChuyen;
            this.maTau     = maTau;
            this.maTuyen   = maTuyen;
        }
    }

    // =========================================================
    // LẤY TẤT CẢ CHUYẾN TÀU (JOIN với Tuyen để lấy tenTuyen)
    // =========================================================
    public List<ChuyenTauRow> getAll() {
        List<ChuyenTauRow> list = new ArrayList<>();
        String sql = "SELECT c.maChuyen, c.tenChuyen, c.maTau, c.maTuyen " +
                "FROM ChuyenTau c " +
                "ORDER BY c.maChuyen ASC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ChuyenTauRow(
                        rs.getString("maChuyen"),
                        rs.getString("tenChuyen"),
                        rs.getString("maTau"),
                        rs.getString("maTuyen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // THÊM CHUYẾN TÀU
    // =========================================================
    public boolean insert(String maChuyen, String tenChuyen,
                          String maTau,    String maTuyen) {
        String sql = "INSERT INTO ChuyenTau (maChuyen, tenChuyen, maTau, maTuyen) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuyen);
            ps.setString(2, tenChuyen);
            ps.setString(3, maTau);
            ps.setString(4, maTuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // CẬP NHẬT CHUYẾN TÀU
    // =========================================================
    public boolean update(String maChuyen, String tenChuyen,
                          String maTau,    String maTuyen) {
        String sql = "UPDATE ChuyenTau " +
                "SET tenChuyen = ?, maTau = ?, maTuyen = ? " +
                "WHERE maChuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenChuyen);
            ps.setString(2, maTau);
            ps.setString(3, maTuyen);
            ps.setString(4, maChuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // XÓA CHUYẾN TÀU (xóa LichTrinh liên quan trước)
    // =========================================================
    public boolean delete(String maChuyen) {
        String sqlDelLT  = "DELETE FROM LichTrinh WHERE maChuyen = ?";
        String sqlDelCT  = "DELETE FROM ChuyenTau WHERE maChuyen = ?";
        try (Connection conn = ConnectDB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDelLT);
                 PreparedStatement ps2 = conn.prepareStatement(sqlDelCT)) {
                ps1.setString(1, maChuyen); ps1.executeUpdate();
                ps2.setString(1, maChuyen);
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
    // KIỂM TRA MÃ CHUYẾN ĐÃ TỒN TẠI CHƯA
    // =========================================================
    public boolean exists(String maChuyen) {
        String sql = "SELECT COUNT(*) FROM ChuyenTau WHERE maChuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuyen);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // LẤY SỐ LƯỢNG CHUYẾN HIỆN CÓ (để sinh mã tiếp theo)
    // =========================================================
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ChuyenTau";
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