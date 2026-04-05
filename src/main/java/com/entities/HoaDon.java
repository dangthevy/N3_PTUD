package com.entities;

import java.util.Date;
import java.util.List;

import com.enums.PTThanhToan;

public class HoaDon {
	private String maHD;
	private Date ngayLap;
	private NhanVien nhanVien; // Quan hệ 1-1: Nhân viên lập hóa đơn
	private KhachHang khachHang; // Quan hệ 1-1: Khách hàng mua vé
	private KhuyenMai khuyenMai; // Có thể null nếu không có khuyến mãi
	private PTThanhToan ptt; // Phương thức thanh toán (TIENMAT, CHUYENKHOAN)
	private double thueVAT;
	private double tongTienGoc; // Tổng tiền trước khi giảm giá
	private double thanhTien; // Tổng tiền cuối cùng sau KM và Thuế

	public KhuyenMai getKhuyenMai() {
		return khuyenMai;
	}

	public void setKhuyenMai(KhuyenMai khuyenMai) {
		this.khuyenMai = khuyenMai;
	}

	public PTThanhToan getPtt() {
		return ptt;
	}

	public void setPtt(PTThanhToan ptt) {
		this.ptt = ptt;
	}

	public double getThueVAT() {
		return thueVAT;
	}

	public void setThueVAT(double thueVAT) {
		this.thueVAT = thueVAT;
	}

	public double getTongTienGoc() {
		return tongTienGoc;
	}

	public void setTongTienGoc(double tongTienGoc) {
		this.tongTienGoc = tongTienGoc;
	}

	public HoaDon() {
		this.ngayLap = new Date(); // Gán ngay ngày hiện tại khi vừa tạo đối tượng
	}

	public HoaDon(String maHD, Date ngayLap, NhanVien nhanVien, KhachHang khachHang, KhuyenMai khuyenMai,
			PTThanhToan ptt, double thueVAT) {
		this.maHD = maHD;
		this.ngayLap = ngayLap;
		this.nhanVien = nhanVien;
		this.khachHang = khachHang;
		this.khuyenMai = khuyenMai;
		this.ptt = ptt;
		this.thueVAT = thueVAT;
	}

	// Hàm tính toán logic (Business Logic)
	public double tinhTongTien() {
		// thueVAT thường là 0.1 (10%)
		// thanhTien = (tongTienGoc * (1 - tyLeGiam)) * (1 + thueVAT)
		return thanhTien;
	}

	// Getters and Setters
	public String getMaHD() {
		return maHD;
	}

	public void setMaHD(String maHD) {
		this.maHD = maHD;
	}

	public Date getNgayLap() {
		return ngayLap;
	}

	public void setNgayLap(Date ngayLap) {
		this.ngayLap = ngayLap;
	}

	public NhanVien getNhanVien() {
		return nhanVien;
	}

	public void setNhanVien(NhanVien nhanVien) {
		this.nhanVien = nhanVien;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public double getThanhTien() {
		return thanhTien;
	}

	public void setThanhTien(double thanhTien) {
		this.thanhTien = thanhTien;
	}
	// ... (Các getter/setter khác tự tạo tương tự)
}