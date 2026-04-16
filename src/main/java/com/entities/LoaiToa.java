package com.entities;
public class LoaiToa {
    private String maLoaiToa, tenLoaiToa, kieuHienThi;
    private int soHang, soCot;
    public LoaiToa() {}
    public LoaiToa(String ma, String ten, int soHang, int soCot, String kieu) {
        this.maLoaiToa = ma; this.tenLoaiToa = ten; this.soHang = soHang; this.soCot = soCot; this.kieuHienThi = kieu;
    }
    public String getMaLoaiToa() { return maLoaiToa; } public void setMaLoaiToa(String maLoaiToa) { this.maLoaiToa = maLoaiToa; }
    public String getTenLoaiToa() { return tenLoaiToa; } public void setTenLoaiToa(String tenLoaiToa) { this.tenLoaiToa = tenLoaiToa; }
    public int getSoHang() { return soHang; } public void setSoHang(int soHang) { this.soHang = soHang; }
    public int getSoCot() { return soCot; } public void setSoCot(int soCot) { this.soCot = soCot; }
    public String getKieuHienThi() { return kieuHienThi; } public void setKieuHienThi(String kieuHienThi) { this.kieuHienThi = kieuHienThi; }
}
