package com.enums;

public enum TrangThaiNhanVien {
    HOATDONG("Hoạt động"),
    NGUNGHOATDONG("Ngưng hoạt động");

    private final String label;

    TrangThaiNhanVien(String label) {
        this.label = label;
    }

        public String getLabel() {
            return label;
        }

    public static TrangThaiNhanVien fromString(String value) {
        return TrangThaiNhanVien.valueOf(value.toUpperCase());
    }
}
