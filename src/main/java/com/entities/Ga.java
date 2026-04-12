package com.entities;

public class Ga {
	private String maGa;
	private String tenGa;
	private String diaChi;

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
	public Ga(String maGa, String tenGa, String diaChi) {
		super();
		this.maGa = maGa;
		this.tenGa = tenGa;
		this.diaChi = diaChi;
	}
	public Ga() {
		super();
	}

	@Override
	public String toString() {
		return "Ga{" +
				"maGa='" + maGa + '\'' +
				", tenGa='" + tenGa + '\'' +
				", diaChi='" + diaChi + '\'' +
				'}';
	}
}