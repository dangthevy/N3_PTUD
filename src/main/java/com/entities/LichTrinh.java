package com.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class LichTrinh {
	private String maLT;
	private LocalDate ngayKhoiHanh;
	private LocalTime gioKhoiHanh;
	private String maChuyen;

	// Default Constructor
	public LichTrinh() {
	}

	// Full Constructor
	public LichTrinh(String maLT, LocalDate ngayKhoiHanh, LocalTime gioKhoiHanh, String maChuyen) {
		this.maLT = maLT;
		this.ngayKhoiHanh = ngayKhoiHanh;
		this.gioKhoiHanh = gioKhoiHanh;
		this.maChuyen = maChuyen;
	}

	// Getters and Setters
	public String getMaLT() {
		return maLT;
	}

	public void setMaLT(String maLT) {
		this.maLT = maLT;
	}

	public LocalDate getNgayKhoiHanh() {
		return ngayKhoiHanh;
	}

	public void setNgayKhoiHanh(LocalDate ngayKhoiHanh) {
		this.ngayKhoiHanh = ngayKhoiHanh;
	}

	public LocalTime getGioKhoiHanh() {
		return gioKhoiHanh;
	}

	public void setGioKhoiHanh(LocalTime gioKhoiHanh) {
		this.gioKhoiHanh = gioKhoiHanh;
	}

	public String getMaChuyen() {
		return maChuyen;
	}

	public void setMaChuyen(String maChuyen) {
		this.maChuyen = maChuyen;
	}

	@Override
	public String toString() {
		return "LichTrinh{" + "maLT='" + maLT + '\'' + ", ngayKhoiHanh=" + ngayKhoiHanh + ", gioKhoiHanh=" + gioKhoiHanh
				+ ", maChuyen='" + maChuyen + '\'' + '}';
	}
}