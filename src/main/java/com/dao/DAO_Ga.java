package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entities.Ga;

public class DAO_Ga {
    public List<Ga> getAllGa() {
        List<Ga> dsGa = new ArrayList<>();
        // CHỈ LẤY CÁC GA ĐANG HOẠT ĐỘNG (trangThai = 1)
        String sql = "SELECT * FROM Ga WHERE trangThai = 1 OR trangThai IS NULL ORDER BY maGa ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Cập nhật: Thêm thuộc tính tinhThanh
                dsGa.add(new Ga(rs.getString("maGa"), rs.getString("tenGa"), rs.getString("diaChi"), rs.getString("tinhThanh")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dsGa;
    }

    public Ga getGaByMa(String maGa) {
        Ga ga = null;
        // Lấy tất cả (kể cả đã ẩn) để Tuyến không bị mất thông tin Ga Đi/Đến
        String sql = "SELECT * FROM Ga WHERE maGa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ga = new Ga(rs.getString("maGa"), rs.getString("tenGa"), rs.getString("diaChi"), rs.getString("tinhThanh"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ga;
    }

    public String getTenGaByMa(String maGa) {
        String tenGa = "";
        String sql = "SELECT tenGa FROM Ga WHERE maGa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tenGa = rs.getString("tenGa");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tenGa;
    }

    public List<Ga> timKiemGa(String tuKhoa) {
        List<Ga> dsGa = new ArrayList<>();
        // Tìm kiếm trên cả mã ga, tên ga, địa chỉ và tỉnh thành
        String sql = "SELECT * FROM Ga WHERE (trangThai = 1 OR trangThai IS NULL) AND (maGa LIKE ? OR tenGa LIKE ? OR diaChi LIKE ? OR tinhThanh LIKE ?)";

        try(Connection conn = ConnectDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            String searchPattern = "%" + tuKhoa + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()){
                    dsGa.add(new Ga(rs.getString("maGa"), rs.getString("tenGa"), rs.getString("diaChi"), rs.getString("tinhThanh")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dsGa;
    }

    public boolean addGa(Ga ga) {
        // Cập nhật: Thêm tinhThanh vào truy vấn INSERT
        String sql = "INSERT INTO Ga (maGa, tenGa, diaChi, tinhThanh) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ga.getMaGa());
            ps.setString(2, ga.getTenGa());
            ps.setString(3, ga.getDiaChi());
            ps.setString(4, ga.getTinhThanh());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean updateGa(Ga ga) {
        // Cập nhật: Thêm tinhThanh vào truy vấn UPDATE
        String sql = "UPDATE Ga SET tenGa = ?, diaChi = ?, tinhThanh = ? WHERE maGa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ga.getTenGa());
            ps.setString(2, ga.getDiaChi());
            ps.setString(3, ga.getTinhThanh());
            ps.setString(4, ga.getMaGa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // XÓA ẢO BẰNG UPDATE
    public boolean deleteGa(String maGa) {
        String sql = "UPDATE Ga SET trangThai = 0 WHERE maGa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // TỰ ĐỘNG SINH MÃ BẰNG SQL (Quét trên toàn bộ CSDL kể cả mã đã ẩn)
    public String phatSinhMaGa() {
        String maGa = "GA01";
        String sql = "SELECT MAX(CAST(SUBSTRING(maGa, 3, LEN(maGa)) AS INT)) FROM Ga WHERE maGa LIKE 'GA%'";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int lastSo = rs.getInt(1);
                if (lastSo > 0) {
                    lastSo++;
                    maGa = String.format("GA%02d", lastSo);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return maGa;
    }
}