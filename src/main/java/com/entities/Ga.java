package com.entities;

public class Ga {
	private String maGa;
	private String tenGa;
	private String diaChi;
	private String tinhThanh;

	public Ga(String maGa, String tenGa, String diaChi, String tinhThanh) {
		this.maGa = maGa;
		this.tenGa = tenGa;
		this.diaChi = diaChi;
		this.tinhThanh = tinhThanh;
	}

	public String getMaGa() {
		return maGa;
	}

	public void setMaGa(String maGa) {
		this.maGa = maGa;
	}

	public String getTenGa() {
		return tenGa;
	}

	public void setTenGa(String tenGa) {
		this.tenGa = tenGa;
	}

	public String getDiaChi() {
		return diaChi;
	}

	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}

	public String getTinhThanh() {
		return tinhThanh;
	}

	public void setTinhThanh(String tinhThanh) {
		this.tinhThanh = tinhThanh;
	}

	@Override
	public String toString() {
		return "Ga{" +
				"maGa='" + maGa + '\'' +
				", tenGa='" + tenGa + '\'' +
				", diaChi='" + diaChi + '\'' +
				", tinhThanh='" + tinhThanh + '\'' +
				'}';
	}
}