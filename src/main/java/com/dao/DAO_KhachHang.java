package com.dao;

import com.connectDB.ConnectDB;
import com.entities.KhachHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DAO_KhachHang {

	public Vector<Vector<Object>> getAllKhachHang() {
		Vector<Vector<Object>> data = new Vector<>();
		String sql = "SELECT maKH, tenKH, email, sdt, cccd FROM KhachHang WHERE trangThai = 1";
		try (Connection con = ConnectDB.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
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

	public Vector<Vector<Object>> searchKhachHang(String keyword) {
		Vector<Vector<Object>> data = new Vector<>();
		String sql = "SELECT maKH, tenKH, email, sdt, cccd FROM KhachHang "
				+ "WHERE (tenKH COLLATE Latin1_General_CI_AI LIKE ? OR sdt LIKE ?) " + "AND trangThai = 1";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
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

	public boolean addKhachHang(KhachHang kh) {
		String sql = "INSERT INTO KhachHang (maKH, tenKH, sdt, cccd, email, trangThai) VALUES(?, ?, ?, ?, ?, 1)";
		if (kh.getMaKH() == null || kh.getMaKH().isEmpty()) {
			kh.setMaKH("KH" + (getCount() + 1));
		}
		try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, kh.getMaKH());
			stmt.setString(2, kh.getHoTen());
			stmt.setString(3, kh.getSdt());
			stmt.setString(4, kh.getCccd());
			stmt.setString(5, kh.getEmail());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateKhachHang(KhachHang kh) {
		String sql = "UPDATE KhachHang SET tenKH=?, sdt=?, cccd=?, email=? WHERE maKH=?";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, kh.getHoTen());
			stmt.setString(2, kh.getSdt());
			stmt.setString(3, kh.getCccd());
			stmt.setString(4, kh.getEmail());
			stmt.setString(5, kh.getMaKH());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// XÓA MỀM (SOFT DELETE)
	public boolean deleteKhachHang(String maKH) {
		String sql = "UPDATE KhachHang SET trangThai = 0 WHERE maKH = ?";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, maKH);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private int getCount() {
		String sql = "SELECT COUNT(*) FROM KhachHang";
		try (Connection con = ConnectDB.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next())
				return rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<KhachHang> searchBySdt(String sdt) {
		List<KhachHang> result = new ArrayList<>();
		String sql = "SELECT TOP 5 maKH, tenKH, sdt, cccd, email " + "FROM KhachHang "
				+ "WHERE sdt LIKE ? AND trangThai = 1 " + "ORDER BY tenKH";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, "%" + sdt + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new KhachHang(rs.getString("maKH"), rs.getString("tenKH"), rs.getString("sdt"),
						rs.getString("cccd"), rs.getString("email")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public KhachHang searchByCccd(String cccd) {
		// Nếu cccd trống thì không cần tìm kiếm
		if (cccd == null || cccd.trim().isEmpty()) {
			return null;
		}

		String sql = "SELECT maKH, tenKH, sdt, cccd, email FROM KhachHang WHERE cccd = ? AND trangThai = 1";

		// Sử dụng try-with-resources để tự động đóng Connection, PreparedStatement và
		// ResultSet
		try (Connection con = ConnectDB.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {

			stmt.setString(1, cccd.trim());

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new KhachHang(rs.getString("maKH"), rs.getString("tenKH"), rs.getString("sdt"),
							rs.getString("cccd"), rs.getString("email"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}