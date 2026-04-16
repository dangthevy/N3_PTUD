package com.dao;

import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_Ve {

	// ===================== QUERY =====================

	/**
	 * Lấy tất cả vé, có thể lọc theo mã vé hoặc mã khách hàng.
	 * 
	 * @param maVeFilter null hoặc chuỗi tìm kiếm theo maVe
	 * @param maKHFilter null hoặc chuỗi tìm kiếm theo maKH
	 */
	public ResultSet getDanhSachVe(String maVeFilter, String maKHFilter) throws SQLException {
		Connection conn = ConnectDB.getConnection();
		if (conn == null)
			return null;

		StringBuilder sql = new StringBuilder("SELECT v.maVe, v.maKH, k.tenKH, v.maLT, v.maToa, v.viTriGhe, "
				+ "       lv.tenLoai AS tenLoaiVe, v.giaVe, v.trangThaiVe " + "FROM Ve v "
				+ "LEFT JOIN KhachHang k  ON v.maKH     = k.maKH " + "LEFT JOIN LoaiVe    lv ON v.maLoaiVe = lv.maLoai "
				+ "WHERE 1=1 ");

		List<Object> params = new ArrayList<>();

		if (maVeFilter != null && !maVeFilter.trim().isEmpty()) {
			sql.append("AND v.maVe LIKE ? ");
			params.add("%" + maVeFilter.trim() + "%");
		}
		if (maKHFilter != null && !maKHFilter.trim().isEmpty()) {
			sql.append("AND (v.maKH LIKE ? OR k.tenKH LIKE ?) ");
			params.add("%" + maKHFilter.trim() + "%");
			params.add("%" + maKHFilter.trim() + "%");
		}
		sql.append("ORDER BY v.maVe");

		PreparedStatement ps = conn.prepareStatement(sql.toString());
		for (int i = 0; i < params.size(); i++) {
			ps.setObject(i + 1, params.get(i));
		}
		return ps.executeQuery();
	}

	/**
	 * Lấy thông tin chi tiết một vé (JOIN đầy đủ).
	 */
	public ResultSet getChiTietVe(String maVe) throws SQLException {
		Connection conn = ConnectDB.getConnection();
		if (conn == null)
			return null;

		String sql = "SELECT v.maVe, v.maKH, k.tenKH, k.sdt, k.cccd, k.email, "
				+ "       v.maLT, lt.ngayKhoiHanh, lt.gioKhoiHanh, " + "       ct.tenChuyen, t.tenTuyen, "
				+ "       v.maToa, toa.tenToa, lt2.tenLoaiToa, " + "       v.viTriGhe, "
				+ "       lv.tenLoai AS tenLoaiVe, " + "       v.giaVe, v.trangThaiVe " + "FROM Ve v "
				+ "LEFT JOIN KhachHang  k   ON v.maKH     = k.maKH "
				+ "LEFT JOIN LichTrinh  lt  ON v.maLT      = lt.maLT "
				+ "LEFT JOIN ChuyenTau  ct  ON lt.maChuyen = ct.maChuyen "
				+ "LEFT JOIN Tuyen      t   ON ct.maTuyen  = t.maTuyen "
				+ "LEFT JOIN Toa        toa ON v.maToa     = toa.maToa "
				+ "LEFT JOIN LoaiToa    lt2 ON toa.maLoaiToa = lt2.maLoaiToa "
				+ "LEFT JOIN LoaiVe     lv  ON v.maLoaiVe  = lv.maLoai " + "WHERE v.maVe = ?";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, maVe);
		return ps.executeQuery();
	}

	/**
	 * Đếm tổng vé và số vé đã hoàn (trangThaiVe = 'DAHOAN').
	 * 
	 * @return int[]{tongVe, veHoan}
	 */
	public int[] demTongVeVaVeHoan() {
		int[] result = { 0, 0 };
		try (Connection conn = ConnectDB.getConnection()) {
			if (conn == null)
				return result;
			String sql = "SELECT COUNT(*) AS tongVe, "
					+ "SUM(CASE WHEN trangThaiVe = 'DAHOAN' THEN 1 ELSE 0 END) AS veHoan " + "FROM Ve";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				result[0] = rs.getInt("tongVe");
				result[1] = rs.getInt("veHoan");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	// lấy thông tin khởi hành để tính phí hoàn vé
	public Object[] getThongTinHoanVe(String maVe) {
		String sql = "SELECT v.giaVe, lt.ngayKhoiHanh, lt.gioKhoiHanh " + "FROM Ve v "
				+ "JOIN LichTrinh lt ON v.maLT = lt.maLT " + "WHERE v.maVe = ?";
		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new Object[] { rs.getDouble("giaVe"), rs.getDate("ngayKhoiHanh"),
							rs.getTime("gioKhoiHanh") };
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Hoàn vé: cập nhật trangThaiVe = 'DAHOAN' Đồng thời cập nhật GheLichTrinh về
	 * TRONG để có thể đặt lại.
	 *
	 * @return true nếu thành công
	 */
	public boolean hoanVe(String maVe) {
		try (Connection conn = ConnectDB.getConnection()) {
			if (conn == null)
				return false;
			conn.setAutoCommit(false);

			try {
				// 1. Cập nhật trạng thái vé
				String sqlVe = "UPDATE Ve SET trangThaiVe = 'DAHOAN' WHERE maVe = ? AND trangThaiVe = 'CHUASUDUNG'";
				PreparedStatement ps1 = conn.prepareStatement(sqlVe);
				ps1.setString(1, maVe);
				int updated = ps1.executeUpdate();
				if (updated == 0) {
					conn.rollback();
					return false; // Vé không tồn tại hoặc không phải CHUASUDUNG
				}
				ps1.close();

				// 2. Lấy maLT, maToa, viTriGhe của vé vừa hoàn
				String sqlGet = "SELECT maLT, maToa, viTriGhe FROM Ve WHERE maVe = ?";
				PreparedStatement ps2 = conn.prepareStatement(sqlGet);
				ps2.setString(1, maVe);
				ResultSet rs = ps2.executeQuery();
				if (rs.next()) {
					String maLT = rs.getString("maLT");
					String maToa = rs.getString("maToa");
					String viTri = rs.getString("viTriGhe");

					// 3. Cập nhật GheLichTrinh trả ghế về TRONG
					String sqlGhe = "UPDATE GheLichTrinh SET trangThai = 'TRONG' "
							+ "WHERE maLT = ? AND maToa = ? AND viTri = ?";
					PreparedStatement ps3 = conn.prepareStatement(sqlGhe);
					ps3.setString(1, maLT);
					ps3.setString(2, maToa);
					ps3.setString(3, viTri);
					ps3.executeUpdate();
					ps3.close();
				}
				rs.close();
				ps2.close();

				conn.commit();
				return true;

			} catch (SQLException e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Lấy giá vé và thời điểm tàu chạy dưới dạng miliseconds
	 * 
	 * @param maVe Mã vé cần lấy thông tin
	 * @return double array: [0] = giá vé, [1] = epoch miliseconds của giờ khởi hành
	 */
	public double[] getGiaVeVaGioTau(String maVe) {
		double[] result = null;
		// Query lấy giá vé và thời gian khởi hành từ bảng Ve và LichTrinh
		String sql = "SELECT v.giaVe, lt.ngayKhoiHanh, lt.gioKhoiHanh " + "FROM Ve v "
				+ "JOIN LichTrinh lt ON v.maLT = lt.maLT " + "WHERE v.maVe = ?";

		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					double giaVe = rs.getDouble("giaVe");
					Date ngayKH = rs.getDate("ngayKhoiHanh");
					Time gioKH = rs.getTime("gioKhoiHanh");

					// Sử dụng Calendar để gộp Ngày và Giờ lại thành một mốc thời gian duy nhất
					java.util.Calendar cal = java.util.Calendar.getInstance();
					cal.setTime(ngayKH); // Đặt ngày

					java.util.Calendar timeCal = java.util.Calendar.getInstance();
					timeCal.setTime(gioKH); // Lấy giờ từ Time

					cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
					cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
					cal.set(java.util.Calendar.SECOND, timeCal.get(java.util.Calendar.SECOND));
					cal.set(java.util.Calendar.MILLISECOND, 0);

					long epochMillis = cal.getTimeInMillis();

					result = new double[] { giaVe, (double) epochMillis };
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}