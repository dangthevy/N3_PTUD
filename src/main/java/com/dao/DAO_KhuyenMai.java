package com.dao;

import com.entities.KhuyenMai;
import com.entities.LoaiToa;
import com.entities.LoaiVe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_KhuyenMai – stub với đầy đủ chữ ký hàm.
 * Implement SQL tùy schema thực tế của bạn.
 */
public class DAO_KhuyenMai {

    private final Connection conn;

    public DAO_KhuyenMai(Connection conn) {
        this.conn = conn;
    }

    // ---- READ ----
    public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE An = 0 ORDER BY maKM";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public KhuyenMai getKhuyenMaiByID(String maKM) {
        String sql = "SELECT * FROM KhuyenMai WHERE An = 0 AND maKM = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

//    public List<KhuyenMai> findKhuyenMaiByTen(String ten) {
//        List<KhuyenMai> list = new ArrayList<>();
//        String sql = "SELECT * FROM KhuyenMai WHERE tenKM LIKE ?";
//        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, "%" + ten + "%");
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) list.add(mapRow(rs));
//            }
//        } catch (SQLException e) { e.printStackTrace(); }
//        return list;
//    }

    public List<LoaiVe> getAllLoaiVe(){
        List<LoaiVe> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiVe ORDER BY maLoai";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LoaiVe lv = new LoaiVe();
                lv.setMaLoai(rs.getString("maLoai"));
                lv.setTenLoai(rs.getString("tenLoai"));
                list.add(lv);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<LoaiToa> getAllLoaiToa(){
        List<LoaiToa> list = new ArrayList<>();
        String sql = "SELECT * FROM LoaiToa ORDER BY maLoaiToa";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LoaiToa lt  = new LoaiToa();
                lt.setMaLoaiToa(rs.getString("maLoaiToa"));
                lt.setTenLoaiToa(rs.getString("tenLoaiToa"));
                list.add(lt);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Tìm kiếm kết hợp tên + ngày
    public List<KhuyenMai> searchKhuyenMai(String ten, java.util.Date ngayBD, java.util.Date ngayKT) {
        List<KhuyenMai> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM KhuyenMai WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // ===== FILTER TÊN =====
        if (ten != null && !ten.trim().isEmpty()) {
            sql.append("AND TenKM LIKE ? ");
            params.add("%" + ten.trim() + "%");
        }

        // ===== FILTER NGÀY BẮT ĐẦU =====
        if (ngayBD != null) {
            sql.append("AND NgayBatDau >= ? ");
            params.add(new java.sql.Date(ngayBD.getTime()));
        }

        // ===== FILTER NGÀY KẾT THÚC =====
        if (ngayKT != null) {
            sql.append("AND NgayKetThuc <= ? ");
            params.add(new java.sql.Date(ngayKT.getTime()));
        }

        sql.append("AND An = 0 ORDER BY maKM");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // ===== SET PARAM =====
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Đếm số KM đã có lượt sử dụng
    public int countKhuyenMaiDaDung(){
        return 0; // Cần JOIN với bảng Hóa Đơn để đếm số KM đã được áp dụng
    }

    // ---- CREATE ----
    public boolean insertKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai(tenKM,ngayBatDau,ngayKetThuc,trangThai,moTa) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, km);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- UPDATE ----
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM=?,ngayBatDau=?,ngayKetThuc=?,trangThai=?,moTa=? WHERE maKM=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setDate(2, km.getNgayBatDau() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
            ps.setDate(3, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayKetThuc().getTime()) : null);
            ps.setInt(4, km.isTrangThai() ? 1 : 0);
            ps.setString(5, km.getMoTa());
            ps.setString(6, km.getMaKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean setAnKhuyenMai(String maKM) {
        String sqlKM  = "UPDATE KhuyenMai SET An = 1, TrangThai = 0 WHERE MaKM = ?";
        String sqlKMD = "UPDATE KhuyenMaiDetail SET An = 1, TrangThai = 0 WHERE MaKM = ?";
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psKM  = conn.prepareStatement(sqlKM);
                 PreparedStatement psKMD = conn.prepareStatement(sqlKMD)) {

                psKM.setString(1, maKM);
                psKM.executeUpdate();

                psKMD.setString(1, maKM);
                psKMD.executeUpdate(); // không cần > 0 vì KM có thể chưa có detail nào

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ---- HELPER ----
    private void setParams(PreparedStatement ps, KhuyenMai km) throws SQLException {
//        ps.setString(1, km.getMaKM());
        ps.setString(1, km.getTenKM());
        ps.setDate(2, km.getNgayBatDau()  != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
        ps.setDate(3, km.getNgayKetThuc() != null ? new java.sql.Date(km.getNgayBatDau().getTime()) : null);
        ps.setBoolean(4, km.isTrangThai());
        ps.setString(5, km.getMoTa());
    }

    private KhuyenMai mapRow(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(rs.getString("maKM"));
        km.setTenKM(rs.getString("tenKM"));
        Date bd = rs.getDate("ngayBatDau");
        if (bd != null) km.setNgayBatDau(new java.util.Date(bd.getTime()));
        Date kt = rs.getDate("ngayKetThuc");
        if (kt != null) km.setNgayKetThuc(new java.util.Date(kt.getTime()));
        km.setTrangThai(rs.getBoolean("trangThai"));
        km.setMoTa(rs.getString("moTa"));
        return km;
    }
}