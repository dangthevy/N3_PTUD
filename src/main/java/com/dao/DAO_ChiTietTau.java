package com.dao;

import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.*;

public class DAO_ChiTietTau {
	public boolean ganToaVaoTau(String maTau, String maToa, int thuTu) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietTau VALUES (?,?,?)")) {
			ps.setString(1, maTau);
			ps.setString(2, maToa);
			ps.setInt(3, thuTu);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean goToaKhoiTau(String maTau, String maToa) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietTau WHERE maTau=? AND maToa=?")) {
			ps.setString(1, maTau);
			ps.setString(2, maToa);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean hoanDoiThuTu(String maTau, String maA, int thuTuA, String maB, int thuTuB) {
		Connection c = null;
		try {
			c = ConnectDB.getConnection();
			c.setAutoCommit(false); // Bắt đầu Transaction

			PreparedStatement ps = c.prepareStatement("UPDATE ChiTietTau SET thuTu=? WHERE maTau=? AND maToa=?");
			// Update toa B
			ps.setInt(1, thuTuB);
			ps.setString(2, maTau);
			ps.setString(3, maA);
			ps.executeUpdate();
			// Update toa A
			ps.setInt(1, thuTuA);
			ps.setString(2, maTau);
			ps.setString(3, maB);
			ps.executeUpdate();

			c.commit(); // Thành công thì lưu
			return true;
		} catch (Exception ex) {
			if (c != null)
				try {
					c.rollback();
				} catch (SQLException e) {
				} // Lỗi thì quay lui dữ liệu
			return false;
		} finally {
			if (c != null)
				try {
					c.setAutoCommit(true);
				} catch (SQLException e) {
				}
		}
	}

	public boolean capNhatThuTuSauKhiGo(String maTau, List<String> dsMaToaConLai) {
		Connection c = null;
		try {
			c = ConnectDB.getConnection();
			c.setAutoCommit(false); // Bắt đầu Transaction

			PreparedStatement p2 = c.prepareStatement("UPDATE ChiTietTau SET thuTu=? WHERE maTau=? AND maToa=?");
			for (int i = 0; i < dsMaToaConLai.size(); i++) {
				p2.setInt(1, i + 1);
				p2.setString(2, maTau);
				p2.setString(3, dsMaToaConLai.get(i));
				p2.executeUpdate();
			}

			c.commit();
			return true;
		} catch (Exception ex) {
			if (c != null)
				try {
					c.rollback();
				} catch (SQLException e) {
				}
			return false;
		} finally {
			if (c != null)
				try {
					c.setAutoCommit(true);
				} catch (SQLException e) {
				}
		}
	}

	public Set<String> getGheDaDat(String maToa) {
		Set<String> bookedSeats = new HashSet<>();
		try (Connection c = ConnectDB.getConnection();
				ResultSet rs = c.createStatement().executeQuery("SELECT DISTINCT viTri FROM GheLichTrinh WHERE maToa='"
						+ maToa + "' AND trangThai != 'TRONG'")) {
			while (rs.next())
				bookedSeats.add(rs.getString(1).trim());
		} catch (Exception e) {
		}
		return bookedSeats;
	}

	public List<Object[]> getToaOfTau(String maTau) {
		List<Object[]> list = new ArrayList<>();
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement(
						"SELECT c.thuTu, t.maToa, t.tenToa, l.tenLoaiToa, t.soGhe FROM ChiTietTau c JOIN Toa t ON c.maToa=t.maToa JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE c.maTau=? ORDER BY c.thuTu")) {
			ps.setString(1, maTau);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
				list.add(
						new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5) });
		} catch (Exception ex) {
		}
		return list;
	}

	public boolean autoSinhToaTransaction(String maTau, List<Object[]> toaToInsert) {
		try (Connection c = ConnectDB.getConnection()) {
			c.setAutoCommit(false);
			PreparedStatement psToa = c.prepareStatement(
					"INSERT INTO Toa (maToa, tenToa, soGhe, maLoaiToa, trangThai) VALUES (?,?,?,?,'SAN_SANG')");
			PreparedStatement psGan = c.prepareStatement("INSERT INTO ChiTietTau (maTau, maToa, thuTu) VALUES (?,?,?)");

			String lastMa = "TOA0000";
			try (Statement st = c.createStatement();
					ResultSet rs = st
							.executeQuery("SELECT TOP 1 maToa FROM Toa WHERE maToa LIKE 'TOA%' ORDER BY maToa DESC")) {
				if (rs.next())
					lastMa = rs.getString(1);
			}
			int currentNum = Integer.parseInt(lastMa.substring(3));

			for (Object[] data : toaToInsert) {
				currentNum++;
				String newMaToa = String.format("TOA%04d", currentNum);
				int ghe = (int) data[0];
				String maLoaiToa = (String) data[1];
				int thuTu = (int) data[2];
				String tenToaChuyenDung = (String) data[3];

				psToa.setString(1, newMaToa);
				psToa.setString(2, tenToaChuyenDung);
				psToa.setInt(3, ghe);
				psToa.setString(4, maLoaiToa);
				psToa.executeUpdate();
				psGan.setString(1, maTau);
				psGan.setString(2, newMaToa);
				psGan.setInt(3, thuTu);
				psGan.executeUpdate();
			}
			c.commit();
			c.setAutoCommit(true);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	// Hàm check xem Toa này có vé nào đã bán trong CÁC CHUYẾN ĐI TƯƠNG LAI không
		public boolean checkKhongCoVeDaBan(String maTau, String maToa) {
			String sql = "SELECT COUNT(*) FROM GheLichTrinh gl "
					+ "JOIN LichTrinh lt ON gl.maLT = lt.maLT "
					+ "JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen "
					+ "WHERE ct.maTau = ? AND gl.maToa = ? AND gl.trangThai IN ('DADAT', 'GIUCHO') "
					+ "AND (lt.ngayKhoiHanh > CAST(GETDATE() AS DATE) OR (lt.ngayKhoiHanh = CAST(GETDATE() AS DATE) AND lt.gioKhoiHanh > CAST(GETDATE() AS TIME)))";
			try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setString(1, maTau);
				ps.setString(2, maToa);
				ResultSet rs = ps.executeQuery();
				if (rs.next() && rs.getInt(1) > 0) return false; // Có vé đã bán -> Không cho gỡ
			} catch (Exception e) {}
			return true; // An toàn, cho phép gỡ
		}
}