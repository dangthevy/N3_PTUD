package com.dao;

import com.connectDB.ConnectDB;
import com.entities.NhanVien;
import com.enums.ChucVu;
import com.enums.TrangThaiNhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_NhanVien {

    private Connection conn;

    public DAO_NhanVien(Connection conn) {
        this.conn = conn;
    }

    public DAO_NhanVien() {
    }

    // ================= GET ALL =================
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE An = 0 ORDER BY ngayVaoLam ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET BY ID =================
    public NhanVien getNhanVienByID(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE maNV = ? AND An = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= SEARCH =================
    public List<NhanVien> searchNhanVien(String keyword, ChucVu chucVu, TrangThaiNhanVien trangThai) {
        List<NhanVien> list = new ArrayList<>();

        // Xây WHERE động
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM NhanVien WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (tenNV LIKE ? OR sdt LIKE ? OR email LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (chucVu != null) {
            sql.append(" AND chucVu = ?");
            params.add(chucVu.name()); // hoặc chucVu.getLabel() tuỳ bạn lưu gì trong DB
        }
        if (trangThai != null) {
            sql.append(" AND trangThai = ?");
            params.add(trangThai.name());
        }

        sql.append(" AND An = 0 ORDER BY tenNV");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return list;
    }

    // ================= INSERT =================

    public boolean insertNhanVien(NhanVien nv) {
        String sql = "INSERT INTO NhanVien(tenNV, sdt, email, taiKhoan, matKhau, chucVu, trangThai, ngayVaoLam) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getTenNV());
            ps.setString(2, nv.getSdt());
            ps.setString(3, nv.getEmail());
            ps.setString(4, nv.getTaiKhoan());
            ps.setString(5, nv.getMatKhau());
            ps.setString(6, nv.getChucVu().name()); // QUANLY
            ps.setString(7, nv.getTrangThai().name()); // HOATDONG
            ps.setDate(8, new java.sql.Date(nv.getNgayVaoLam().getTime()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= UPDATE =================
    public boolean updateNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET " +
                "tenNV = ?, sdt = ?, email = ?, taiKhoan = ?, matKhau = ?, chucVu = ?, " +
                "trangThai = ?, ngayVaoLam = ? " +
                "WHERE maNV = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nv.getTenNV());
            ps.setString(2, nv.getSdt());
            ps.setString(3, nv.getEmail());
            ps.setString(4, nv.getTaiKhoan());
            ps.setString(5, nv.getMatKhau());
            ps.setString(6, nv.getChucVu().name());
            ps.setString(7, nv.getTrangThai().name());
            ps.setDate(8, new java.sql.Date(nv.getNgayVaoLam().getTime()));
            ps.setString(9, nv.getMaNV());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean setAnNhanVien(String maNV) {
        String sql = "UPDATE NhanVien SET An = 1, TrangThai = 'NGUNGHOATDONG' WHERE MaNV = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ================= MAP =================
    private NhanVien mapResultSet(ResultSet rs) throws SQLException {
        return new NhanVien(
                rs.getString("maNV"),
                rs.getString("tenNV"),
                rs.getString("sdt"),
                rs.getString("email"),
                rs.getString("taiKhoan"),
                rs.getString("matKhau"),
                ChucVu.fromString(rs.getString("chucVu")),
                TrangThaiNhanVien.fromString(rs.getString("trangThai")),
                rs.getDate("ngayVaoLam")
        );
    }

    /**
     * --- HÀM ĐĂNG NHẬP --- Kiểm tra tài khoản, mật khẩu và trạng thái hoạt động
     */
    public boolean checkLogin(String username, String password) {
        // Lưu ý: Tên cột 'trangThai' phải khớp với DB của bạn (có thể là trangThaiNV)
        String sql = "SELECT maNV FROM NhanVien WHERE maNV = ? AND matKhau = ? AND (trangThai = 'HOATDONG' OR trangThai = 'HOAT DONG')";

        try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean create(String maNV, String tenNV, String matKhau, String chucVu) {
        String sql = "INSERT INTO NhanVien (maNV, tenNV, matKhau, chucVu, trangThai) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, maNV);
            stmt.setString(2, tenNV);
            stmt.setString(3, matKhau);
            // Chuẩn hóa chức vụ để phân quyền dễ hơn
            stmt.setString(4, chucVu);
            stmt.setString(5, "HOATDONG");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * --- KIỂM TRA MÃ NV TỒN TẠI ---
     */
    public boolean isIdExists(String id) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE maNV = ? AND (trangThai = 'HOATDONG' OR trangThai = 'HOAT DONG')";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

            if (con == null)
                return false;

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * --- HÀM LẤY ĐỐI TƯỢNG NHÂN VIÊN THEO ID --- Dùng để lấy Tên và Chức vụ sau
     * khi đăng nhập thành công
     */
    public NhanVien getNhanVienById(String id) {
        NhanVien nv = null;
        String sql = "SELECT * FROM NhanVien WHERE maNV = ? AND (trangThai = 'HOATDONG' OR trangThai = 'HOAT DONG')";

        try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setTenNV(rs.getString("tenNV"));
                    nv.setChucVu(ChucVu.fromString(rs.getString("chucVu"))  );
                    nv.setMatKhau(rs.getString("matKhau"));

                    nv.setTrangThai(TrangThaiNhanVien.fromString(rs.getString("trangThai")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }
}