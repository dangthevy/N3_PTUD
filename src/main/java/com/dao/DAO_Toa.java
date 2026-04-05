package com.dao;

import com.connectDB.ConnectDB;
import com.entities.LoaiToa;
import com.entities.Tau;
import com.entities.Toa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_Toa {

    // --- 1. HÀM PHÁT SINH MÃ TỰ ĐỘNG ---
    public String phatSinhMaToa() {
        String ma = "TOA001";
        // Lấy mã có số thứ tự lớn nhất
        String sql = "SELECT TOP 1 maToa FROM Toa ORDER BY maToa DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String lastMa = rs.getString("maToa");
                // Cắt chuỗi "TOA005" lấy số 5, tăng lên 1 thành 6
                int number = Integer.parseInt(lastMa.substring(3)) + 1;
                ma = String.format("TOA%03d", number);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ma;
    }

    // --- HÀM THÊM TOA MỚI ---
    public boolean insertToa(Toa toa) {
        // Sửa maLoai -> maLoaiToa
        String sql = "INSERT INTO Toa (maToa, tenToa, soGhe, maTau, maLoaiToa) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toa.getMaToa());
            ps.setString(2, toa.getTenToa());
            ps.setInt(3, toa.getSoGhe());
            ps.setString(4, toa.getTau().getMaTau());
            ps.setString(5, toa.getLoaiToa().getMaLoaiToa()); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- HÀM LẤY DANH SÁCH THEO MÀ TÀU ---
    public List<Toa> getToaByMaTau(String maTau) {
        List<Toa> ds = new ArrayList<>();
        // Sắp xếp theo độ dài tên toa trước, sau đó mới đến giá trị tên toa
        // Cách này giúp "Toa 2" luôn đứng trước "Toa 10"
        String sql = "SELECT * FROM Toa t " +
                     "JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa " +
                     "WHERE maTau = ? " +
                     "ORDER BY LEN(tenToa) ASC, tenToa ASC"; 
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTau);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Toa t = new Toa();
                t.setMaToa(rs.getString("maToa"));
                t.setTenToa(rs.getString("tenToa"));
                t.setSoGhe(rs.getInt("soGhe"));
                
                LoaiToa lt = new LoaiToa();
                lt.setMaLoaiToa(rs.getString("maLoaiToa"));
                lt.setTenLoaiToa(rs.getString("tenLoaiToa"));
                t.setLoaiToa(lt);
                
                ds.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // --- 3. HÀM CẬP NHẬT TÊN TOA ---
    public boolean updateToa(Toa toa) {
        String sql = "UPDATE Toa SET tenToa = ? WHERE maToa = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, toa.getTenToa());
            ps.setString(2, toa.getMaToa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

//    // --- 4. HÀM XÓA TOA ---
//    public boolean deleteToa(String maToa) {
//        // Lưu ý: Trong SQL nên để ON DELETE CASCADE ở bảng ChoNgoi 
//        // Nếu không, bạn phải xóa hết Chỗ Ngồi của toa đó trước khi xóa Toa.
//        String sql = "DELETE FROM Toa WHERE maToa = ?";
//        try (Connection con = ConnectDB.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setString(1, maToa);
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            // Lỗi này thường xảy ra nếu còn ràng buộc khóa ngoại (vẫn còn vé đã đặt ở toa này)
//            System.err.println("Lỗi ràng buộc: Không thể xóa toa đang có dữ liệu liên quan.");
//        }
//        return false;
//    }

    // 5. Lấy thông tin chi tiết 1 Toa theo Mã (Dùng cho chức năng Sửa)
    public Toa getToaById(String maToa) {
        String sql = "SELECT t.*, lt.tenLoaiToa FROM Toa t " +
                     "JOIN LoaiToa lt ON t.maLoaiToa = lt.maLoaiToa " +
                     "WHERE t.maToa = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maToa);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Toa t = new Toa();
                t.setMaToa(rs.getString("maToa"));
                t.setTenToa(rs.getString("tenToa"));
                t.setSoGhe(rs.getInt("soGhe"));
                
                // Gán Loại Toa
                LoaiToa lt = new LoaiToa();
                lt.setMaLoaiToa(rs.getString("maLoaiToa"));
                lt.setTenLoaiToa(rs.getString("tenLoaiToa"));
                t.setLoaiToa(lt);
                
                // Gán Tàu (Chỉ cần mã để Form biết thuộc tàu nào)
                Tau tau = new Tau();
                tau.setMaTau(rs.getString("maTau"));
                t.setTau(tau);
                
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}