package com.entities;

public class Toa {
	private String maToa;
    private String tenToa;
    private int soGhe;
    private Tau tau; // Liên kết với đối tượng Tau
    private LoaiToa loaiToa;
	public Toa(String maToa, String tenToa, int soGhe, Tau tau, LoaiToa loaiToa) {
		super();
		this.maToa = maToa;
		this.tenToa = tenToa;
		this.soGhe = soGhe;
		this.tau = tau;
		this.loaiToa = loaiToa;
	}

	public Toa(String maToa, String tenToa) {
	    this.maToa = maToa;
	    this.tenToa = tenToa;
	}
	
	public Toa() {
		super();
	}

	public String getMaToa() {
		return maToa;
	}
	public void setMaToa(String maToa) {
		this.maToa = maToa;
	}
	public String getTenToa() {
		return tenToa;
	}
	public void setTenToa(String tenToa) {
		this.tenToa = tenToa;
	}
	public int getSoGhe() {
		return soGhe;
	}
	public void setSoGhe(int soGhe) {
		this.soGhe = soGhe;
	}
	public Tau getTau() {
		return tau;
	}
	public void setTau(Tau tau) {
		this.tau = tau;
	}
	public LoaiToa getLoaiToa() {
		return loaiToa;
	}
	public void setLoaiToa(LoaiToa loaiToa) {
		this.loaiToa = loaiToa;
	}
	
}
