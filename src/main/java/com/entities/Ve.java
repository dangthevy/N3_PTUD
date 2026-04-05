package com.entities;

import com.enums.TrangThaiVe;

public class Ve {
	private String maVe;
	private KhachHang khachHang; // Thay cho maKH
	private ChoNgoi choNgoi; // Thay cho maCho
	private LichTrinh lichTrinh; // Thay cho maLT
	private LoaiVe loaiVe; // Thay cho maLoaiVe
	private double giaVe;
	private TrangThaiVe trangThaiVe; // CHUASUDUNG, DASUDUNG, HETHAN

	// Constructor đầy đủ tham số
	public Ve(String maVe, KhachHang khachHang, ChoNgoi choNgoi, LichTrinh lichTrinh, LoaiVe loaiVe, double giaVe,
			TrangThaiVe trangThaiVe) {
		this.maVe = maVe;
		this.khachHang = khachHang;
		this.choNgoi = choNgoi;
		this.lichTrinh = lichTrinh;
		this.loaiVe = loaiVe;
		this.giaVe = giaVe;
		this.trangThaiVe = trangThaiVe;
	}

	// Constructor mặc định
	public Ve() {
	}

	// Constructor với mã vé (thường dùng khi cần xóa hoặc tìm kiếm nhanh)
	public Ve(String maVe) {
		this.maVe = maVe;
	}

	// Getter và Setter
	public String getMaVe() {
		return maVe;
	}

	public void setMaVe(String maVe) {
		this.maVe = maVe;
	}

	public KhachHang getKhachHang() {
		return khachHang;
	}

	public void setKhachHang(KhachHang khachHang) {
		this.khachHang = khachHang;
	}

	public ChoNgoi getChoNgoi() {
		return choNgoi;
	}

	public void setChoNgoi(ChoNgoi choNgoi) {
		this.choNgoi = choNgoi;
	}

	public LichTrinh getLichTrinh() {
		return lichTrinh;
	}

	public void setLichTrinh(LichTrinh lichTrinh) {
		this.lichTrinh = lichTrinh;
	}

	public LoaiVe getLoaiVe() {
		return loaiVe;
	}

	public void setLoaiVe(LoaiVe loaiVe) {
		this.loaiVe = loaiVe;
	}

	public double getGiaVe() {
		return giaVe;
	}

	public void setGiaVe(double giaVe) {
		this.giaVe = giaVe;
	}

	public TrangThaiVe getTrangThaiVe() {
		return trangThaiVe;
	}

	public void setTrangThaiVe(TrangThaiVe trangThaiVe) {
		this.trangThaiVe = trangThaiVe;
	}

	@Override
	public String toString() {
		return "Ve [maVe=" + maVe + ", khachHang=" + khachHang.getHoTen() + ", giaVe=" + giaVe + ", trangThai="
				+ trangThaiVe + "]";
	}
}