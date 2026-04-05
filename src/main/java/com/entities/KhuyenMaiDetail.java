package com.entities;

import com.enums.LoaiKhuyenMai;

public class KhuyenMaiDetail {
    private String maKMDetail;
    private KhuyenMai khuyenMai;
    private boolean trangThai;
    private Tuyen    tuyen;
    private String    loaiVe;
    private String    doiTuong;
    private LoaiKhuyenMai loaiKM;   // "Giảm %", "Giảm tiền", "Miễn phí"
    private double    giaTri;   // số %, số tiền, hoặc 0 nếu miễn phí

    public KhuyenMaiDetail() {}

    public String       getMaKMDetail()  { return maKMDetail; }
    public void      setMaKMDetail(String v) { this.maKMDetail = v; }

    public KhuyenMai getKhuyenMai()   { return khuyenMai; }
    public void      setKhuyenMai(KhuyenMai v) { this.khuyenMai = v; }

    public Tuyen    getTuyen()     { return tuyen; }
    public void      setTuyen(Tuyen v) { this.tuyen = v; }

    public String    getLoaiVe()      { return loaiVe; }
    public void      setLoaiVe(String v) { this.loaiVe = v; }

    public String    getDoiTuong()    { return doiTuong; }
    public void      setDoiTuong(String v) { this.doiTuong = v; }

    public LoaiKhuyenMai getLoaiKM() {
        return loaiKM;
    }

    public void setLoaiKM(LoaiKhuyenMai loaiKM) {
        this.loaiKM = loaiKM;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public double    getGiaTri()      { return giaTri; }
    public void      setGiaTri(double v) { this.giaTri = v; }
}