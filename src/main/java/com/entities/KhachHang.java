package com.entities;

import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String hoTen;
	private String sdt;
	private String cccd;
	private String email;

	// Constructor đầy đủ tham số
	public KhachHang(String maKH, String hoTen, String sdt, String cccd, String email) {
		this.maKH = maKH;
		this.hoTen = hoTen;
		this.sdt = sdt;
		this.cccd = cccd;
		this.email = email;
	}

	// Constructor mặc định
	public KhachHang() {
	}

	// Getter và Setter
	public String getMaKH() {
		return maKH;
	}

	public void setMaKH(String maKH) {
		this.maKH = maKH;
	}

	public String getHoTen() {
		return hoTen;
	}

	public void setHoTen(String hoTen) {
		this.hoTen = hoTen;
	}

	public String getSdt() {
		return sdt;
	}

	public void setSdt(String sdt) {
		this.sdt = sdt;
	}

	public String getCccd() {
		return cccd;
	}

	public void setCccd(String cccd) {
		this.cccd = cccd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// Override toString để debug nếu cần
	@Override
	public String toString() {
		return "KhachHang [maKH=" + maKH + ", hoTen=" + hoTen + ", sdt=" + sdt + "]";
	}

	// So sánh 2 khách hàng dựa trên mã hoặc CCCD (hữu ích cho việc kiểm tra trùng)
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		KhachHang khachHang = (KhachHang) o;
		return Objects.equals(maKH, khachHang.maKH) || Objects.equals(cccd, khachHang.cccd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maKH, cccd);
	}
}