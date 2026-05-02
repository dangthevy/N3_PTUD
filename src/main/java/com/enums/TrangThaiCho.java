package com.enums;

/**
 * Enum định nghĩa các trạng thái của Chỗ Ngồi (Ghế/Giường)
 * Khớp với sơ đồ lớp QLBanVeTau.jpg
 */
public enum TrangThaiCho {
	TRONG("Trống"),
	BAOTRI("Bảo trì"),
	DADAT("Đã đặt"),
	GIUCHO("Đang giữ chỗ");

	private final String moTa;

	// Constructor của Enum
	TrangThaiCho(String moTa) {
		this.moTa = moTa;
	}

	// Getter để lấy chuỗi tiếng Việt hiển thị lên UI
	public String getMoTa() {
		return moTa;
	}

	/**
	 * Chuyển đổi từ String (trong Database) sang Enum
	 * Giúp xử lý lỗi khi dữ liệu DB không khớp
	 */
	public static TrangThaiCho fromString(String text) {
		for (TrangThaiCho tt : TrangThaiCho.values()) {
			if (tt.name().equalsIgnoreCase(text)) {
				return tt;
			}
		}
		return TRONG; // Mặc định nếu không tìm thấy
	}
}