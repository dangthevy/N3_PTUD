package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entities.Tau;
import com.enums.TrangThaiTau;

public class DAO_Tau {
    // ==== HÀM MỚI: TỰ ĐỘNG SINH MÃ TÀU (Ví dụ: TAU0015) ====
    public String phatSinhMaTau() {
        String sql = "SELECT TOP 1 maTau FROM Tau ORDER BY maTau DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastMa = rs.getString("maTau");
                int number = Integer.parseInt(lastMa.substring(3)) + 1;
                return String.format("TAU%04d", number);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "TAU0001"; // Trả về mặc định nếu bảng trống
    }

    public List<Tau> getAllTau() {
        List<Tau> dsTau = new ArrayList<>();
        String sql = "SELECT * FROM Tau";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dbStatus = rs.getString("trangThai");
                TrangThaiTau statusEnum = TrangThaiTau.valueOf(dbStatus.trim());
                Tau t = new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soToa"), statusEnum);
                dsTau.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); } 
        return dsTau;
    }

    public Tau getTauByMa(String ma) {
        Tau tau = null;
        String sql = "SELECT * FROM Tau WHERE maTau = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TrangThaiTau trangThai = TrangThaiTau.valueOf(rs.getString("trangThai").trim());
                tau = new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soToa"), trangThai);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tau;
    }

    public boolean insertTau(Tau t) {
        String sql = "INSERT INTO Tau VALUES(?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getMaTau()); ps.setString(2, t.getTenTau());
            ps.setInt(3, t.getSoToa()); ps.setString(4, t.getTrangThaiTau().name()); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateTau(Tau t) {
        String sql = "UPDATE Tau SET tenTau = ?, soToa = ?, trangThai = ? WHERE maTau = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTenTau()); ps.setInt(2, t.getSoToa());
            ps.setString(3, t.getTrangThaiTau().name()); ps.setString(4, t.getMaTau());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Tau> searchTau(String keyword) {
        List<Tau> list = new ArrayList<>();
        String sql = "SELECT * FROM Tau WHERE maTau LIKE ? OR tenTau LIKE ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%"); ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soToa"), TrangThaiTau.valueOf(rs.getString("trangThai").trim())));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}