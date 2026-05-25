package com.dao;

import com.connectDB.ConnectDB;
import com.entities.LichSuBaoTri;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_LichSuBaoTri {
    
    // Ghi nhận bắt đầu bảo trì tài sản (Nhập lý do)
    public boolean ghiNhanBaoTri(LichSuBaoTri log) {
        String sql = "INSERT INTO LichSuBaoTri (loaiTaiSan, maTaiSan, ngayBatDau, lyDo, chiPhi, nguoiThucHien) VALUES (?, ?, GETDATE(), ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, log.getLoaiTaiSan());
            ps.setString(2, log.getMaTaiSan());
            ps.setString(3, log.getLyDo());
            ps.setDouble(4, log.getChiPhi());
            ps.setString(5, log.getNguoiThucHien());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Ghi nhận hoàn tất bảo trì (Cập nhật ngày kết thúc và Chi phí thực tế)
    public boolean hoanTatBaoTri(String loaiTaiSan, String maTaiSan, double chiPhiThucTe) {
        String sql = "UPDATE LichSuBaoTri SET ngayKetThuc = GETDATE(), chiPhi = ? " +
                     "WHERE loaiTaiSan = ? AND maTaiSan = ? AND ngayKetThuc IS NULL";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, chiPhiThucTe);
            ps.setString(2, loaiTaiSan);
            ps.setString(3, maTaiSan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy lịch sử bảo trì của một tài sản cụ thể
    public List<LichSuBaoTri> getLichSuByTaiSan(String loaiTaiSan, String maTaiSan) {
        List<LichSuBaoTri> list = new ArrayList<>();
        String sql = "SELECT * FROM LichSuBaoTri WHERE loaiTaiSan = ? AND maTaiSan = ? ORDER BY ngayBatDau DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loaiTaiSan);
            ps.setString(2, maTaiSan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuBaoTri log = new LichSuBaoTri();
                    log.setId(rs.getInt("id"));
                    log.setLoaiTaiSan(rs.getString("loaiTaiSan"));
                    log.setMaTaiSan(rs.getString("maTaiSan"));
                    log.setNgayBatDau(rs.getTimestamp("ngayBatDau"));
                    log.setNgayKetThuc(rs.getTimestamp("ngayKetThuc"));
                    log.setLyDo(rs.getString("lyDo"));
                    log.setChiPhi(rs.getDouble("chiPhi"));
                    log.setNguoiThucHien(rs.getString("nguoiThucHien"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}