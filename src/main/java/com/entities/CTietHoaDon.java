package com.entities;

public class CTietHoaDon {
	private HoaDon hoaDon; // Thuộc hóa đơn nào
	private Ve ve; // Vé cụ thể nào
	private double donGia; // Giá vé tại thời điểm bán

	public CTietHoaDon() {
	}

	public CTietHoaDon(HoaDon hoaDon, Ve ve, double donGia) {
		this.hoaDon = hoaDon;
		this.ve = ve;
		this.donGia = donGia;
	}

	// Thành tiền của một dòng trong hóa đơn
	public double tinhThanhTien() {
		return donGia;
	}

	// Getters and Setters
	public HoaDon getHoaDon() {
		return hoaDon;
	}

	public void setHoaDon(HoaDon hoaDon) {
		this.hoaDon = hoaDon;
	}

	public Ve getVe() {
		return ve;
	}

	public void setVe(Ve ve) {
		this.ve = ve;
	}

	public double getDonGia() {
		return donGia;
	}

	public void setDonGia(double donGia) {
		this.donGia = donGia;
	}

	@Override
	public String toString() {
		return "CTietHoaDon [hoaDon=" + hoaDon + ", ve=" + ve + ", donGia=" + donGia + "]";
	}

}