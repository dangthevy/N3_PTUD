package com.entities;

import java.util.Date;
import java.util.List;

public class HoaDon {
	private String maHD;
	private Date ngayLap;
	private NhanVien nhanVien;     // Quan hệ N-1: Nhiều Hóa đơn do 1 Nhân viên lập
	private KhachHang khachHang;   // Quan hệ N-1: Nhiều Hóa đơn thuộc 1 Khách hàng

	// Chỉ lưu Tổng Tiền Cuối Cùng (theo cấu trúc DB mới)
	private double tongTien;

	// Quan hệ 1-N: Một hóa đơn có nhiều chi tiết hóa đơn (vé)
	private List<CTietHoaDon> danhSachChiTiet;

	public HoaDon() {
		this.ngayLap = new Date(); // Gán ngay ngày hiện tại khi vừa tạo đối tượng
		this.tongTien = 0;
	}

	public HoaDon(String maHD, Date ngayLap, NhanVien nhanVien, KhachHang khachHang, double tongTien) {
		this.maHD = maHD;
		this.ngayLap = ngayLap;
		this.nhanVien = nhanVien;
		this.khachHang = khachHang;
		this.tongTien = tongTien;
	}

	// Hàm tính toán logic (Business Logic) dựa theo Item-Level Discount
	public double tinhTongTien() {
		double total = 0;
		if (this.danhSachChiTiet != null) {
			for (CTietHoaDon cthd : danhSachChiTiet) {
				total += cthd.getThanhTien();
			}
		}
		this.tongTien = total;
		return total;
	}

	// ================= GETTERS & SETTERS =================
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

	public double getTongTien() {
		return tongTien;
	}

	public void setTongTien(double tongTien) {
		this.tongTien = tongTien;
	}

	public List<CTietHoaDon> getDanhSachChiTiet() {
		return danhSachChiTiet;
	}

	public void setDanhSachChiTiet(List<CTietHoaDon> danhSachChiTiet) {
		this.danhSachChiTiet = danhSachChiTiet;
	}
}