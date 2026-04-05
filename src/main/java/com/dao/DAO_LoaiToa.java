package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entities.LoaiToa;

public class DAO_LoaiToa {
	public List<LoaiToa> getAllLoaiToa() {
	    List<LoaiToa> ds = new ArrayList<>();
	    String sql = "SELECT * FROM LoaiToa";
	    try (Connection con = ConnectDB.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql);
	         ResultSet rs = ps.executeQuery()) {
	        while (rs.next()) {
	            ds.add(new LoaiToa(rs.getString("maLoaiToa"), rs.getString("tenLoaiToa")));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return ds;
	}
}
