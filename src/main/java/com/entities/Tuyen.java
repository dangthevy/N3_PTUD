package com.entities;

public class Tuyen {
	private String maTuyen;
	private String tenTuyen;
	private int thoiGianChay;
	private Ga gaDi;
	private Ga gaDen;


	public Tuyen(String maTuyen, String tenTuyen, int thoiGianChay, Ga gaDi, Ga gaDen) {
		this.maTuyen = maTuyen;
		this.tenTuyen = tenTuyen;
		this.thoiGianChay = thoiGianChay;
		this.gaDi = gaDi;
		this.gaDen = gaDen;

	}

    public Tuyen(String maTuyen, String tenTuyen) {
        this.maTuyen = maTuyen;
        this.tenTuyen = tenTuyen;
    }

	public String getMaTuyen() {
		return maTuyen;
	}

	public void setMaTuyen(String maTuyen) {
		this.maTuyen = maTuyen;
	}

	public String getTenTuyen() {
		return tenTuyen;
	}

	public void setTenTuyen(String tenTuyen) {
		this.tenTuyen = tenTuyen;
	}

	public int getThoiGianChay() {
		return thoiGianChay;
	}

	public void setThoiGianChay(int thoiGianChay) {
		this.thoiGianChay = thoiGianChay;
	}

	public Ga getGaDi() {
		return gaDi;
	}

	public void setGaDi(Ga gaDi) {
		this.gaDi = gaDi;
	}

	public Ga getGaDen() {
		return gaDen;
	}

	public void setGaDen(Ga gaDen) {
		this.gaDen = gaDen;
	}

	@Override
	public String toString() {
		return "Tuyen{" +
				"maTuyen='" + maTuyen + '\'' +
				", tenTuyen='" + tenTuyen + '\'' +
				", thoiGianChay=" + thoiGianChay +
				", gaDi=" + gaDi +
				", gaDen=" + gaDen +
				'}';
	}
}
