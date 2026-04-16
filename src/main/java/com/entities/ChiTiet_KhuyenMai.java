package com.entities;

public class ChiTiet_KhuyenMai {
    private HoaDon hoaDon;
    private Ve ve;
    private KhuyenMaiDetail khuyenMaiDetail;

    private double tienGiamCuaKM;

    public ChiTiet_KhuyenMai(HoaDon hoaDon, Ve ve, KhuyenMaiDetail khuyenMaiDetail, double tienGiamCuaKM) {
        this.hoaDon = hoaDon;
        this.ve = ve;
        this.khuyenMaiDetail = khuyenMaiDetail;
        this.tienGiamCuaKM = tienGiamCuaKM;
    }

    public ChiTiet_KhuyenMai() {
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public Ve getVe() {
        return ve;
    }

    public void setVe(Ve ve) {
        this.ve = ve;
    }

    public KhuyenMaiDetail getKhuyenMaiDetail() {
        return khuyenMaiDetail;
    }

    public void setKhuyenMaiDetail(KhuyenMaiDetail khuyenMaiDetail) {
        this.khuyenMaiDetail = khuyenMaiDetail;
    }

    public double getTienGiamCuaKM() {
        return tienGiamCuaKM;
    }

    public void setTienGiamCuaKM(double tienGiamCuaKM) {
        this.tienGiamCuaKM = tienGiamCuaKM;
    }
}
