package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entities.Tuyen;
import com.entities.Ga;

public class DAO_Tuyen {
    public List<Tuyen> getAllTuyen() {
        List<Tuyen> dsTuyen = new ArrayList<>();
        // CHỈ LẤY TUYẾN ĐANG HOẠT ĐỘNG
        String sql = "SELECT * FROM Tuyen WHERE trangThai = 1 OR trangThai IS NULL ORDER BY maTuyen ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maGaDi = rs.getString("gaDi");
                Ga gaDi = (maGaDi != null) ? new DAO_Ga().getGaByMa(maGaDi) : null;

                String maGaDen = rs.getString("gaDen");
                Ga gaDen = (maGaDen != null) ? new DAO_Ga().getGaByMa(maGaDen) : null;

                dsTuyen.add(new Tuyen(
                        rs.getString("maTuyen"), rs.getString("tenTuyen"),
                        rs.getInt("thoiGianChay"), gaDi, gaDen
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dsTuyen;
    }

    // ==========================================================
    // HÀM TÌM KIẾM MỚI THÊM VÀO
    // ==========================================================
    public List<Tuyen> timKiemTuyen(String tuKhoa) {
        List<Tuyen> dsTuyen = new ArrayList<>();
        // Lấy tuyến hoạt động có mã hoặc tên chứa từ khóa
        String sql = "SELECT * FROM Tuyen WHERE (trangThai = 1 OR trangThai IS NULL) AND (maTuyen LIKE ? OR tenTuyen LIKE ?) ORDER BY maTuyen ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + tuKhoa + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maGaDi = rs.getString("gaDi");
                    Ga gaDi = (maGaDi != null) ? new DAO_Ga().getGaByMa(maGaDi) : null;

                    String maGaDen = rs.getString("gaDen");
                    Ga gaDen = (maGaDen != null) ? new DAO_Ga().getGaByMa(maGaDen) : null;

                    dsTuyen.add(new Tuyen(
                            rs.getString("maTuyen"), rs.getString("tenTuyen"),
                            rs.getInt("thoiGianChay"), gaDi, gaDen
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dsTuyen;
    }

    public boolean addTuyen(Tuyen tuyen) {
        String sql = "INSERT INTO Tuyen (maTuyen, tenTuyen, thoiGianChay, gaDi, gaDen) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tuyen.getMaTuyen());
            ps.setString(2, tuyen.getTenTuyen());
            ps.setInt(3, tuyen.getThoiGianChay());
            ps.setString(4, tuyen.getGaDi() != null ? tuyen.getGaDi().getMaGa() : null);
            ps.setString(5, tuyen.getGaDen() != null ? tuyen.getGaDen().getMaGa() : null);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm Tuyến: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTuyen(Tuyen tuyen) {
        String sql = "UPDATE Tuyen SET tenTuyen = ?, thoiGianChay = ?, gaDi = ?, gaDen = ? WHERE maTuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tuyen.getTenTuyen());
            ps.setInt(2, tuyen.getThoiGianChay());
            ps.setString(3, tuyen.getGaDi().getMaGa());
            ps.setString(4, tuyen.getGaDen().getMaGa());
            ps.setString(5, tuyen.getMaTuyen());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi sửa Tuyến: " + e.getMessage());
            return false;
        }
    }

    public Tuyen getTuyenByMa(String maTuyen) {
        String sql = "SELECT * FROM Tuyen WHERE maTuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maTuyen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ma = rs.getString("maTuyen");
                    String ten = rs.getString("tenTuyen");
                    int thoiGianChay = rs.getInt("thoiGianChay");
                    String maGaDi = rs.getString("gaDi");
                    String maGaDen = rs.getString("gaDen");

                    DAO_Ga daoGa = new DAO_Ga();
                    Ga gaDi = daoGa.getGaByMa(maGaDi);
                    Ga gaDen = daoGa.getGaByMa(maGaDen);

                    return new Tuyen(ma, ten, thoiGianChay, gaDi, gaDen);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm Tuyến theo mã: " + e.getMessage());
        }
        return null;
    }

    public String phatSinhMaTuyen(){
        String maTuyen = "T001";
        // Phát sinh trên toàn bộ CSDL để tránh trùng lặp
        String sql = "SELECT MAX(CAST(SUBSTRING(maTuyen, 2, LEN(maTuyen)) AS INT)) FROM Tuyen WHERE maTuyen LIKE 'T%'";
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){

            if (rs.next()){
                int lastSo = rs.getInt(1);
                if (lastSo > 0) {
                    lastSo++;
                    maTuyen = String.format("T%02d", lastSo);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi phát sinh mã tuyến: " + e.getMessage());
        }
        return maTuyen;
    }

    public boolean deleteTuyen(String maTuyen) {
        String sql = "UPDATE Tuyen SET trangThai = 0 WHERE maTuyen = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}