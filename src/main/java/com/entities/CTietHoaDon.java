package com.entities;

public class CTietHoaDon {
	private HoaDon hoaDon;               // Khóa ngoại maHD
	private Ve ve;                       // Khóa ngoại maVe
	private KhuyenMaiDetail khuyenMai;   // Khóa ngoại MaKMDetail (Có thể null)

	private double tienGoc;
	private double tienGiam;
	private double thanhTien;

	public CTietHoaDon() {
	}

	public CTietHoaDon(HoaDon hoaDon, Ve ve, KhuyenMaiDetail khuyenMai, double tienGoc, double tienGiam, double thanhTien) {
		this.hoaDon = hoaDon;
		this.ve = ve;
		this.khuyenMai = khuyenMai;
		this.tienGoc = tienGoc;
		this.tienGiam = tienGiam;
		this.thanhTien = thanhTien;
	}

	// Hàm tính toán logic nội bộ của Chi Tiết (Nên gọi khi thêm Khuyến mãi)
	public void tinhThanhTien() {
		if (this.khuyenMai != null) {
			// Giả sử có logic tính toán tùy theo loại KM (GIAM_PHAN_TRAM hay GIAM_TIEN)
			// Lấy giá trị từ đối tượng KhuyenMaiDetail
		} else {
			this.tienGiam = 0;
		}
		this.thanhTien = this.tienGoc - this.tienGiam;
	}

	// Getters và Setters...
	public HoaDon getHoaDon() { return hoaDon; }
	public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

	public Ve getVe() { return ve; }
	public void setVe(Ve ve) { this.ve = ve; }

	public KhuyenMaiDetail getKhuyenMai() { return khuyenMai; }
	public void setKhuyenMai(KhuyenMaiDetail khuyenMai) { this.khuyenMai = khuyenMai; }

	public double getTienGoc() { return tienGoc; }
	public void setTienGoc(double tienGoc) { this.tienGoc = tienGoc; }

	public double getTienGiam() { return tienGiam; }
	public void setTienGiam(double tienGiam) { this.tienGiam = tienGiam; }

	public double getThanhTien() { return thanhTien; }
	public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
}