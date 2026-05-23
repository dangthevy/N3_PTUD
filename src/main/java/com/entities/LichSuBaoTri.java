package com.entities;

import java.sql.Timestamp;

public class LichSuBaoTri {
	private int id;
	private String loaiTaiSan;
	private String maTaiSan;
	private Timestamp ngayBatDau;
	private Timestamp ngayKetThuc;
	private String lyDo;
	private double chiPhi;
	private String nguoiThucHien;

	public LichSuBaoTri() {
	}

	public LichSuBaoTri(String loaiTaiSan, String maTaiSan, String lyDo, double chiPhi, String nguoiThucHien) {
		this.loaiTaiSan = loaiTaiSan;
		this.maTaiSan = maTaiSan;
		this.lyDo = lyDo;
		this.chiPhi = chiPhi;
		this.nguoiThucHien = nguoiThucHien;
	}

	// ===== GETTERS & SETTERS =====
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLoaiTaiSan() {
		return loaiTaiSan;
	}

	public void setLoaiTaiSan(String loaiTaiSan) {
		this.loaiTaiSan = loaiTaiSan;
	}

	public String getMaTaiSan() {
		return maTaiSan;
	}

	public void setMaTaiSan(String maTaiSan) {
		this.maTaiSan = maTaiSan;
	}

	public Timestamp getNgayBatDau() {
		return ngayBatDau;
	}

	public void setNgayBatDau(Timestamp ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}

	public Timestamp getNgayKetThuc() {
		return ngayKetThuc;
	}

	public void setNgayKetThuc(Timestamp ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}

	public String getLyDo() {
		return lyDo;
	}

	public void setLyDo(String lyDo) {
		this.lyDo = lyDo;
	}

	public double getChiPhi() {
		return chiPhi;
	}

	public void setChiPhi(double chiPhi) {
		this.chiPhi = chiPhi;
	}

	public String getNguoiThucHien() {
		return nguoiThucHien;
	}

	public void setNguoiThucHien(String nguoiThucHien) {
		this.nguoiThucHien = nguoiThucHien;
	}
}