package com.dao;

import com.connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO_ThongKeTuyen {

    public List<Object[]> getDanhSachTuyen() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT maTuyen, tenTuyen " +
                "FROM Tuyen " +
                "WHERE trangThai = 1 OR trangThai IS NULL " +
                "ORDER BY maTuyen ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("maTuyen"),
                        rs.getString("tenTuyen")
                });
            }
        } catch (SQLException e) {
            System.err.println("Loi lay danh sach tuyen: " + e.getMessage());
        }

        return list;
    }

    public List<Object[]> getDsTheoTuyen(Date tuNgay, Date denNgay, String maTuyen) {
        List<Object[]> list = new ArrayList<>();

        String denDateTimeExpr = "DATEADD(MINUTE, ISNULL(ty.thoiGianChay, 0), " +
                "DATEADD(SECOND, DATEDIFF(SECOND, 0, lt.gioKhoiHanh), CAST(lt.ngayKhoiHanh AS DATETIME)))";

        String sql = "SELECT " +
                "lt.maLT AS MaLT, " +
                "ct.tenChuyen AS TenChuyen, " +
                "ty.tenTuyen AS TenTuyen, " +
                "CONVERT(VARCHAR, lt.ngayKhoiHanh, 103) AS NgayKhoiHanh, " +
                "CONVERT(VARCHAR(5), lt.gioKhoiHanh, 108) AS GioKhoiHanh, " +
                "FORMAT(" + denDateTimeExpr + ", 'dd/MM/yyyy') AS NgayDen, " +
                "FORMAT(" + denDateTimeExpr + ", 'HH:mm') AS GioDen, " +
                "CASE " +
                "WHEN GETDATE() < DATEADD(SECOND, DATEDIFF(SECOND, 0, lt.gioKhoiHanh), CAST(lt.ngayKhoiHanh AS DATETIME)) THEN N'Chưa khởi hành' " +
                "WHEN GETDATE() <= " + denDateTimeExpr + " THEN N'Đang khởi hành' " +
                "ELSE N'Đã hoàn thành' " +
                "END AS TrangThai " +
                "FROM LichTrinh lt " +
                "INNER JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                "INNER JOIN Tuyen ty ON ct.maTuyen = ty.maTuyen " +
                "WHERE 1=1 ";

        if (maTuyen != null && !maTuyen.trim().isEmpty()) {
            sql += " AND ct.maTuyen = ? ";
        }
        if (tuNgay != null) {
            sql += " AND CAST(lt.ngayKhoiHanh AS DATE) >= ? ";
        }
        if (denNgay != null) {
            sql += " AND CAST(lt.ngayKhoiHanh AS DATE) <= ? ";
        }

        sql += " ORDER BY lt.ngayKhoiHanh ASC, lt.gioKhoiHanh ASC, ct.maChuyen ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (maTuyen != null && !maTuyen.trim().isEmpty()) {
                ps.setString(idx++, maTuyen.trim());
            }
            if (tuNgay != null) {
                ps.setDate(idx++, new java.sql.Date(tuNgay.getTime()));
            }
            if (denNgay != null) {
                ps.setDate(idx++, new java.sql.Date(denNgay.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("MaLT"),
                            rs.getString("TenChuyen"),
                            rs.getString("TenTuyen"),
                            rs.getString("NgayKhoiHanh"),
                            rs.getString("GioKhoiHanh"),
                            rs.getString("NgayDen"),
                            rs.getString("GioDen"),
                            rs.getString("TrangThai")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van thong ke tuyen: " + e.getMessage());
        }

        return list;
    }

    public int[] getKpiData(Date tuNgay, Date denNgay, String maTuyen) {
        int[] kpi = new int[2];
        String sql = "SELECT " +
                "COUNT(DISTINCT ct.maChuyen) AS TongChuyen, " +
                "COUNT(lt.maLT) AS TongLichTrinh " +
                "FROM LichTrinh lt " +
                "INNER JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                "WHERE 1=1 ";

        if (maTuyen != null && !maTuyen.trim().isEmpty()) {
            sql += " AND ct.maTuyen = ? ";
        }
        if (tuNgay != null) {
            sql += " AND CAST(lt.ngayKhoiHanh AS DATE) >= ? ";
        }
        if (denNgay != null) {
            sql += " AND CAST(lt.ngayKhoiHanh AS DATE) <= ? ";
        }

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (maTuyen != null && !maTuyen.trim().isEmpty()) {
                ps.setString(idx++, maTuyen.trim());
            }
            if (tuNgay != null) {
                ps.setDate(idx++, new java.sql.Date(tuNgay.getTime()));
            }
            if (denNgay != null) {
                ps.setDate(idx++, new java.sql.Date(denNgay.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpi[0] = rs.getInt("TongChuyen");
                    kpi[1] = rs.getInt("TongLichTrinh");
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van KPI thong ke tuyen: " + e.getMessage());
        }

        return kpi;
    }
}
