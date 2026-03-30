package com.gui;

public class NhanVienItem {
    public String maNV;
    public String tenNV;

    public NhanVienItem(String maNV, String tenNV) {
        this.maNV = maNV;
        this.tenNV = tenNV;
    }

    public String getMaNV() { return maNV; }
    @Override
    public String toString() { return tenNV; }
}