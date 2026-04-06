package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.connectDB.ConnectDB;

public class DAO_BanVe {

    // 1. LẤY DANH SÁCH GA (Chỉ lấy Ga đang hoạt động - Giống DAO_Ga)
    public Map<String, String> getDanhSachGa() {
        Map<String, String> mapGa = new HashMap<>();
        String sql = "SELECT maGa, tenGa FROM Ga WHERE trangThai = 1 OR trangThai IS NULL";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapGa.put(rs.getString("tenGa"), rs.getString("maGa"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapGa;
    }

    // 2. TÌM CHUYẾN TÀU (Dựa vào Ga Đi, Ga Đến, Ngày Khởi Hành)
    public List<Map<String, Object>> timChuyenTau(String maGaDi, String maGaDen, String ngayKhoiHanh) {
        List<Map<String, Object>> listChuyen = new ArrayList<>();
        String sql = "SELECT lt.maLT, t.maTau, t.tenTau, lt.gioKhoiHanh " +
                "FROM LichTrinh lt " +
                "JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                "JOIN Tuyen ty ON ct.maTuyen = ty.maTuyen " +
                "JOIN Tau t ON ct.maTau = t.maTau " +
                "WHERE ty.gaDi = ? AND ty.gaDen = ? AND lt.ngayKhoiHanh = ? AND t.trangThai = 'HOATDONG'";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGaDi);
            ps.setString(2, maGaDen);
            ps.setString(3, ngayKhoiHanh); // Format yyyy-MM-dd

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maLT", rs.getString("maLT"));
                    map.put("maTau", rs.getString("maTau"));
                    map.put("tenTau", rs.getString("tenTau"));
                    map.put("gioKhoiHanh", rs.getString("gioKhoiHanh"));
                    listChuyen.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listChuyen;
    }

    // 3. LẤY DANH SÁCH TOA CỦA 1 TÀU
    public List<Map<String, Object>> getDanhSachToa(String maTau) {
        List<Map<String, Object>> listToa = new ArrayList<>();
        String sql = "SELECT Toa.maToa, Toa.tenToa, LoaiToa.tenLoaiToa " +
                "FROM Toa JOIN LoaiToa ON Toa.maLoaiToa = LoaiToa.maLoaiToa " +
                "WHERE Toa.maTau = ? ORDER BY Toa.tenToa ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTau);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maToa", rs.getString("maToa"));
                    map.put("tenToa", rs.getString("tenToa"));
                    map.put("tenLoaiToa", rs.getString("tenLoaiToa"));
                    listToa.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listToa;
    }

    // 4. LẤY DANH SÁCH CHỖ NGỒI (GHẾ) CỦA 1 TOA
    public List<Map<String, Object>> getDanhSachGhe(String maToa) {
        List<Map<String, Object>> listGhe = new ArrayList<>();
        String sql = "SELECT maCho, tenCho, trangThai FROM ChoNgoi WHERE maToa = ? ORDER BY tenCho ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maCho", rs.getString("maCho"));
                    map.put("tenCho", rs.getString("tenCho"));
                    map.put("trangThai", rs.getString("trangThai"));
                    listGhe.add(map);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return listGhe;
    }
}