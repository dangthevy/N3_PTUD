package com.dao;

import com.connectDB.ConnectDB;
import com.entities.LichTrinh;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng LichTrinh
 *
 * Cấu trúc bảng LichTrinh (đã cập nhật thêm ngayDen):
 *   maLT          VARCHAR(10)  PK
 *   ngayKhoiHanh  DATE         NOT NULL
 *   gioKhoiHanh   TIME         NOT NULL
 *   ngayDen       DATETIME     NULL  ← MỚI: ngày giờ đến dự kiến
 *   maChuyen      VARCHAR(10)  FK → ChuyenTau
 *
 * Để thêm cột ngayDen vào DB, chạy lệnh SQL sau trong SSMS:
 *   ALTER TABLE LichTrinh ADD ngayDen DATETIME NULL;
 *
 * ngayDen = ngayKhoiHanh + gioKhoiHanh + thoiGianChay (phút từ bảng Tuyen)
 * Được tính tự động ở tầng GUI, truyền vào DAO dưới dạng "dd/MM/yyyy HH:mm"
 */
public class DAO_LichTrinh {

    // =========================================================================
    // MODEL truyền dữ liệu (inner class)
    // =========================================================================
    public static class LichTrinhRow {
        public String maLT, ngayKhoiHanh, gioKhoiHanh, ngayDen, maChuyen;

        public LichTrinhRow(String maLT, String ngayKhoiHanh,
                            String gioKhoiHanh, String ngayDen, String maChuyen) {
            this.maLT         = maLT;
            this.ngayKhoiHanh = ngayKhoiHanh;
            this.gioKhoiHanh  = gioKhoiHanh;
            this.ngayDen      = ngayDen != null ? ngayDen : "";
            this.maChuyen     = maChuyen;
        }
    }

    // =========================================================================
    // HELPER: chuyển "dd/MM/yyyy HH:mm" → "yyyy-MM-dd HH:mm:00"
    // để SQL Server nhận dạng được (style 120)
    // =========================================================================
    private String toSqlDatetime(String ngayDen) {
        if (ngayDen == null || ngayDen.isEmpty()) return null;
        try {
            SimpleDateFormat inFmt  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            SimpleDateFormat outFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
            return outFmt.format(inFmt.parse(ngayDen));
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================================
    // LẤY TẤT CẢ LỊCH TRÌNH
    // =========================================================================
    public List<LichTrinhRow> getAll() {
        List<LichTrinhRow> list = new ArrayList<>();
        String sql = "SELECT maLT, " +
                "CONVERT(VARCHAR, ngayKhoiHanh, 103) AS ngayKhoiHanh, " +
                "CONVERT(VARCHAR, gioKhoiHanh, 108)  AS gioKhoiHanh, " +
                // ngayDen lưu dạng DATETIME → trả về "dd/MM/yyyy HH:mm"
                "CASE WHEN ngayDen IS NOT NULL " +
                "     THEN FORMAT(ngayDen, 'dd/MM/yyyy HH:mm') " +
                "     ELSE '' END AS ngayDen, " +
                "maChuyen " +
                "FROM LichTrinh ORDER BY ngayKhoiHanh ASC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LichTrinhRow(
                        rs.getString("maLT"),
                        rs.getString("ngayKhoiHanh"),
                        rs.getString("gioKhoiHanh"),
                        rs.getString("ngayDen"),
                        rs.getString("maChuyen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // LẤY LỊCH TRÌNH THEO MÃ CHUYẾN
    // =========================================================================
    public List<LichTrinhRow> getByMaChuyen(String maChuyen) {
        List<LichTrinhRow> list = new ArrayList<>();
        String sql = "SELECT maLT, " +
                "CONVERT(VARCHAR, ngayKhoiHanh, 103) AS ngayKhoiHanh, " +
                "CONVERT(VARCHAR, gioKhoiHanh, 108)  AS gioKhoiHanh, " +
                "CASE WHEN ngayDen IS NOT NULL " +
                "     THEN FORMAT(ngayDen, 'dd/MM/yyyy HH:mm') " +
                "     ELSE '' END AS ngayDen, " +
                "maChuyen " +
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
                        rs.getString("ngayDen"),
                        rs.getString("maChuyen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // THÊM LỊCH TRÌNH
    // ngayKhoiHanh : "dd/MM/yyyy"
    // gioKhoiHanh  : "HH:mm"
    // ngayDen      : "dd/MM/yyyy HH:mm"  (tính từ ngayDi + thoiGianChay)
    // =========================================================================
    public boolean insert(String maLT, String ngayKhoiHanh,
                          String gioKhoiHanh, String ngayDen, String maChuyen) {
        String sql = "INSERT INTO LichTrinh (maLT, ngayKhoiHanh, gioKhoiHanh, ngayDen, maChuyen) " +
                "VALUES (?, CONVERT(DATE,?,105), CONVERT(TIME,?), ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT);
            ps.setString(2, ngayKhoiHanh);               // dd/MM/yyyy → style 105
            ps.setString(3, gioKhoiHanh);                // HH:mm
            String sqlDt = toSqlDatetime(ngayDen);
            if (sqlDt != null) ps.setString(4, sqlDt);
            else               ps.setNull(4, Types.TIMESTAMP);
            ps.setString(5, maChuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // CẬP NHẬT LỊCH TRÌNH
    // =========================================================================
    public boolean update(String maLT, String ngayKhoiHanh,
                          String gioKhoiHanh, String ngayDen, String maChuyen) {
        String sql = "UPDATE LichTrinh " +
                "SET ngayKhoiHanh = CONVERT(DATE,?,105), " +
                "    gioKhoiHanh  = CONVERT(TIME,?), " +
                "    ngayDen      = ?, " +
                "    maChuyen     = ? " +
                "WHERE maLT = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ngayKhoiHanh);
            ps.setString(2, gioKhoiHanh);
            String sqlDt = toSqlDatetime(ngayDen);
            if (sqlDt != null) ps.setString(3, sqlDt);
            else               ps.setNull(3, Types.TIMESTAMP);
            ps.setString(4, maChuyen);
            ps.setString(5, maLT);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    // XÓA THEO maLT
    // =========================================================================
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

    // =========================================================================
    // XÓA TẤT CẢ THEO MÃ CHUYẾN
    // =========================================================================
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

    // =========================================================================
    // KIỂM TRA MÃ LT ĐÃ TỒN TẠI CHƯA
    // =========================================================================
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

    // =========================================================================
    // ĐẾM TỔNG SỐ LỊCH TRÌNH (để sinh mã tiếp theo)
    // =========================================================================
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
    
    // =========================================================================
    // LẤY 1 LỊCH TRÌNH THEO MÃ LT (entity)
    // =========================================================================
    public LichTrinh getLichTrinhByMa(String maLT) {
        String sql = "SELECT maLT, ngayKhoiHanh, gioKhoiHanh, maChuyen FROM LichTrinh WHERE maLT = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LichTrinh lt = new LichTrinh();
                    lt.setMaLT(rs.getString("maLT"));
                    Date ngay = rs.getDate("ngayKhoiHanh");
                    Time gio = rs.getTime("gioKhoiHanh");
                    lt.setNgayKhoiHanh(ngay != null ? ngay.toLocalDate() : null);
                    lt.setGioKhoiHanh(gio != null ? gio.toLocalTime() : null);
                    lt.setMaChuyen(rs.getString("maChuyen"));
                    return lt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}