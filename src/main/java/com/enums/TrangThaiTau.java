package com.enums;

public enum TrangThaiTau {
	HOATDONG("Đang hoạt động"),
	BAOTRI("Bảo trì"),
	NGUNGHOATDONG("Ngưng hoạt động");

	private final String moTa;

	// Constructor của Enum
	TrangThaiTau(String moTa) {
		this.moTa = moTa;
	}

	public String getMoTa() {
		return moTa;
	}

	// (Tùy chọn) Hàm để chuyển đổi từ String trong DB sang Enum an toàn hơn
	public static TrangThaiTau fromString(String text) {
		for (TrangThaiTau b : TrangThaiTau.values()) {
			if (b.name().equalsIgnoreCase(text)) {
				return b;
			}
		}
		return HOATDONG; // Giá trị mặc định nếu không khớp
	}
}
