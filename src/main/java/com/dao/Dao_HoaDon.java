package com.dao;

import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Dao_HoaDon {

	public Dao_HoaDon() {
	}

	/**
	 * Lấy danh sách hóa đơn dựa trên các tiêu chí lọc Tương ứng với logic trong hàm
	 * loadDanhSachHoaDon()
	 */
	public ResultSet getDanhSachHoaDon(String tenKH, String maNV, Timestamp tuNgay, Timestamp toiNgay)
			throws SQLException {
		Connection con = ConnectDB.getConnection();

		// 1. Thêm n.tenNV vào SELECT và LEFT JOIN với bảng NhanVien
		StringBuilder sql = new StringBuilder(
				"SELECT h.maHD, h.ngayLap, k.tenKH, n.tenNV, h.tongTien " + "FROM HoaDon h "
						+ "LEFT JOIN KhachHang k ON h.maKH = k.maKH " + "LEFT JOIN NhanVien n ON h.maNV = n.maNV " // Thêm
																													// dòng
																													// này
						+ "WHERE 1=1");

		if (tenKH != null && !tenKH.isEmpty())
			sql.append(" AND k.tenKH LIKE ?");
		if (maNV != null && !maNV.isEmpty() && !maNV.equals("--- Tất cả ---"))
			sql.append(" AND h.maNV = ?");
		if (tuNgay != null)
			sql.append(" AND h.ngayLap >= ?");
		if (toiNgay != null)
			sql.append(" AND h.ngayLap <= ?");

		sql.append(" ORDER BY h.ngayLap DESC");

		PreparedStatement ps = con.prepareStatement(sql.toString());
		int i = 1;
		if (tenKH != null && !tenKH.isEmpty())
			ps.setString(i++, "%" + tenKH + "%");
		if (maNV != null && !maNV.isEmpty() && !maNV.equals("--- Tất cả ---"))
			ps.setString(i++, maNV);
		if (tuNgay != null)
			ps.setTimestamp(i++, tuNgay);
		if (toiNgay != null)
			ps.setTimestamp(i++, toiNgay);

		return ps.executeQuery();
	}

	/**
	 * Lấy thông tin chung của một hóa đơn Tương ứng với phần đầu của hàm
	 * hienThiChiTiet()
	 */
	public ResultSet getThongTinHoaDon(String maHD) throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "SELECT h.maHD, h.ngayLap, h.tongTien, n.tenNV, k.tenKH " + "FROM HoaDon h "
				+ "LEFT JOIN NhanVien n ON h.maNV = n.maNV " + "LEFT JOIN KhachHang k ON h.maKH = k.maKH "
				+ "WHERE h.maHD = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, maHD);
		return ps.executeQuery();
	}

	/**
	 * Lấy chi tiết các vé trong hóa đơn Tương ứng với phần truy vấn bảng
	 * ChiTietHoaDon
	 */
	public ResultSet getChiTietHoaDon(String maHD) throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "SELECT ct.maVe, v.maLoaiVe, ct.tienGoc, ct.tienGiam, ct.thanhTien " + "FROM ChiTietHoaDon ct "
				+ "JOIN Ve v ON ct.maVe = v.maVe " + "WHERE ct.maHD = ?";
		PreparedStatement ps = con.prepareStatement(sql);
		ps.setString(1, maHD);
		return ps.executeQuery();
	}

	/**
	 * Lấy danh sách nhân viên để đổ vào ComboBox lọc Tương ứng với hàm
	 * loadNhanVienToCombo()
	 */
	public ResultSet getAllNhanVien() throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "SELECT maNV, tenNV FROM NhanVien";
		return con.createStatement().executeQuery(sql);
	}

	/**
	 * Thực thi truy vấn SQL tùy chỉnh với tham số Tháng và Năm Phục vụ cho việc
	 * thống kê hoặc xuất báo cáo Excel
	 */
	public ResultSet queryRaw(String sql, int month, int year) {
		try {
			Connection con = ConnectDB.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);

			// Truyền tham số vào câu SQL: WHERE MONTH(ngayLap) = ? AND YEAR(ngayLap) = ?
			ps.setInt(1, month);
			ps.setInt(2, year);

			return ps.executeQuery();
		} catch (SQLException e) {
			System.err.println("Lỗi thực thi queryRaw: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}