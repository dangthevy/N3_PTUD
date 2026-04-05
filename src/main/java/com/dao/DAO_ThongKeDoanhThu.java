package com.dao;

import com.connectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO_ThongKeDoanhThu {

    // Hàm lấy danh sách thống kê doanh thu theo khoảng thời gian
    public List<Object[]> getDsTheoDoanhThu(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();

        // Đếm số lượng hóa đơn (COUNT(maHD)) thay vì số vé
        String sql = "SELECT CAST(ngayLap AS DATE) AS Ngay, " +
                "COUNT(maHD) AS SoHoaDon, " +
                "SUM(thanhTien) AS DoanhThu " +
                "FROM HoaDon " +
                "WHERE 1=1 ";

        // Điều kiện lọc theo ngày (nếu có)
        if (tuNgay != null) {
            sql += " AND CAST(ngayLap AS DATE) >= ? ";
        }
        if (denNgay != null) {
            sql += " AND CAST(ngayLap AS DATE) <= ? ";
        }

        sql += " GROUP BY CAST(ngayLap AS DATE) ORDER BY Ngay ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (tuNgay != null) {
                ps.setDate(paramIndex++, new java.sql.Date(tuNgay.getTime()));
            }
            if (denNgay != null) {
                ps.setDate(paramIndex++, new java.sql.Date(denNgay.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getDate("Ngay"),        // Ngày
                            rs.getInt("SoHoaDon"),     // Số hóa đơn
                            rs.getDouble("DoanhThu")   // Doanh thu
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn thống kê doanh thu: " + e.getMessage());
        }

        return list;
    }
}