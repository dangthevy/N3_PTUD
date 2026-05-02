package com.entities;

import com.enums.TrangThaiVe;

public class Ve {
	private String maVe;
	private KhachHang khachHang; // Thay cho maKH
	private Toa toa; // Thay cho maCho
    private String viTriGhe;
	private LichTrinh lichTrinh; // Thay cho maLT
	private LoaiVe loaiVe; // Thay cho maLoaiVe
	private double giaVe;
	private TrangThaiVe trangThaiVe; // CHUASUDUNG, DASUDUNG, HETHAN, DAHOAN

	// Constructor đầy đủ tham số
	public Ve(String maVe, KhachHang khachHang, Toa toa, String viTriGhe, LichTrinh lichTrinh, LoaiVe loaiVe, double giaVe,
			TrangThaiVe trangThaiVe) {
		this.maVe = maVe;
		this.khachHang = khachHang;
		this.lichTrinh = lichTrinh;
        this.toa = toa;
        this.viTriGhe = viTriGhe;
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

    public Toa getToa() {
        return toa;
    }

    public void setToa(Toa toa) {
        this.toa = toa;
    }

    public String getViTriGhe() {
        return viTriGhe;
    }

    public void setViTriGhe(String viTriGhe) {
        this.viTriGhe = viTriGhe;
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