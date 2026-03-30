package com.entities;

import com.enums.TrangThaiTau;

public class Tau {
	private String maTau;
	private String tenTau;
	private int soToa;
	private TrangThaiTau trangThaiTau;

	public Tau() {
		super();
	}

	public Tau(String maTau, String tenTau, int soToa, TrangThaiTau trangThaiTau) {
		super();
		this.maTau = maTau;
		this.tenTau = tenTau;
		this.soToa = soToa;
		this.trangThaiTau = trangThaiTau;
	}

	public String getMaTau() {
		return maTau;
	}

	public void setMaTau(String maTau) {
		this.maTau = maTau;
	}

	public String getTenTau() {
		return tenTau;
	}

	public void setTenTau(String tenTau) {
		this.tenTau = tenTau;
	}

	public int getSoToa() {
		return soToa;
	}

	public void setSoToa(int soToa) {
		this.soToa = soToa;
	}

	public TrangThaiTau getTrangThaiTau() {
		return trangThaiTau;
	}

	public void setTrangThaiTau(TrangThaiTau trangThaiTau) {
		this.trangThaiTau = trangThaiTau;
	}

	@Override
	public String toString() {
		return "Tau [maTau=" + maTau + ", tenTau=" + tenTau + ", soToa=" + soToa + ", trangThaiTau=" + trangThaiTau
				+ "]";
	}

}