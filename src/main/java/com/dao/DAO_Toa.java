package com.dao;

import com.connectDB.ConnectDB;
import com.entities.LoaiToa;
import com.entities.Tau;
import com.entities.Toa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_Toa {
    
    // ==== HÀM MỚI: TỰ ĐỘNG SINH MÃ TOA THEO TÀU (Ví dụ: TAU0001_T_03) ====
	public String phatSinhMaToaTheoTau(String maTau) {
        String sql = "SELECT TOP 1 maToa FROM Toa WHERE maTau = ? ORDER BY maToa DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastMa = rs.getString("maToa");
                    // Bắt lỗi an toàn: Nếu mã cũ không có chữ "_T_" thì mặc định bắt đầu từ 01
                    if (lastMa != null && lastMa.contains("_T_")) {
                        String[] parts = lastMa.split("_T_");
                        if (parts.length > 1) {
                            try {
                                int number = Integer.parseInt(parts[1]) + 1;
                                return String.format("%s_T_%02d", maTau, number);
                            } catch (NumberFormatException e) {
                                // Bỏ qua nếu lỗi format, chạy xuống return mặc định
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        // Nếu chưa có toa nào hoặc gặp mã cũ không đúng chuẩn thì trả về 01
        return String.format("%s_T_01", maTau);
    }
	
    public boolean insertToa(Toa toa) {
        String sql = "INSERT INTO Toa (maToa, tenToa, soGhe, maTau, maLoaiToa) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toa.getMaToa()); ps.setString(2, toa.getTenToa());
            ps.setInt(3, toa.getSoGhe()); ps.setString(4, toa.getTau().getMaTau());
            ps.setString(5, toa.getLoaiToa().getMaLoaiToa()); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Toa> getToaByMaTau(String maTau) {
        List<Toa> ds = new ArrayList<>();
        String sql = "SELECT * FROM Toa t JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa WHERE maTau = ? ORDER BY LEN(tenToa) ASC, tenToa ASC"; 
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTau); ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Toa t = new Toa();
                t.setMaToa(rs.getString("maToa")); t.setTenToa(rs.getString("tenToa")); t.setSoGhe(rs.getInt("soGhe"));
                LoaiToa lt = new LoaiToa(); lt.setMaLoaiToa(rs.getString("maLoaiToa")); lt.setTenLoaiToa(rs.getString("tenLoaiToa")); t.setLoaiToa(lt);
                ds.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean updateToa(Toa toa) {
        String sql = "UPDATE Toa SET tenToa = ?, soGhe = ?, maLoaiToa = ? WHERE maToa = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toa.getTenToa()); ps.setInt(2, toa.getSoGhe());
            ps.setString(3, toa.getLoaiToa().getMaLoaiToa()); ps.setString(4, toa.getMaToa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public Toa getToaById(String maToa) {
        String sql = "SELECT t.*, lt.tenLoaiToa FROM Toa t JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa WHERE t.maToa = ?";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Toa t = new Toa();
                t.setMaToa(rs.getString("maToa")); t.setTenToa(rs.getString("tenToa")); t.setSoGhe(rs.getInt("soGhe"));
                LoaiToa lt = new LoaiToa(); lt.setMaLoaiToa(rs.getString("maLoaiToa")); lt.setTenLoaiToa(rs.getString("tenLoaiToa")); t.setLoaiToa(lt);
                Tau tau = new Tau(); tau.setMaTau(rs.getString("maTau")); t.setTau(tau);
                return t;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean deleteToa(String maToa) {
        String sql = "DELETE FROM Toa WHERE maToa = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maToa); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}