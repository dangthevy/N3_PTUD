package com.entities;

import com.enums.TrangThaiCho;

public class ChoNgoi {
	private String maCho, tenCho;
	private Toa toa;
	private TrangThaiCho trangThai;
	public ChoNgoi(String maCho, String tenCho, Toa toa, TrangThaiCho trangThai) {
		super();
		this.maCho = maCho;
		this.tenCho = tenCho;
		this.toa = toa;
		this.trangThai = trangThai;
	}

	public ChoNgoi() {
		super();
	}

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
	public Toa getToa() {
		return toa;
	}
	public void setToa(Toa toa) {
		this.toa = toa;
	}
	public TrangThaiCho getTrangThai() {
		return trangThai;
	}
	public void setTrangThai(TrangThaiCho trangThai) {
		this.trangThai = trangThai;
	}

}
