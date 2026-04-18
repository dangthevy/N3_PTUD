package com.dao;

import com.connectDB.ConnectDB;
import com.entities.LoaiToa;
import java.sql.*;
import java.util.*;

public class DAO_LoaiToa {
	public List<LoaiToa> getAllLoaiToa() {
		List<LoaiToa> ds = new ArrayList<>();
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT * FROM LoaiToa");
				ResultSet rs = ps.executeQuery()) {
			while (rs.next())
				ds.add(new LoaiToa(rs.getString("maLoaiToa"), rs.getString("tenLoaiToa"), rs.getInt("soHang"),
						rs.getInt("soCot"), rs.getString("kieuHienThi")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}

	public boolean insertLoaiToa(LoaiToa lt) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO LoaiToa VALUES(?,?,?,?,?)")) {
			ps.setString(1, lt.getMaLoaiToa());
			ps.setString(2, lt.getTenLoaiToa());
			ps.setInt(3, lt.getSoHang());
			ps.setInt(4, lt.getSoCot());
			ps.setString(5, lt.getKieuHienThi());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean updateLoaiToa(LoaiToa lt) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"UPDATE LoaiToa SET tenLoaiToa=?, soHang=?, soCot=?, kieuHienThi=? WHERE maLoaiToa=?")) {
			ps.setString(1, lt.getTenLoaiToa());
			ps.setInt(2, lt.getSoHang());
			ps.setInt(3, lt.getSoCot());
			ps.setString(4, lt.getKieuHienThi());
			ps.setString(5, lt.getMaLoaiToa());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean deleteLoaiToa(String maLoai) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM LoaiToa WHERE maLoaiToa=?")) {
			ps.setString(1, maLoai);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}
}
