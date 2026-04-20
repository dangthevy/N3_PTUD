package com.dao;

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

    // ================= GET ALL =================
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE An = 0 AND ChucVu != 'ADMIN' ORDER BY ngayVaoLam ASC";

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

    // Kiểm tra đăng nhập
    public NhanVien checkLogin(String username, String password) {
        String sql = "SELECT * FROM NhanVien WHERE taiKhoan = ? AND matKhau = ? AND trangThai = 'HOATDONG'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(String ten, String sdt, String email, String tk, String mk, String cv, int tinhTrang, int an, java.sql.Date ngayVaoLam) {
        // BỎ maNV ra khỏi danh sách cột và VALUES
        String sql = "INSERT INTO NhanVien (tenNV, sdt, email, taiKhoan, matKhau, chucVu, trangThai, An, ngayVaoLam) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, ten);
            pst.setString(2, sdt);
            pst.setString(3, email);
            pst.setString(4, tk);
            pst.setString(5, mk);
            pst.setString(6, cv);
            pst.setInt(7, tinhTrang);
            pst.setInt(8, an);
            pst.setDate(9, ngayVaoLam);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUserByEmail(String taiKhoan, String email) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE taiKhoan = ? AND email = ?";
        // Sử dụng try-with-resources để tự động đóng kết nối, tránh rò rỉ bộ nhớ
        try (Connection con = com.connectDB.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, taiKhoan);
            ps.setString(2, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn verifyUserByEmail: " + e.getMessage());
        }
        return false;
    }

    // ================= KIỂM TRA SĐT =================
    /**
     * Kiểm tra SĐT đã tồn tại với An = 0 (đang hoạt động) chưa.
     * - excludeMaNV: khi edit thì bỏ qua chính nhân viên đang sửa (truyền maNV của họ).
     *               khi thêm mới thì truyền null.
     * @return true nếu SĐT bị trùng (chặn lưu), false nếu hợp lệ (cho phép lưu)
     */
    public boolean isSdtExists(String sdt, String excludeMaNV) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE sdt = ? AND An = 0"
                + (excludeMaNV != null ? " AND maNV != ?" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            if (excludeMaNV != null) ps.setString(2, excludeMaNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Kiểm tra mã tồn tại
    public boolean isIdExists(String id) {
        String sql = "SELECT COUNT(*) FROM NhanVien WHERE maNV = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật Profile (Dùng cho trang cá nhân - chỉ sửa thông tin cơ bản)
    public boolean updateProfile(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNV=?, sdt=?, email=? WHERE maNV=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, nv.getTenNV());
            pst.setString(2, nv.getSdt());
            pst.setString(3, nv.getEmail());
            pst.setString(4, nv.getMaNV());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Đổi mật khẩu
    public boolean updatePassword(String taiKhoan, String newPassword) {
        String sql = "UPDATE NhanVien SET matKhau = ? WHERE taiKhoan = ?";
        try (Connection con = com.connectDB.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, taiKhoan);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật mật khẩu: " + e.getMessage());
            return false;
        }
    }
}