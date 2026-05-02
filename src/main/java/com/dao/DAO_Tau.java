package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.connectDB.ConnectDB;
import com.entities.Tau;
import com.enums.TrangThaiTau;

public class DAO_Tau {
	public String phatSinhMaTau() {
		String sql = "SELECT TOP 1 maTau FROM Tau ORDER BY maTau DESC";
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				String lastMa = rs.getString("maTau");
				return String.format("TAU%04d", Integer.parseInt(lastMa.substring(3)) + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "TAU0001";
	}

	public List<Tau> getAllTau() {
		List<Tau> dsTau = new ArrayList<>();
		try (Connection conn = ConnectDB.getConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM Tau");
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String dbStatus = rs.getString("trangThai");
				TrangThaiTau statusEnum = TrangThaiTau.HOATDONG;
				try {
					if (dbStatus != null && !dbStatus.trim().isEmpty())
						statusEnum = TrangThaiTau.valueOf(dbStatus.trim().toUpperCase());
				} catch (Exception ex) {
				}
				dsTau.add(new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soToa"), statusEnum));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dsTau;
	}

	public Tau getTauByMa(String ma) {
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT * FROM Tau WHERE maTau = ?")) {
			ps.setString(1, ma);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String dbStatus = rs.getString("trangThai");
				TrangThaiTau statusEnum = TrangThaiTau.HOATDONG;
				try {
					if (dbStatus != null)
						statusEnum = TrangThaiTau.valueOf(dbStatus.trim().toUpperCase());
				} catch (Exception ex) {
				}
				return new Tau(rs.getString("maTau"), rs.getString("tenTau"), rs.getInt("soToa"), statusEnum);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean insertTau(Tau t) {
		try (Connection conn = ConnectDB.getConnection();
				PreparedStatement ps = conn.prepareStatement("INSERT INTO Tau VALUES(?, ?, ?, ?)")) {
			ps.setString(1, t.getMaTau());
			ps.setString(2, t.getTenTau());
			ps.setInt(3, t.getSoToa());
			ps.setString(4, t.getTrangThaiTau().name());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean updateTau(Tau t) {
		try (Connection conn = ConnectDB.getConnection();
				PreparedStatement ps = conn
						.prepareStatement("UPDATE Tau SET tenTau = ?, soToa = ?, trangThai = ? WHERE maTau = ?")) {
			ps.setString(1, t.getTenTau());
			ps.setInt(2, t.getSoToa());
			ps.setString(3, t.getTrangThaiTau().name());
			ps.setString(4, t.getMaTau());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			return false;
		}
	}
}