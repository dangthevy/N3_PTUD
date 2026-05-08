package com.dao;

import com.connectDB.ConnectDB;
import com.entities.*;
import com.enums.LoaiKhuyenMai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_KhuyenMaiDetail – stub với đầy đủ chữ ký hàm.
 */
public class DAO_KhuyenMaiDetail {

    private final Connection conn;

    public DAO_KhuyenMaiDetail(Connection conn) {
        this.conn = conn;
    }

    // ---- READ ----
    public List<KhuyenMaiDetail> getKhuyenMaiDetailByMaKM(String maKM) {
        List<KhuyenMaiDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMaiDetail kmd " +
                "LEFT JOIN KhuyenMai km ON km.maKM = kmd.maKM " +
                "LEFT JOIN Tuyen t ON kmd.MaTuyen = t.MaTuyen " +
                "LEFT JOIN LoaiVe lv ON lv.MaLoai = kmd.MaLoai " +
                "LEFT JOIN LoaiToa lt ON lt.MaLoaiToa = kmd.MaLoaiToa " +
                "WHERE kmd.An = 0 AND kmd.maKM = ? " +
                "ORDER BY kmd.TrangThai DESC, kmd.maKMDetail DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, maKM));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public KhuyenMaiDetail getKhuyenMaiDetailByID(String maKMDetail) {
        String sql = "SELECT * FROM KhuyenMaiDetail kmd " +
                "LEFT JOIN KhuyenMai km ON km.maKM = kmd.maKM " +
                "LEFT JOIN Tuyen t ON kmd.MaTuyen = t.MaTuyen " +
                "LEFT JOIN LoaiVe lv ON lv.MaLoai = kmd.MaLoai " +
                "LEFT JOIN LoaiToa lt ON lt.MaLoaiToa = kmd.MaLoaiToa " +
                "WHERE kmd.An = 0 AND kmd.maKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maKM = rs.getString("maKM");
                    return mapRow(rs, maKM);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int demLuotDungKM(String maKMDetail) {
        String sql = "SELECT COUNT(*) FROM ChiTiet_KhuyenMai v WHERE v.maKMDetail = ?";
        try (java.sql.Connection c = ConnectDB.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Set TrangThai = 0 cho một KhuyenMaiDetail (giữ lịch sử, không xóa).
     */
    public boolean deactivateKMDetail(String maKMDetail) {
        String sql = "UPDATE KhuyenMaiDetail SET TrangThai = 0 WHERE MaKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reactivate một KhuyenMaiDetail (rollback khi insert mới thất bại).
     */
    public boolean reactivateKMDetail(String maKMDetail) {
        String sql = "UPDATE KhuyenMaiDetail SET TrangThai = 1 WHERE MaKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra xem đã có KMDetail khác cùng bộ (MaKM, MaTuyen, maLoaiToa, MaLoai)
     * đang TrangThai = 1 và An = 0 chưa — dùng trước khi bật lại 1 dòng đã tắt.
     *
     * @param maKMDetailHienTai  Mã của dòng đang xét (để loại trừ chính nó)
     * @param maKM               Mã KhuyenMai cha
     * @param maTuyen            Mã tuyến (có thể null = tất cả)
     * @param maLoaiToa          Mã loại toa (có thể null = tất cả)
     * @param maLoai             Mã loại vé (có thể null = tất cả)
     * @return tên KMDetail đang conflict, hoặc null nếu không có conflict
     */
    public String kiemTraConKMDActive(String maKMDetailHienTai,
                                      String maKM,
                                      String maTuyen,
                                      String maLoaiToa,
                                      String maLoai) {
        // Xây SQL động để xử lý NULL đúng (NULL = NULL không dùng được với =)
        StringBuilder sql = new StringBuilder(
                "SELECT TOP 1 MaKMDetail FROM KhuyenMaiDetail " +
                        "WHERE MaKM = ? " +
                        "AND MaKMDetail <> ? " +
                        "AND TrangThai = 1 AND An = 0 "
        );
        if (maTuyen == null)    sql.append("AND MaTuyen IS NULL ");
        else                    sql.append("AND MaTuyen = ? ");
        if (maLoaiToa == null)  sql.append("AND maLoaiToa IS NULL ");
        else                    sql.append("AND maLoaiToa = ? ");
        if (maLoai == null)     sql.append("AND MaLoai IS NULL ");
        else                    sql.append("AND MaLoai = ? ");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, maKM);
            ps.setString(idx++, maKMDetailHienTai);
            if (maTuyen   != null) ps.setString(idx++, maTuyen);
            if (maLoaiToa != null) ps.setString(idx++, maLoaiToa);
            if (maLoai    != null) ps.setString(idx++, maLoai);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("MaKMDetail");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<KhuyenMaiDetail> getKhuyenMaiDetailKhaDung(java.util.Date ngayApDung, LoaiVe loaiVe, LoaiToa loaiToa, Tuyen tuyen) {
        List<KhuyenMaiDetail> list = new ArrayList<>();

        String sql = "SELECT * " +
                "FROM KhuyenMaiDetail kmd " +
                "LEFT JOIN KhuyenMai km ON kmd.MaKM = km.MaKM " +
                "LEFT JOIN Tuyen t ON kmd.MaTuyen = t.MaTuyen " +
                "LEFT JOIN LoaiVe lv ON lv.MaLoai = kmd.MaLoai " +
                "LEFT JOIN LoaiToa lt ON lt.MaLoaiToa = kmd.MaLoaiToa " +
                "WHERE km.NgayBatDau <= ? " +
                "AND km.NgayKetThuc >= ? " +
                "AND (kmd.MaLoai = ? OR kmd.MaLoai IS NULL) " +
                "AND (kmd.MaLoaiToa = ? OR kmd.MaLoaiToa IS NULL) " +
                "AND (kmd.MaTuyen = ? OR kmd.MaTuyen IS NULL) " +
                "AND km.TrangThai = 1 " +
                "AND km.An = 0 " +
                "AND kmd.TrangThai = 1 " +
                "AND kmd.An = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Date sqlDate = new java.sql.Date(ngayApDung.getTime());
            ps.setDate(1, sqlDate);
            ps.setDate(2, sqlDate);
            ps.setString(3, loaiVe.getMaLoai());
            ps.setString(4, loaiToa.getMaLoaiToa());
            ps.setString(5, tuyen.getMaTuyen());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, rs.getString("MaKM")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Tuyen> getAllTuyen() {
        List<Tuyen> list = new ArrayList<Tuyen>();
        String sql = "SELECT maTuyen, tenTuyen FROM Tuyen ORDER BY maTuyen";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Tuyen(
                        rs.getString("maTuyen"),
                        rs.getString("tenTuyen")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ---- CREATE ----
    public boolean insertKhuyenMaiDetail(KhuyenMaiDetail kmd) {
        String sql = "INSERT INTO KhuyenMaiDetail(maKM,maTuyen,loaiKM,giaTri,maLoai,maLoaiToa,trangThai) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kmd.getKhuyenMai() != null ? kmd.getKhuyenMai().getMaKM() : null);
            ps.setString(2, kmd.getTuyen() != null ? kmd.getTuyen().getMaTuyen() : null);
            ps.setString(3, kmd.getLoaiKM().name());
            ps.setDouble(4, kmd.getGiaTri());
            ps.setString(5, kmd.getLoaiVe() != null ? kmd.getLoaiVe().getMaLoai() : null);
            ps.setString(6, kmd.getLoaiToa() != null  ? kmd.getLoaiToa().getMaLoaiToa() : null);
            ps.setInt(7, kmd.isTrangThai() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- UPDATE ----
    public boolean updateKhuyenMaiDetail(KhuyenMaiDetail kmd) {
        String sql = "UPDATE KhuyenMaiDetail SET maTuyen=?,loaiKM=?,giaTri=?,maLoai=?,maLoaiToa=?, trangThai=? WHERE maKMDetail=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kmd.getTuyen() != null ? kmd.getTuyen().getMaTuyen() : null);
            ps.setString(2, kmd.getLoaiKM().name());
            ps.setDouble(3, kmd.getGiaTri());
            ps.setString(4, kmd.getLoaiVe() != null ? kmd.getLoaiVe().getMaLoai() : null);
            ps.setString(5, kmd.getLoaiToa() != null ? kmd.getLoaiToa().getMaLoaiToa() : null);
            ps.setInt(6, kmd.isTrangThai() ? 1 : 0);
            System.out.println(kmd.isTrangThai() ? 1 : 0);
            ps.setString(7,    kmd.getMaKMDetail());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- DELETE ----
    public boolean setAnKMD(String maKMDetail) {
        String sql = "UPDATE KhuyenMaiDetail SET an = 1, trangThai=0 WHERE maKMDetail = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKMDetail);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ---- HELPER ----
    private KhuyenMaiDetail mapRow(ResultSet rs, String maKM) throws SQLException {
        KhuyenMaiDetail kmd = new KhuyenMaiDetail();
        kmd.setMaKMDetail(rs.getString("maKMDetail"));

        String maTuyen = rs.getString("maTuyen");
        if(maTuyen != null) {
            kmd.setTuyen(new Tuyen(
                    maTuyen,
                    rs.getString("tenTuyen")
            ));
        } else kmd.setTuyen(null);

        kmd.setLoaiKM(LoaiKhuyenMai.fromString(rs.getString("loaiKM")));
        kmd.setGiaTri(rs.getDouble("giaTri"));

        String maLoaiVe = rs.getString("maLoai");
        if(maLoaiVe != null){
            LoaiVe loaiVe = new LoaiVe();
            loaiVe.setMaLoai(rs.getString("maLoai"));
            loaiVe.setTenLoai(rs.getString("tenLoai"));
            kmd.setLoaiVe(loaiVe);
        } else kmd.setLoaiVe(null);

        String maLoaiToa = rs.getString("maLoaiToa");
        if(maLoaiToa != null){
            LoaiToa loaiToa = new LoaiToa();
            loaiToa.setMaLoaiToa(rs.getString("maLoaiToa"));
            loaiToa.setTenLoaiToa(rs.getString("tenLoaiToa"));
            kmd.setLoaiToa(loaiToa);
        } else  kmd.setLoaiToa(null);

        kmd.setTrangThai(rs.getBoolean("TrangThai"));
        // gán KhuyenMai stub chỉ chứa maKM (đủ để dùng trong UI)
        KhuyenMai km = new KhuyenMai();
        km.setMaKM(maKM);
        km.setTenKM(rs.getString("tenKM"));
        kmd.setKhuyenMai(km);
        return kmd;
    }
}