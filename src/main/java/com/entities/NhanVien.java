package com.entities;

import com.enums.ChucVu;
import com.enums.TrangThaiNhanVien;

import java.util.Date;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String sdt;
    private String email;
    private String taiKhoan;
    private String matKhau;
    private ChucVu chucVu;
    private TrangThaiNhanVien trangThai;
    private Date ngayVaoLam;

    public NhanVien() {}

    public NhanVien(String maNV, String tenNV, String sdt, String email, String taiKhoan, String matKhau, ChucVu chucVu, TrangThaiNhanVien trangThai, Date ngayVaoLam) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.sdt = sdt;
        this.email = email;
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
        this.chucVu = chucVu;
        this.trangThai = trangThai;
        this.ngayVaoLam = ngayVaoLam;
    }

    // Getter - Setter
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getTenNV() { return tenNV; }
    public void setTenNV(String tenNV) { this.tenNV = tenNV; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu;
    }

    public TrangThaiNhanVien getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiNhanVien trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayVaoLam() { return ngayVaoLam; }
    public void setNgayVaoLam(Date ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    public String getSdt() {
        return sdt;
    }
    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

	public Object getHoTen() {
		// TODO Auto-generated method stub
		return (this.tenNV != null) ? this.tenNV : "N/A";
	}
}