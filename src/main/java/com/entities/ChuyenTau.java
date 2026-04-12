package com.entities;

import com.enums.TrangThaiChuyenTau;
import java.util.Objects;

public class ChuyenTau {
	private String maChuyen;
	private String tenChuyen;
	private TrangThaiChuyenTau trangThai;
	private Tau tau; // Mối kết hợp 1-1 với lớp Tau
	private Tuyen tuyen; // Mối kết hợp 1-1 với lớp Tuyen

	// Constructor đầy đủ tham số
	public ChuyenTau(String maChuyen, String tenChuyen, TrangThaiChuyenTau trangThai, Tau tau, Tuyen tuyen) {
		this.maChuyen = maChuyen;
		this.tenChuyen = tenChuyen;
		this.trangThai = trangThai;
		this.tau = tau;
		this.tuyen = tuyen;
	}

	// Constructor mặc định
	public ChuyenTau() {
	}

	// Getter và Setter
	public String getMaChuyen() {
		return maChuyen;
	}

	public void setMaChuyen(String maChuyen) {
		this.maChuyen = maChuyen;
	}

	public String getTenChuyen() {
		return tenChuyen;
	}

	public void setTenChuyen(String tenChuyen) {
		this.tenChuyen = tenChuyen;
	}

	public TrangThaiChuyenTau getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(TrangThaiChuyenTau trangThai) {
		this.trangThai = trangThai;
	}

	public Tau getTau() {
		return tau;
	}

	public void setTau(Tau tau) {
		this.tau = tau;
	}

	public Tuyen getTuyen() {
		return tuyen;
	}

	public void setTuyen(Tuyen tuyen) {
		this.tuyen = tuyen;
	}

	// Phương thức cập nhật trạng thái theo logic nghiệp vụ trong sơ đồ
	public String capNhatTrangThai() {
		// Logic xử lý cập nhật trạng thái có thể viết ở đây
		return this.trangThai.toString();
	}

	@Override
	public String toString() {
		return "ChuyenTau [maChuyen=" + maChuyen + ", tenChuyen=" + tenChuyen + ", trangThai=" + trangThai + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ChuyenTau chuyenTau = (ChuyenTau) o;
		return Objects.equals(maChuyen, chuyenTau.maChuyen);
	}

	@Override
	public int hashCode() {
		return Objects.hash(maChuyen);
	}
}