package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entities.Ga;

public class DAO_Ga {
    public List<Ga> getAllGa() {
        List<Ga> dsGa = new ArrayList<>();
        String sql = "SELECT * FROM Ga ORDER BY maGa ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dsGa.add(new Ga(
                        rs.getString("maGa"),
                        rs.getString("tenGa"),
                        rs.getString("diaChi")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsGa;
    }

    // Lấy Ga dựa vào mã Ga
    public Ga getGaByMa(String maGa) {
        Ga ga = null;
        String sql = "SELECT * FROM Ga WHERE maGa = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ga = new Ga(
                            rs.getString("maGa"),
                            rs.getString("tenGa"),
                            rs.getString("diaChi")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ga;
    }

    // Lấy tên Ga dựa vào mã Ga (Dùng để tự động tạo tên Tuyến)
    public String getTenGaByMa(String maGa) {
        String tenGa = "";
        String sql = "SELECT tenGa FROM Ga WHERE maGa = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Truyền mã ga vào dấu ?
            ps.setString(1, maGa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenGa = rs.getString("tenGa"); // Lấy tên ga nếu tìm thấy
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenGa;
    }

    // Tìm kiếm theo mã Ga, tên Ga và địa chỉ
    // Tìm kiếm theo mã Ga, tên Ga và địa chỉ (Hỗ trợ Live Search / Autocomplete)
    public List<Ga> timKiemGa(String tuKhoa) {
        List<Ga> dsGa = new ArrayList<>();

        // Dùng LIKE để tìm kiếm tương đối trên cả 3 cột
        String sql = "SELECT * FROM Ga WHERE maGa LIKE ? OR tenGa LIKE ? OR diaChi LIKE ?";

        try(Connection conn = ConnectDB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            // Dùng "%" + tuKhoa + "%" để tìm CHỨA ký tự (Khuyên dùng cho Autocomplete)
            String searchPattern = "%" + tuKhoa + "%";

            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try(ResultSet rs = ps.executeQuery()) {
                // SỬA LỖI Ở ĐÂY: Phải dùng while để lấy toàn bộ danh sách kết quả thay vì if
                while(rs.next()){
                    String ma = rs.getString("maGa");
                    String ten = rs.getString("tenGa");
                    String dc = rs.getString("diaChi");

                    dsGa.add(new Ga(ma, ten, dc));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm ga: " + e.getMessage());
        }
        return dsGa;
    }



}