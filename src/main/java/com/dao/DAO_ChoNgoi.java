package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entities.ChoNgoi;
import com.entities.Toa;
import com.enums.TrangThaiCho;

public class DAO_ChoNgoi {

    public List<ChoNgoi> getChoNgoiByToa(String maToa) {
        List<ChoNgoi> ds = new ArrayList<>();
        String sql = "SELECT * FROM ChoNgoi WHERE maToa = ? " +
                     "ORDER BY LEN(tenCho) ASC, tenCho ASC";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChoNgoi cn = new ChoNgoi();
                cn.setMaCho(rs.getString("maCho"));
                cn.setTenCho(rs.getString("tenCho"));
                cn.setTrangThai(TrangThaiCho.valueOf(rs.getString("trangThai").trim())); 
                ds.add(cn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean insertBatchGhe(Toa toa) {
        String sql = "INSERT INTO ChoNgoi (maCho, tenCho, maToa, trangThai) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null; 
        try {
            conn = ConnectDB.getConnection();
            conn.setAutoCommit(false); 
            
            ps = conn.prepareStatement(sql);
            int soGhe = toa.getSoGhe();
            
            for (int i = 1; i <= soGhe; i++) {
                String tenGhe = String.format("G%02d", i);
                String maGhe = toa.getMaToa() + "-" + tenGhe;
                
                ps.setString(1, maGhe);
                ps.setString(2, tenGhe);
                ps.setString(3, toa.getMaToa());
                ps.setString(4, "TRONG");
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit(); 
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean updateTrangThai(String maCho, TrangThaiCho status) {
        String sql = "UPDATE ChoNgoi SET trangThai = ? WHERE maCho = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, maCho);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int countGheByTrangThai(String maToa, TrangThaiCho status) {
        String sql = "SELECT COUNT(*) FROM ChoNgoi WHERE maToa = ? AND trangThai = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa);
            ps.setString(2, status.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean deleteGheByToa(String maToa) {
        String sql = "DELETE FROM ChoNgoi WHERE maToa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa);
            return ps.executeUpdate() >= 0; 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 // Thêm hàm này vào DAO_ChoNgoi.java
    public boolean updateTrangThaiToanToa(String maToa, TrangThaiCho status) {
        // Chỉ cập nhật những ghế chưa có người đặt (tránh đụng chạm vé đã bán)
        String sql = "UPDATE ChoNgoi SET trangThai = ? WHERE maToa = ? AND trangThai != 'DADAT'";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, maToa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}