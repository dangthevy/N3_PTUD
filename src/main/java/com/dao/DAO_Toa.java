package com.dao;

import com.connectDB.ConnectDB;
import com.entities.*;
import java.sql.*;
import java.util.*;

public class DAO_Toa {
	public String phatSinhMaToa() {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con
						.prepareStatement("SELECT TOP 1 maToa FROM Toa WHERE maToa LIKE 'TOA%' ORDER BY maToa DESC");
				ResultSet rs = ps.executeQuery()) {
			if (rs.next())
				return String.format("TOA%04d", Integer.parseInt(rs.getString("maToa").substring(3)) + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "TOA0001";
	}

	public Toa getToaById(String maToa) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"SELECT t.*, l.tenLoaiToa, l.soHang, l.soCot, l.kieuHienThi FROM Toa t JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE t.maToa=?")) {
			ps.setString(1, maToa);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				LoaiToa lt = new LoaiToa(rs.getString("maLoaiToa"), rs.getString("tenLoaiToa"), rs.getInt("soHang"),
						rs.getInt("soCot"), rs.getString("kieuHienThi"));
				return new Toa(maToa, rs.getString("tenToa"), rs.getInt("soGhe"), lt, rs.getString("trangThai"));
			}
		} catch (Exception e) {
		}
		return null;
	}

	public boolean insertToa(Toa t) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"INSERT INTO Toa (maToa, tenToa, soGhe, maLoaiToa, trangThai) VALUES (?,?,?,?,'SAN_SANG')")) {
			ps.setString(1, t.getMaToa());
			ps.setString(2, t.getTenToa());
			ps.setInt(3, t.getSoGhe());
			ps.setString(4, t.getLoaiToa().getMaLoaiToa());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean updateToa(Toa t) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con
						.prepareStatement("UPDATE Toa SET tenToa=?, soGhe=?, maLoaiToa=? WHERE maToa=?")) {
			ps.setString(1, t.getTenToa());
			ps.setInt(2, t.getSoGhe());
			ps.setString(3, t.getLoaiToa().getMaLoaiToa());
			ps.setString(4, t.getMaToa());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean deleteToa(String maToa) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM Toa WHERE maToa=?")) {
			ps.setString(1, maToa);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean updateTrangThai(String maToa, String trangThai) {
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement("UPDATE Toa SET trangThai=? WHERE maToa=?")) {
			ps.setString(1, trangThai);
			ps.setString(2, maToa);
			return ps.executeUpdate() > 0;
		} catch (Exception ex) {
			return false;
		}
	}

	public List<Object[]> getToaTrongKhoSanSang() {
		List<Object[]> list = new ArrayList<>();
		try (Connection c = ConnectDB.getConnection();
				ResultSet rs = c.createStatement().executeQuery(
						"SELECT t.maToa, t.tenToa, t.soGhe, l.tenLoaiToa FROM Toa t JOIN LoaiToa l ON t.maLoaiToa = l.maLoaiToa WHERE t.maToa NOT IN (SELECT maToa FROM ChiTietTau) AND t.trangThai='SAN_SANG' ORDER BY t.maToa")) {
			while (rs.next())
				list.add(new Object[] { rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4) });
		} catch (Exception ex) {
		}
		return list;
	}

	public List<Object[]> getAllToaWithViTri() {
		List<Object[]> list = new ArrayList<>();
		try (Connection c = ConnectDB.getConnection();
				ResultSet rs = c.createStatement().executeQuery(
						"SELECT t.maToa, t.tenToa, l.tenLoaiToa, t.soGhe, t.trangThai, c.maTau FROM Toa t JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa LEFT JOIN ChiTietTau c ON t.maToa=c.maToa")) {
			while (rs.next())
				list.add(new Object[] { rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4),
						rs.getString(5), rs.getString(6) });
		} catch (Exception ex) {
		}
		return list;
	}

	public Object[] getThongTinToaForMap(String maToa) {
		try (Connection c = ConnectDB.getConnection();
				PreparedStatement ps = c.prepareStatement(
						"SELECT l.soHang, l.soCot, l.kieuHienThi, t.soGhe FROM Toa t JOIN LoaiToa l ON t.maLoaiToa=l.maLoaiToa WHERE t.maToa=?")) {
			ps.setString(1, maToa);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return new Object[] { rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4) };
		} catch (Exception e) {
		}
		return null;
	}
}