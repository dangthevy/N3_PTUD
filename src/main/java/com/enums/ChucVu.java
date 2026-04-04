package com.enums;

public enum ChucVu {
    QUANLY("Quản lý"),
    ADMIN("Admin"),
    NHANVIEN("Nhân viên");

    private final String label;

    ChucVu(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ChucVu fromString(String value) {
        return ChucVu.valueOf(value.toUpperCase());
    }
}
