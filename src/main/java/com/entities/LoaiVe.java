package com.entities;

public class LoaiVe {
	private String maLoai;
	private String tenLoai;
	private double mucGiam;

	// Default Constructor
	public LoaiVe() {
	}

	// Full Constructor
	public LoaiVe(String maLoai, String tenLoai, double mucGiam) {
		this.maLoai = maLoai;
		this.tenLoai = tenLoai;
		this.mucGiam = mucGiam;
	}

	// Getters and Setters
	public String getMaLoai() {
		return maLoai;
	}

	public void setMaLoai(String maLoai) {
		this.maLoai = maLoai;
	}

	public String getTenLoai() {
		return tenLoai;
	}

	public void setTenLoai(String tenLoai) {
		this.tenLoai = tenLoai;
	}

	public double getMucGiam() {
		return mucGiam;
	}

	public void setMucGiam(double mucGiam) {
		this.mucGiam = mucGiam;
	}

	@Override
	public String toString() {
		return "LoaiVe{" + "maLoai='" + maLoai + '\'' + ", tenLoai='" + tenLoai + '\'' + ", mucGiam=" + mucGiam + '}';
	}
}