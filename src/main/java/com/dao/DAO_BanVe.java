package com.dao;

import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.*;

public class DAO_BanVe {

	// 1. Lấy danh sách Ga để đưa lên ComboBox Step 1
	public Map<String, String> getDanhSachGa() {
		Map<String, String> map = new LinkedHashMap<>();
		String sql = "SELECT maGa, tenGa FROM Ga WHERE trangThai = 1";
		try (Connection c = ConnectDB.getConnection();
				Statement st = c.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) {
				map.put(rs.getString("tenGa"), rs.getString("maGa"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	// 2. Tìm Lịch trình, tính toán Tổng ghế và Ghế đã đặt để lên UI
	public List<Map<String, Object>> timChuyenTau(String maGaDi, String maGaDen, String ngayDi) {
		List<Map<String, Object>> list = new ArrayList<>();
		String sql = "SELECT t.maTau, t.tenTau, lt.gioKhoiHanh, CONVERT(VARCHAR(5), lt.ngayDen, 108) AS gioDen, lt.maLT, "
				+ "(SELECT ISNULL(SUM(toa.soGhe), 0) FROM ChiTietTau ctt JOIN Toa toa ON ctt.maToa = toa.maToa WHERE ctt.maTau = t.maTau) as tongGhe, "
				+ "(SELECT COUNT(*) FROM GheLichTrinh glt WHERE glt.maLT = lt.maLT AND glt.trangThai != 'TRONG') as gheDaDat "
				+ "FROM LichTrinh lt " + "JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen "
				+ "JOIN Tuyen ty ON ct.maTuyen = ty.maTuyen " + "JOIN Tau t ON ct.maTau = t.maTau "
				+ "WHERE ty.gaDi = ? AND ty.gaDen = ? AND lt.ngayKhoiHanh = ?";

		try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, maGaDi);
			ps.setString(2, maGaDen);
			ps.setString(3, ngayDi);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				map.put("maTau", rs.getString("maTau"));
				map.put("tenTau", rs.getString("tenTau"));
				map.put("maLT", rs.getString("maLT"));

				// Xử lý định dạng ngày tháng (VD: 16/04 06:00)
				String[] parts = ngayDi.split("-");
				String ddMM = parts.length == 3 ? parts[2] + "/" + parts[1] : "";

				String gioDi = rs.getString("gioKhoiHanh");
				String gioDen = rs.getString("gioDen");
				map.put("tgDi", ddMM + " " + (gioDi != null ? gioDi.substring(0, 5) : ""));
				map.put("tgDen", ddMM + " " + (gioDen != null ? gioDen.substring(0, 5) : ""));

				int tongGhe = rs.getInt("tongGhe");
				int gheDaDat = rs.getInt("gheDaDat");
				map.put("slDat", String.valueOf(gheDaDat));
				map.put("slTrong", String.valueOf(Math.max(0, tongGhe - gheDaDat)));

				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// 3. Lấy danh sách các Toa đang được gắn trên Tàu đó
	public List<Map<String, Object>> getDanhSachToa(String maTau) {
		List<Map<String, Object>> list = new ArrayList<>();
		String sql = "SELECT t.maToa, t.tenToa, lt.tenLoaiToa " + "FROM ChiTietTau ctt "
				+ "JOIN Toa t ON ctt.maToa = t.maToa " + "JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa "
				+ "WHERE ctt.maTau = ? ORDER BY ctt.thuTu ASC";
		try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, maTau);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				map.put("maToa", rs.getString("maToa"));
				map.put("tenToa", rs.getString("tenToa"));
				map.put("tenLoaiToa", rs.getString("tenLoaiToa"));
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	// 4. Sinh sơ đồ ghế, quét trạng thái và LẤY GIÁ VÉ TỪ BẢNG GiaDetail
	// 4. Sinh sơ đồ ghế, quét trạng thái (Đã bán + Bảo trì) và lấy Giá vé
	public List<Map<String, Object>> getDanhSachGhe(String maLT, String maToa) {
		List<Map<String, Object>> list = new ArrayList<>();
		int soGhe = 0;
		double giaVe = 0;

		// BƯỚC 1: Lấy tổng số ghế và giá vé từ bảng Toa và GiaDetail
		String sqlToaVaGia = "SELECT t.soGhe, gd.gia " + "FROM Toa t " + "JOIN LichTrinh lt ON lt.maLT = ? "
				+ "JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen "
				+ "JOIN GiaDetail gd ON gd.maTuyen = ct.maTuyen AND gd.maLoaiToa = t.maLoaiToa " + "WHERE t.maToa = ?";
		try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sqlToaVaGia)) {
			ps.setString(1, maLT);
			ps.setString(2, maToa);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				soGhe = rs.getInt("soGhe");
				giaVe = rs.getDouble("gia");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// BƯỚC 2: TẠO BỘ NHỚ LƯU TRẠNG THÁI GHẾ BỊ KHÓA (Map lưu vị trí -> Trạng thái)
		Map<String, String> lockedSeats = new HashMap<>();

		// 2.1 - Quét bảng GheLichTrinh (Tìm ghế ĐÃ BÁN / GIỮ CHỖ)
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement(
						"SELECT viTri, trangThai FROM GheLichTrinh WHERE maLT = ? AND maToa = ? AND trangThai != 'TRONG'")) {
			ps.setString(1, maLT);
			ps.setString(2, maToa);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// Đánh dấu ghế này là DADAT
				lockedSeats.put(rs.getString("viTri").trim(), rs.getString("trangThai").trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 2.2 - Quét bảng GheBaoTri (Tìm ghế BỊ HỎNG/BẢO TRÌ)
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement("SELECT viTri FROM GheBaoTri WHERE maToa = ?")) {
			ps.setString(1, maToa);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// Nếu ghế này bị hỏng, ghi đè trạng thái thành BAOTRI
				lockedSeats.put(rs.getString("viTri").trim(), "BAOTRI");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// BƯỚC 3: Phát sinh danh sách 1..N ghế để đổ lên Giao diện Step 2
		for (int i = 1; i <= soGhe; i++) {
			String viTri = String.valueOf(i);
			Map<String, Object> map = new HashMap<>();
			map.put("maCho", maToa + "_" + viTri);
			map.put("tenCho", viTri);

			// Nếu vị trí này nằm trong danh sách khóa, lấy trạng thái tương ứng (DADAT hoặc
			// BAOTRI), ngược lại là TRONG
			String trangThai = lockedSeats.getOrDefault(viTri, "TRONG");
			map.put("trangThai", trangThai);
			map.put("giaVe", giaVe);

			list.add(map);
		}

		return list;
	}
}