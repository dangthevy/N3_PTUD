package com.entities;
public class Toa {
    private String maToa, tenToa, trangThai;
    private int soGhe;
    private LoaiToa loaiToa;
    public Toa() {}
    public Toa(String ma, String ten, int soGhe, LoaiToa loaiToa, String tt) {
        this.maToa = ma; this.tenToa = ten; this.soGhe = soGhe; this.loaiToa = loaiToa; this.trangThai = tt;
    }
    public String getMaToa() { return maToa; } public void setMaToa(String maToa) { this.maToa = maToa; }
    public String getTenToa() { return tenToa; } public void setTenToa(String tenToa) { this.tenToa = tenToa; }
    public int getSoGhe() { return soGhe; } public void setSoGhe(int soGhe) { this.soGhe = soGhe; }
    public LoaiToa getLoaiToa() { return loaiToa; } public void setLoaiToa(LoaiToa loaiToa) { this.loaiToa = loaiToa; }
    public String getTrangThai() { return trangThai; } public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
