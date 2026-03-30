package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entities.ChoNgoi;
import com.entities.Toa;
import com.enums.TrangThaiCho;

public class DAO_ChoNgoi {

    // 1. Lấy danh sách ghế theo toa (Khớp bảng ChoNgoi có trangThai)
    public List<ChoNgoi> getChoNgoiByToa(String maToa) {
    List<ChoNgoi> ds = new ArrayList<>();
    // Ép kiểu phần số sau chữ cái để sắp xếp chính xác từ 1 đến 64
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
            cn.setTrangThai(TrangThaiCho.valueOf(rs.getString("trangThai")));
            ds.add(cn);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return ds;
}

    // 2. Batch Insert (Tự động tạo ghế khi thêm Toa mới)
    // Cải tiến: Thêm Transaction Rollback để an toàn dữ liệu
    public boolean insertBatchGhe(Toa toa) {
        String sql = "INSERT INTO ChoNgoi (maCho, tenCho, maToa, trangThai) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = ConnectDB.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction
            
            PreparedStatement ps = conn.prepareStatement(sql);
            int soGhe = toa.getSoGhe();
            
            for (int i = 1; i <= soGhe; i++) {
                String tenGhe = String.format("G%02d", i);
                // Mã ghế = Mã Toa + Số ghế (Đảm bảo không bao giờ trùng PK)
                String maGhe = toa.getMaToa() + "-" + tenGhe;
                
                ps.setString(1, maGhe);
                ps.setString(2, tenGhe);
                ps.setString(3, toa.getMaToa());
                ps.setString(4, "TRONG"); // Mặc định khi tạo mới là TRỐNG
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit(); // Lưu thay đổi
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // 3. Cập nhật trạng thái (Dùng khi đặt vé hoặc giữ chỗ)
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
 // Thêm hàm này vào file DAO_ChoNgoi.java
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
 // Xóa toàn bộ ghế thuộc về một mã toa cụ thể
    public boolean deleteGheByToa(String maToa) {
        String sql = "DELETE FROM ChoNgoi WHERE maToa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maToa);
            // Trả về true nếu có ít nhất một dòng bị xóa (hoặc toa đó vốn không có ghế)
            return ps.executeUpdate() >= 0; 
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}