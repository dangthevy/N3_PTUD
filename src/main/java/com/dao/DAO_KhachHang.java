package com.dao;

import com.connectDB.ConnectDB;
import com.entities.KhachHang;

import java.sql.*;
import java.util.Vector;

public class DAO_KhachHang {

	public Vector<Vector<Object>> getAllKhachHang() {
		Vector<Vector<Object>> data = new Vector<>();
		// Truy vấn đúng các cột: maKH, tenKH, email, sdt, cccd
		String sql = "SELECT maKH, tenKH, email, sdt, cccd FROM KhachHang";

		try (Connection con = com.connectDB.ConnectDB.getConnection(); // Gọi kết nối từ lớp ConnectDB của bạn
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Vector<Object> row = new Vector<>();
				row.add(rs.getString("maKH")); // Cột 0: Mã KH
				row.add(rs.getString("tenKH")); // Cột 1: Tên KH
				row.add(rs.getString("email")); // Cột 2: Email
				row.add(rs.getString("sdt")); // Cột 3: Số điện thoại
				row.add(rs.getString("cccd")); // Cột 4: CCCD

				data.add(row);
			}
		} catch (SQLException e) {
			System.out.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
			e.printStackTrace();
		}

		return data;
	}

	// tim khach hàng
	public Vector<Vector<Object>> searchKhachHang(String keyword) {
		Vector<Vector<Object>> data = new Vector<>();
		// Latin1_General_CI_AI: CI là không phân biệt hoa thường, AI là không phân biệt
		// dấu
		String sql = "SELECT maKH, tenKH, email, sdt, cccd FROM KhachHang "
				+ "WHERE tenKH COLLATE Latin1_General_CI_AI LIKE ? " + "OR sdt LIKE ?";

		try (Connection con = com.connectDB.ConnectDB.getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {

			String pattern = "%" + keyword + "%";
			stmt.setString(1, pattern);
			stmt.setString(2, pattern);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Vector<Object> row = new Vector<>();
				row.add(rs.getString("maKH"));
				row.add(rs.getString("tenKH"));
				row.add(rs.getString("email"));
				row.add(rs.getString("sdt"));
				row.add(rs.getString("cccd"));
				data.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	// thêm khách hàng
	public boolean addKhachHang(KhachHang kh) throws SQLException {
		Connection con = ConnectDB.getConnection();
		String sql = "INSERT INTO KhachHang VALUES(?, ?, ?, ?, ?)";
		try {
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, kh.getMaKH());
			stmt.setString(2, kh.getHoTen());
			stmt.setString(3, kh.getEmail());
			stmt.setString(4, kh.getSdt());
			stmt.setString(5, kh.getCccd());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// --- HÀM CẬP NHẬT ---
	public boolean updateKhachHang(KhachHang kh) {
		Connection con = null;
		PreparedStatement stmt = null;
		int n = 0;
		try {
			con = ConnectDB.getConnection();
			// Thứ tự: tenKH, sdt, cccd, email, maKH (theo dấu ?)
			String sql = "UPDATE KhachHang SET tenKH=?, sdt=?, cccd=?, email=? WHERE maKH=?";
			stmt = con.prepareStatement(sql);

			stmt.setString(1, kh.getHoTen()); // tenKH
			stmt.setString(2, kh.getSdt()); // sdt
			stmt.setString(3, kh.getCccd()); // cccd
			stmt.setString(4, kh.getEmail()); // email
			stmt.setString(5, kh.getMaKH()); // WHERE maKH

			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e2) {
			}
		}
		return n > 0;
	}

	// --- HÀM XÓA ---
	public boolean deleteKhachHang(String maKH) {
		Connection con = null;
		PreparedStatement stmt = null;
		int n = 0;
		try {
			con = ConnectDB.getConnection();
			String sql = "DELETE FROM KhachHang WHERE maKH = ?";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, maKH);

			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return n > 0;
	}
}