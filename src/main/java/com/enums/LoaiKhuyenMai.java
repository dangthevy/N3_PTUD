package com.enums;

public enum LoaiKhuyenMai {
    GIAM_PHAN_TRAM("Giảm %"),
    GIAM_TIEN("Giảm tiền"),
    MIEN_PHI("Miễn phí");

    private final String label;
    LoaiKhuyenMai(String label) { this.label = label; }
    public String getLabel() { return label; }

    public static LoaiKhuyenMai fromString(String value) {
        return LoaiKhuyenMai.valueOf(value.toUpperCase());
    }
}