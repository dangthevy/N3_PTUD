package com.entities;

public class CT_Tuyen {
	private Tuyen tuyen;
	private Ga ga;
	private int thuTu; // VD ga đi = 1, ga trung gian 1 = 2, ga trung gian 2 = 3...
	private int khoangCach; // (Tùy chọn) Khoảng cách từ ga gốc đến ga này

	public Tuyen getTuyen() {
		return tuyen;
	}

	public void setTuyen(Tuyen tuyen) {
		this.tuyen = tuyen;
	}

	public Ga getGa() {
		return ga;
	}

	public void setGa(Ga ga) {
		this.ga = ga;
	}

	public int getThuTu() {
		return thuTu;
	}

	public void setThuTu(int thuTu) {
		this.thuTu = thuTu;
	}

	public int getKhoangCach() {
		return khoangCach;
	}

	public void setKhoangCach(int khoangCach) {
		this.khoangCach = khoangCach;
	}

}
