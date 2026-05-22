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

    public int[] getKpiData(Date tuNgay, Date denNgay) {
        int[] kpi = new int[3];

        String sql = "SELECT " +
                "COUNT(v.maVe) AS TongVe, " +
                "SUM(CASE WHEN v.trangThaiVe = 'DASUDUNG' THEN 1 ELSE 0 END) AS DaSuDung, " +
                "SUM(CASE WHEN v.trangThaiVe IN ('HETHAN', 'HUY', 'DAHOAN') THEN 1 ELSE 0 END) AS HetHan " +
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
            System.err.println("Loi truy van KPI Ve: " + e.getMessage());
        }

        return kpi;
    }

    public List<String> getDsTieuChi(int thongKeTheoIndex) {
        List<String> ds = new ArrayList<>();
        String sql;

        if (thongKeTheoIndex == 0) {
            sql = "SELECT DISTINCT v.trangThaiVe AS TieuChi FROM Ve v ORDER BY v.trangThaiVe";
        } else if (thongKeTheoIndex == 1) {
            sql = "SELECT lv.tenLoai AS TieuChi FROM LoaiVe lv ORDER BY lv.tenLoai";
        } else {
            sql = "SELECT ty.tenTuyen AS TieuChi FROM Tuyen ty ORDER BY ty.tenTuyen";
        }

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tieuChi = rs.getString("TieuChi");
                if (thongKeTheoIndex == 0) {
                    tieuChi = mapTrangThaiVeToDisplay(tieuChi);
                }
                if (!ds.contains(tieuChi)) {
                    ds.add(tieuChi);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van danh sach tieu chi Ve: " + e.getMessage());
        }

        return ds;
    }

    public List<Object[]> getChiTietThongKe(int thongKeTheoIndex, String tieuChiDaChon, Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        String sql;

        if (thongKeTheoIndex == 0) {
            sql = "SELECT v.trangThaiVe AS TieuChi, COUNT(v.maVe) AS SoLuong, COALESCE(SUM(cthd.thanhTien), 0) AS DoanhThu " +
                    "FROM Ve v " +
                    "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                    "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                    "WHERE 1=1 ";
        } else if (thongKeTheoIndex == 1) {
            sql = "SELECT lv.tenLoai AS TieuChi, COUNT(v.maVe) AS SoLuong, COALESCE(SUM(cthd.thanhTien), 0) AS DoanhThu " +
                    "FROM Ve v " +
                    "INNER JOIN ChiTietHoaDon cthd ON v.maVe = cthd.maVe " +
                    "INNER JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                    "INNER JOIN LoaiVe lv ON v.maLoaiVe = lv.maLoai " +
                    "WHERE 1=1 ";
        } else {
            sql = "SELECT ty.tenTuyen AS TieuChi, COUNT(v.maVe) AS SoLuong, COALESCE(SUM(cthd.thanhTien), 0) AS DoanhThu " +
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

        boolean laDaHoanHuy = thongKeTheoIndex == 0 && "Đã hoàn/Hủy".equalsIgnoreCase(tieuChiDaChon);
        if (laDaHoanHuy) {
            sql += " AND v.trangThaiVe IN ('DAHOAN', 'HUY') ";
        } else if (tieuChiDaChon != null && !tieuChiDaChon.trim().isEmpty()) {
            if (thongKeTheoIndex == 0) {
                sql += " AND v.trangThaiVe = ? ";
            } else if (thongKeTheoIndex == 1) {
                sql += " AND lv.tenLoai = ? ";
            } else {
                sql += " AND ty.tenTuyen = ? ";
            }
        }

        if (thongKeTheoIndex == 0) {
            sql += " GROUP BY v.trangThaiVe ";
        } else if (thongKeTheoIndex == 1) {
            sql += " GROUP BY lv.tenLoai ";
        } else {
            sql += " GROUP BY ty.tenTuyen ";
        }

        sql += " ORDER BY SoLuong DESC, TieuChi ASC";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (tuNgay != null) ps.setDate(paramIndex++, new java.sql.Date(tuNgay.getTime()));
            if (denNgay != null) ps.setDate(paramIndex++, new java.sql.Date(denNgay.getTime()));
            if (!laDaHoanHuy && tieuChiDaChon != null && !tieuChiDaChon.trim().isEmpty()) {
                if (thongKeTheoIndex == 0) {
                    ps.setString(paramIndex++, mapTrangThaiVeToDb(tieuChiDaChon));
                } else {
                    ps.setString(paramIndex++, tieuChiDaChon);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenTieuChi = rs.getString("TieuChi");
                    if (thongKeTheoIndex == 0) {
                        tenTieuChi = mapTrangThaiVeToDisplay(tenTieuChi);
                    }
                    list.add(new Object[]{
                            tenTieuChi,
                            rs.getInt("SoLuong"),
                            rs.getDouble("DoanhThu")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi truy van Chi tiet Ve: " + e.getMessage());
        }

        return list;
    }

    private String mapTrangThaiVeToDisplay(String trangThaiDb) {
        if ("CHUASUDUNG".equals(trangThaiDb)) return "Chưa sử dụng";
        if ("DASUDUNG".equals(trangThaiDb)) return "Đã sử dụng";
        if ("HETHAN".equals(trangThaiDb)) return "Hết hạn";
        if ("HUY".equals(trangThaiDb) || "DAHOAN".equals(trangThaiDb)) return "Đã hoàn/Hủy";
        return trangThaiDb;
    }

    private String mapTrangThaiVeToDb(String trangThaiDisplay) {
        if ("Chưa sử dụng".equalsIgnoreCase(trangThaiDisplay)) return "CHUASUDUNG";
        if ("Đã sử dụng".equalsIgnoreCase(trangThaiDisplay)) return "DASUDUNG";
        if ("Hết hạn".equalsIgnoreCase(trangThaiDisplay)) return "HETHAN";
        if ("Đã hoàn/Hủy".equalsIgnoreCase(trangThaiDisplay) ||
                "Hủy".equalsIgnoreCase(trangThaiDisplay) ||
                "Đã hoàn".equalsIgnoreCase(trangThaiDisplay)) {
            return "DAHOAN";
        }
        return trangThaiDisplay;
    }
}
