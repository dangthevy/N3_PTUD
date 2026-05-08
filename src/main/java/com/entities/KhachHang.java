package com.entities;

import java.util.Objects;

public class KhachHang {
	private String maKH;
	private String hoTen;
	private String sdt;
	private String cccd;
	private String email;
	private String ngayThem;
	private int trangThai;

	// Constructor đầy đủ có ngayThem (dùng cho thêm mới)
	public KhachHang(String maKH, String hoTen, String sdt, String cccd, String email, String ngayThem) {
		this.maKH      = maKH;
		this.hoTen     = hoTen;
		this.sdt       = sdt;
		this.cccd      = cccd;
		this.email     = email;
		this.ngayThem  = ngayThem;
		this.trangThai = 1;
	}

	// Constructor không có ngayThem (dùng cho tìm kiếm / đọc từ DB)
	public KhachHang(String maKH, String hoTen, String sdt, String cccd, String email) {
		this(maKH, hoTen, sdt, cccd, email, null);
	}

	// Constructor mặc định
	public KhachHang() {
	}

	// Getter và Setter
	public String getMaKH()               { return maKH; }
	public void   setMaKH(String maKH)    { this.maKH = maKH; }

	public String getHoTen()              { return hoTen; }
	public void   setHoTen(String hoTen)  { this.hoTen = hoTen; }

	public String getSdt()                { return sdt; }
	public void   setSdt(String sdt)      { this.sdt = sdt; }

	public String getCccd()               { return cccd; }
	public void   setCccd(String cccd)    { this.cccd = cccd; }

	public String getEmail()              { return email; }
	public void   setEmail(String email)  { this.email = email; }

	public String getNgayThem()                   { return ngayThem; }
	public void   setNgayThem(String ngayThem)    { this.ngayThem = ngayThem; }

	public int  getTrangThai()                    { return trangThai; }
	public void setTrangThai(int trangThai)       { this.trangThai = trangThai; }

	@Override
	public String toString() {
		return "KhachHang [maKH=" + maKH + ", hoTen=" + hoTen + ", sdt=" + sdt
				+ ", cccd=" + cccd + ", email=" + email
				+ ", ngayThem=" + ngayThem + ", trangThai=" + trangThai + "]";
	}

	// So sánh 2 khách hàng dựa trên mã hoặc CCCD
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		KhachHang kh = (KhachHang) o;
		return Objects.equals(maKH, kh.maKH) || Objects.equals(cccd, kh.cccd);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maKH, cccd);
	}
}