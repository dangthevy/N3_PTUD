package com.entities;

import com.enums.TrangThaiCho;

public class LoaiCho {
	private String maCho;
	private String tenCho;
	private String maToa;
	private TrangThaiCho trangthaicho;

	// Default Constructor
	public LoaiCho() {
	}

	// Full Constructor
	public LoaiCho(String maCho, String tenCho, String maToa, TrangThaiCho trangThai) {
		this.maCho = maCho;
		this.tenCho = tenCho;
		this.maToa = maToa;
		this.trangthaicho = trangThai;
	}

	// Getters and Setters
	public String getMaCho() {
		return maCho;
	}

	public void setMaCho(String maCho) {
		this.maCho = maCho;
	}

	public String getTenCho() {
		return tenCho;
	}

	public void setTenCho(String tenCho) {
		this.tenCho = tenCho;
	}

	public String getMaToa() {
		return maToa;
	}

	public void setMaToa(String maToa) {
		this.maToa = maToa;
	}

	public TrangThaiCho getTrangThai() {
		return trangthaicho;
	}

	public void setTrangThai(TrangThaiCho trangThai) {
		this.trangthaicho = trangThai;
	}

	@Override
	public String toString() {
		return "LoaiCho{" + "maCho='" + maCho + '\'' + ", tenCho='" + tenCho + '\'' + ", maToa='" + maToa + '\''
				+ ", trangThai='" + trangthaicho + '\'' + '}';
	}
}