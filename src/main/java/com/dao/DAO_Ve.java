package com.dao;

import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_Ve {

	// ===================== QUERY =====================

	/**
	 * Lấy tất cả vé, có thể lọc theo mã vé hoặc mã khách hàng.
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
		for (int i = 0; i < params.size(); i++)
			ps.setObject(i + 1, params.get(i));
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
				+ "LEFT JOIN KhachHang  k   ON v.maKH      = k.maKH "
				+ "LEFT JOIN LichTrinh  lt  ON v.maLT       = lt.maLT "
				+ "LEFT JOIN ChuyenTau  ct  ON lt.maChuyen  = ct.maChuyen "
				+ "LEFT JOIN Tuyen      t   ON ct.maTuyen   = t.maTuyen "
				+ "LEFT JOIN Toa        toa ON v.maToa      = toa.maToa "
				+ "LEFT JOIN LoaiToa    lt2 ON toa.maLoaiToa= lt2.maLoaiToa "
				+ "LEFT JOIN LoaiVe     lv  ON v.maLoaiVe   = lv.maLoai " + "WHERE v.maVe = ?";

		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, maVe);
		return ps.executeQuery();
	}


	/**
	 * Model đơn giản chứa thông tin 1 dòng khuyến mãi áp dụng cho vé.
	 */
	public static class KhuyenMaiInfo {
		public String maKMDetail;
		public String tenKM;
		public String loaiKM; // GIAM_PHAN_TRAM | GIAM_TIEN
		public double giaTri; // % hoặc VND
		public double tienGiamThucTe; // Số tiền thực tế được giảm
		public String moTa; // mô tả thêm (tuyến, loại toa, loại vé)
	}

	/**
	 * Lấy danh sách khuyến mãi đã áp dụng cho 1 vé (qua ChiTiet_KhuyenMai +
	 * ChiTietHoaDon).
	 */
	public List<KhuyenMaiInfo> getKhuyenMaiCuaVe(String maVe) {
		List<KhuyenMaiInfo> list = new ArrayList<>();
		String sql = "SELECT ck.MaKMDetail, km.TenKM, kmd.LoaiKM, kmd.GiaTri, " + "       ck.tienGiamCuaKM, "
				+ "       ISNULL(t.tenTuyen,'Tất cả tuyến')   AS tenTuyen, "
				+ "       ISNULL(lt.tenLoaiToa,'Tất cả loại toa') AS tenLoaiToa, "
				+ "       ISNULL(lv.tenLoai,'Tất cả loại vé') AS tenLoaiVe " + "FROM ChiTiet_KhuyenMai ck "
				+ "JOIN KhuyenMaiDetail kmd ON ck.MaKMDetail = kmd.MaKMDetail "
				+ "JOIN KhuyenMai       km  ON kmd.MaKM      = km.MaKM "
				+ "LEFT JOIN Tuyen      t   ON kmd.MaTuyen   = t.maTuyen "
				+ "LEFT JOIN LoaiToa    lt  ON kmd.maLoaiToa = lt.maLoaiToa "
				+ "LEFT JOIN LoaiVe     lv  ON kmd.MaLoai    = lv.maLoai " + "WHERE ck.maVe = ?";

		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					KhuyenMaiInfo km = new KhuyenMaiInfo();
					km.maKMDetail = rs.getString("MaKMDetail");
					km.tenKM = rs.getString("TenKM");
					km.loaiKM = rs.getString("LoaiKM");
					km.giaTri = rs.getDouble("GiaTri");
					km.tienGiamThucTe = rs.getDouble("tienGiamCuaKM");
					km.moTa = rs.getString("tenTuyen") + " | " + rs.getString("tenLoaiToa") + " | "
							+ rs.getString("tenLoaiVe");
					list.add(km);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Lấy số tiền khách đã thực trả (thanhTien) từ ChiTietHoaDon. Đây là số tiền
	 * GỐC để tính hoàn vé (đã trừ KM khi mua).
	 * 
	 * @return thanhTien, hoặc -1 nếu không tìm thấy hóa đơn
	 */
	public double getTienThanhToanThucTe(String maVe) {
		String sql = "SELECT TOP 1 thanhTien FROM ChiTietHoaDon WHERE maVe = ? ORDER BY maHD DESC";
		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getDouble("thanhTien");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1; // fallback: dùng giaVe trong bảng Ve
	}

	// ===================== THỐNG KÊ =====================

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

	// ===================== TÍNH PHÍ HOÀN VÉ =====================

	/**
	 * Tính phí hoàn vé theo khoảng cách thời gian đến giờ khởi hành.
	 *
	 * Chính sách: - Còn >= 72 giờ : hoàn 90% (phí 10%) - Còn 24–72 giờ : hoàn 75%
	 * (phí 25%) - Còn 4–24 giờ : hoàn 50% (phí 50%) - Còn < 4 giờ : không hoàn (0%)
	 *
	 * @return double[]{tienHoan, phiHoan, phanTramHoan, gioConLai}
	 */
	public double[] tinhTienHoan(String maVe) {
		String sql = "SELECT v.giaVe, lt.ngayKhoiHanh, lt.gioKhoiHanh "
				+ "FROM Ve v JOIN LichTrinh lt ON v.maLT = lt.maLT " + "WHERE v.maVe = ?";

		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					// Số tiền khách thực trả (đã áp KM khi mua)
					double tienThanhToan = getTienThanhToanThucTe(maVe);
					if (tienThanhToan < 0)
						tienThanhToan = rs.getDouble("giaVe");

					// Tính giờ còn lại đến khởi hành
					java.sql.Date ngay = rs.getDate("ngayKhoiHanh");
					java.sql.Time gio = rs.getTime("gioKhoiHanh");

					java.util.Calendar cal = java.util.Calendar.getInstance();
					cal.setTime(ngay);
					java.util.Calendar tc = java.util.Calendar.getInstance();
					tc.setTime(gio);
					cal.set(java.util.Calendar.HOUR_OF_DAY, tc.get(java.util.Calendar.HOUR_OF_DAY));
					cal.set(java.util.Calendar.MINUTE, tc.get(java.util.Calendar.MINUTE));
					cal.set(java.util.Calendar.SECOND, 0);
					cal.set(java.util.Calendar.MILLISECOND, 0);

					long diffMs = cal.getTimeInMillis() - System.currentTimeMillis();
					double gioConLai = diffMs / 3_600_000.0;

					double phanTramHoan;
					if (gioConLai >= 72)
						phanTramHoan = 0.90;
					else if (gioConLai >= 24)
						phanTramHoan = 0.75;
					else if (gioConLai >= 4)
						phanTramHoan = 0.50;
					else
						phanTramHoan = 0.00;

					double tienHoan = Math.round(tienThanhToan * phanTramHoan);
					double phiHoan = tienThanhToan - tienHoan;

					return new double[] { tienHoan, phiHoan, phanTramHoan * 100, gioConLai, tienThanhToan };
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new double[] { 0, 0, 0, 0, 0 };
	}

	// ===================== HOÀN VÉ =====================

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
					return false;
				}
				ps1.close();

				// 2. Lấy maLT, maToa, viTriGhe
				String sqlGet = "SELECT maLT, maToa, viTriGhe FROM Ve WHERE maVe = ?";
				PreparedStatement ps2 = conn.prepareStatement(sqlGet);
				ps2.setString(1, maVe);
				ResultSet rs = ps2.executeQuery();
				if (rs.next()) {
					String maLT = rs.getString("maLT");
					String maToa = rs.getString("maToa");
					String viTri = rs.getString("viTriGhe");

					// 3. Trả ghế về TRONG
					String sqlGhe = "UPDATE GheLichTrinh SET trangThai = 'TRONG' WHERE maLT = ? AND maToa = ? AND viTri = ?";
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

	// ===================== HELPERS CŨ (giữ tương thích) =====================

	public Object[] getThongTinHoanVe(String maVe) {
		String sql = "SELECT v.giaVe, lt.ngayKhoiHanh, lt.gioKhoiHanh "
				+ "FROM Ve v JOIN LichTrinh lt ON v.maLT = lt.maLT WHERE v.maVe = ?";
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

	public double[] getGiaVeVaGioTau(String maVe) {
		String sql = "SELECT v.giaVe, lt.ngayKhoiHanh, lt.gioKhoiHanh "
				+ "FROM Ve v JOIN LichTrinh lt ON v.maLT = lt.maLT WHERE v.maVe = ?";
		try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, maVe);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					double giaVe = rs.getDouble("giaVe");
					java.sql.Date ngay = rs.getDate("ngayKhoiHanh");
					java.sql.Time gioT = rs.getTime("gioKhoiHanh");
					java.util.Calendar cal = java.util.Calendar.getInstance();
					cal.setTime(ngay);
					java.util.Calendar tc = java.util.Calendar.getInstance();
					tc.setTime(gioT);
					cal.set(java.util.Calendar.HOUR_OF_DAY, tc.get(java.util.Calendar.HOUR_OF_DAY));
					cal.set(java.util.Calendar.MINUTE, tc.get(java.util.Calendar.MINUTE));
					cal.set(java.util.Calendar.SECOND, 0);
					cal.set(java.util.Calendar.MILLISECOND, 0);
					return new double[] { giaVe, (double) cal.getTimeInMillis() };
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}