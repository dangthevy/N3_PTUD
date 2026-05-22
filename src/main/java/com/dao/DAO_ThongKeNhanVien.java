package com.dao;

import com.connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO_ThongKeNhanVien {

    public List<Object[]> getDsTheoNhanVien(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT " +
                "CAST(hd.ngayLap AS DATE) AS Ngay, " +
                "nv.tenNV AS NhanVien, " +
                "COUNT(cthd.maVe) AS SoVeBan, " +
                "COALESCE(SUM(cthd.thanhTien), 0) AS DoanhThu " +
                "FROM HoaDon hd " +
                "INNER JOIN NhanVien nv ON hd.maNV = nv.maNV " +
                "INNER JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                "WHERE 1=1 ";

        if (tuNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) >= ? ";
        if (denNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) <= ? ";

        sql += "GROUP BY CAST(hd.ngayLap AS DATE), nv.tenNV " +
                "ORDER BY Ngay ASC, NhanVien ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (tuNgay != null) ps.setDate(idx++, new java.sql.Date(tuNgay.getTime()));
            if (denNgay != null) ps.setDate(idx++, new java.sql.Date(denNgay.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getDate("Ngay"),
                            rs.getString("NhanVien"),
                            rs.getInt("SoVeBan"),
                            rs.getDouble("DoanhThu")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van thong ke nhan vien: " + e.getMessage());
        }
        return list;
    }

    public double[] getKpiData(Date tuNgay, Date denNgay) {
        double[] kpi = new double[2]; // [0]: tong nhan vien, [1]: tong doanh thu
        String sql = "SELECT " +
                "COUNT(DISTINCT hd.maNV) AS TongNhanVien, " +
                "COALESCE(SUM(cthd.thanhTien), 0) AS TongDoanhThu " +
                "FROM HoaDon hd " +
                "INNER JOIN ChiTietHoaDon cthd ON hd.maHD = cthd.maHD " +
                "WHERE 1=1 ";

        if (tuNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) >= ? ";
        if (denNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) <= ? ";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (tuNgay != null) ps.setDate(idx++, new java.sql.Date(tuNgay.getTime()));
            if (denNgay != null) ps.setDate(idx++, new java.sql.Date(denNgay.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpi[0] = rs.getDouble("TongNhanVien");
                    kpi[1] = rs.getDouble("TongDoanhThu");
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van KPI thong ke nhan vien: " + e.getMessage());
        }
        return kpi;
    }
}
