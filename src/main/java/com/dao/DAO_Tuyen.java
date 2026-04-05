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
        String sql = "SELECT * FROM Tuyen ORDER BY maTuyen ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Lấy thông tin Ga Đi
                String GaDi = rs.getString("gaDi");
                Ga gaDi = null;
                if (GaDi != null) {
                    DAO_Ga daoGa = new DAO_Ga();
                    gaDi = daoGa.getGaByMa(GaDi);
                }

                // Lấy thông tin Ga Đến
                String maGaDen = rs.getString("gaDen");
                Ga gaDen = null;
                if (maGaDen != null) {
                    DAO_Ga daoGa = new DAO_Ga();
                    gaDen = daoGa.getGaByMa(maGaDen);
                }

                dsTuyen.add(new Tuyen(
                        rs.getString("maTuyen"),
                        rs.getString("tenTuyen"),
                        rs.getInt("thoiGianChay"),
                        gaDi,
                        gaDen
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    // Cập nhật thông tin Tuyến
    public boolean updateTuyen(Tuyen tuyen) {
        // Cập nhật tất cả trừ Mã Tuyến (Vì Mã Tuyến là khóa chính, không được đổi)
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

    // Lấy thông tin chi tiết của 1 Tuyến dựa vào Mã Tuyến
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

    // Phát sinh tự động mã chuyến
    public String phatSinhMaTuyen(){
        String maTuyen = "T001";
        String sql = "SELECT MAX(CAST(SUBSTRING(maTuyen, 2, LEN(maTuyen)) AS INT)) FROM Tuyen";
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
}

