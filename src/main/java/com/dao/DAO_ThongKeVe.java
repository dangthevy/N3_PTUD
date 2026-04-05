package com.dao;

import com.connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAO_ThongKeVe {

    // 1. Hàm lấy dữ liệu cho 3 thẻ KPI (Tổng vé, Đã sử dụng, Hết hạn)
    public int[] getKpiData(Date tuNgay, Date denNgay) {
        int[] kpi = new int[3]; // [0]: Tổng vé, [1]: Đã sử dụng, [2]: Hết hạn/Hủy

        String sql = "SELECT " +
                "COUNT(v.maVe) AS TongVe, " +
                "SUM(CASE WHEN v.trangThaiVe = 'DASUDUNG' THEN 1 ELSE 0 END) AS DaSuDung, " +
                "SUM(CASE WHEN v.trangThaiVe IN ('HETHAN', 'HUY') THEN 1 ELSE 0 END) AS HetHan " +
                "FROM Ve v " +
                "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                "WHERE 1=1 ";

        if (tuNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) >= ? ";
        if (denNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) <= ? ";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (tuNgay != null) ps.setDate(paramIndex++, new java.sql.Date(tuNgay.getTime()));
            if (denNgay != null) ps.setDate(paramIndex++, new java.sql.Date(denNgay.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kpi[0] = rs.getInt("TongVe");
                    kpi[1] = rs.getInt("DaSuDung");
                    kpi[2] = rs.getInt("HetHan");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn KPI Vé: " + e.getMessage());
        }
        return kpi;
    }

    // 2. Hàm lấy dữ liệu chi tiết cho Bảng và Biểu đồ tùy theo Tiêu chí
    public List<Object[]> getChiTietThongKe(int tieuChiIndex, Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        String sql = "";

        // tieuChiIndex: 0 = Trạng thái vé, 1 = Loại vé, 2 = Tuyến đi
        if (tieuChiIndex == 0) {
            sql = "SELECT v.trangThaiVe AS TieuChi, COUNT(v.maVe) AS SoLuong " +
                    "FROM Ve v " +
                    "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                    "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                    "WHERE 1=1 ";
        } else if (tieuChiIndex == 1) {
            sql = "SELECT lv.tenLoai AS TieuChi, COUNT(v.maVe) AS SoLuong " +
                    "FROM Ve v " +
                    "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                    "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                    "INNER JOIN LoaiVe lv ON v.maLoaiVe = lv.maLoai " +
                    "WHERE 1=1 ";
        } else if (tieuChiIndex == 2) {
            sql = "SELECT ty.tenTuyen AS TieuChi, COUNT(v.maVe) AS SoLuong " +
                    "FROM Ve v " +
                    "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                    "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                    "INNER JOIN LichTrinh lt ON v.maLT = lt.maLT " +
                    "INNER JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                    "INNER JOIN Tuyen ty ON ct.maTuyen = ty.maTuyen " +
                    "WHERE 1=1 ";
        }

        if (tuNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) >= ? ";
        if (denNgay != null) sql += " AND CAST(hd.ngayLap AS DATE) <= ? ";

        // Nhóm dữ liệu
        if (tieuChiIndex == 0) sql += " GROUP BY v.trangThaiVe";
        else if (tieuChiIndex == 1) sql += " GROUP BY lv.tenLoai";
        else if (tieuChiIndex == 2) sql += " GROUP BY ty.tenTuyen";

        sql += " ORDER BY SoLuong DESC"; // Sắp xếp số lượng từ cao xuống thấp

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (tuNgay != null) ps.setDate(paramIndex++, new java.sql.Date(tuNgay.getTime()));
            if (denNgay != null) ps.setDate(paramIndex++, new java.sql.Date(denNgay.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenTieuChi = rs.getString("TieuChi");

                    // Format lại tên trạng thái nếu là tiêu chí 0
                    if (tieuChiIndex == 0) {
                        if ("CHUASUDUNG".equals(tenTieuChi)) tenTieuChi = "Chưa sử dụng";
                        else if ("DASUDUNG".equals(tenTieuChi)) tenTieuChi = "Đã sử dụng";
                        else if ("HETHAN".equals(tenTieuChi) || "HUY".equals(tenTieuChi)) tenTieuChi = "Hết hạn/Hủy";
                    }

                    list.add(new Object[]{ tenTieuChi, rs.getInt("SoLuong") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn Chi tiết Vé: " + e.getMessage());
        }
        return list;
    }
}