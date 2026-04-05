package com.dao;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entities.Tau;
import com.enums.TrangThaiTau;

public class DAO_Tau {
	public List<Tau> getAllTau() {
		List<Tau> dsTau = new ArrayList<>();
		String sql = "SELECT * FROM Tau";

		try (Connection conn = ConnectDB.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				// Chuyển từ String trong DB sang Enum trong Java
				String dbStatus = rs.getString("trangThai");
				TrangThaiTau statusEnum = TrangThaiTau.valueOf(dbStatus);

				Tau t = new Tau(
						rs.getString("maTau"),
						rs.getString("tenTau"),
						rs.getInt("soToa"),
						statusEnum
				);
				dsTau.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println("Lỗi: Giá trị trạng thái trong DB không khớp với Enum Java!");
		}
		return dsTau;
	}
	/**
	 * Tìm kiếm một đoàn tàu dựa trên Mã Tàu (Khóa chính)
	 * @param ma Mã tàu cần tìm
	 * @return Đối tượng Tau nếu tìm thấy, ngược lại trả về null
	 */
	public Tau getTauByMa(String ma) {
		Tau tau = null;
		String sql = "SELECT * FROM Tau WHERE maTau = ?";

		try (Connection con = ConnectDB.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, ma);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String maTau = rs.getString("maTau");
				String tenTau = rs.getString("tenTau");
				int soToa = rs.getInt("soToa");

				// Lấy trạng thái từ Enum TrangThaiTau
				// Giả định database lưu chuỗi 'HOATDONG' hoặc 'NGUNGHOATDONG'
				String tinhTrangStr = rs.getString("trangThai");
				TrangThaiTau trangThai = TrangThaiTau.valueOf(tinhTrangStr);

				tau = new Tau(maTau, tenTau, soToa, trangThai);
			}
		} catch (Exception e) {
			System.err.println("Lỗi khi lấy thông tin tàu theo mã: " + e.getMessage());
			e.printStackTrace();
		}

		return tau;
	}

	public boolean insertTau(Tau t) {
		String sql = "INSERT INTO Tau VALUES(?, ?, ?, ?)";
		try (Connection conn = ConnectDB.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, t.getMaTau());
			ps.setString(2, t.getTenTau());
			ps.setInt(3, t.getSoToa());
			ps.setString(4, t.getTrangThaiTau().name()); // Chuyển Enum thành String để lưu vào DB
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean updateTau(Tau t) {
		String sql = "UPDATE Tau SET tenTau = ?, soToa = ?, trangThai = ? WHERE maTau = ?";
		try (Connection conn = ConnectDB.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, t.getTenTau());
			ps.setInt(2, t.getSoToa());
			ps.setString(3, t.getTrangThaiTau().name());
			ps.setString(4, t.getMaTau());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateTrangThaiTau(String maTau, TrangThaiTau status) {
		String sql = "UPDATE Tau SET trangThai = ? WHERE maTau = ?";
		try (Connection con = ConnectDB.getConnection();
			 PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, status.name()); // Lưu tên Enum (HOATDONG, NGUNGHOATDONG...)
			ps.setString(2, maTau);

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// TÌM KIẾM (Theo Mã hoặc Tên)
	public List<Tau> searchTau(String keyword) {
		List<Tau> list = new ArrayList<>();
		String sql = "SELECT * FROM Tau WHERE maTau LIKE ? OR tenTau LIKE ?";
		try (Connection conn = ConnectDB.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, "%" + keyword + "%");
			ps.setString(2, "%" + keyword + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new Tau(
						rs.getString("maTau"),
						rs.getString("tenTau"),
						rs.getInt("soToa"),
						TrangThaiTau.valueOf(rs.getString("trangThai"))
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}