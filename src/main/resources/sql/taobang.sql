-- =============================================
-- RESET DATABASE & CREATE NEW
-- =============================================
IF DB_ID('BanVeTau') IS NOT NULL 
BEGIN
    USE master;
    ALTER DATABASE BanVeTau SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BanVeTau;
END
GO
CREATE DATABASE BanVeTau;
GO
USE BanVeTau;
GO

-- =============================================
-- 1. DANH MỤC CƠ BẢN (GA, TUYẾN, LOẠI TOA, LOẠI VÉ)
-- =============================================
CREATE TABLE Ga (
    maGa VARCHAR(10) PRIMARY KEY,
    tenGa NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255),           
    tinhThanh NVARCHAR(100) NOT NULL, 
    trangThai BIT DEFAULT 1
);

CREATE TABLE Tuyen (
    maTuyen VARCHAR(10) PRIMARY KEY,
    tenTuyen NVARCHAR(100) NOT NULL,
    thoiGianChay INT, 
    gaDi VARCHAR(10),
    gaDen VARCHAR(10),
    trangThai BIT DEFAULT 1,
    FOREIGN KEY (gaDi) REFERENCES Ga(maGa),
    FOREIGN KEY (gaDen) REFERENCES Ga(maGa)
);

CREATE TABLE LoaiToa (
    maLoaiToa VARCHAR(10) PRIMARY KEY,
    tenLoaiToa NVARCHAR(100) NOT NULL,
    soHang INT DEFAULT 0,
    soCot INT DEFAULT 0,
    kieuHienThi NVARCHAR(50) DEFAULT 'GHE'
);

CREATE TABLE LoaiVe (
    maLoai VARCHAR(10) PRIMARY KEY,
    tenLoai NVARCHAR(50) NOT NULL,
    mucGiam DECIMAL(5,2) DEFAULT 0 
);

-- =============================================
-- 2. TÀU, TOA & LẮP RÁP (KHỚP LOGIC JAVA)
-- =============================================
CREATE TABLE Tau (
    maTau VARCHAR(15) PRIMARY KEY, 
    tenTau NVARCHAR(100),
    soToa INT CHECK (soToa > 0),
    trangThai NVARCHAR(20) DEFAULT 'HOATDONG' CHECK (trangThai IN ('HOATDONG','BAOTRI','NGUNGHOATDONG'))
);

CREATE TABLE Toa (
    maToa VARCHAR(25) PRIMARY KEY, 
    tenToa NVARCHAR(100),
    soGhe INT CHECK (soGhe > 0),
    maLoaiToa VARCHAR(10),
    trangThai NVARCHAR(20) DEFAULT 'SAN_SANG',
    FOREIGN KEY (maLoaiToa) REFERENCES LoaiToa(maLoaiToa)
);

CREATE TABLE ChiTietTau (
    maTau VARCHAR(15),
    maToa VARCHAR(25),
    thuTu INT NOT NULL,
    PRIMARY KEY (maTau, maToa),
    FOREIGN KEY (maTau) REFERENCES Tau(maTau) ON DELETE CASCADE,
    FOREIGN KEY (maToa) REFERENCES Toa(maToa) ON DELETE CASCADE
);

-- =============================================
-- 3. LỊCH TRÌNH VÀ VẬN HÀNH
-- =============================================
CREATE TABLE ChuyenTau (
    maChuyen VARCHAR(15) PRIMARY KEY,
    tenChuyen NVARCHAR(100),
    maTau VARCHAR(15),
    maTuyen VARCHAR(10),
    FOREIGN KEY (maTau) REFERENCES Tau(maTau),
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen)
);

CREATE TABLE LichTrinh (
    maLT VARCHAR(15) PRIMARY KEY,
    ngayKhoiHanh DATE,
    gioKhoiHanh TIME,
    gioDen TIME,
    maChuyen VARCHAR(15),
    FOREIGN KEY (maChuyen) REFERENCES ChuyenTau(maChuyen) ON DELETE CASCADE
);

-- Bảng Ghế động: Java quét bảng này để vẽ Sơ đồ (Đỏ/Trống)
CREATE TABLE GheLichTrinh (
    maLT VARCHAR(15),
    maToa VARCHAR(25),
    viTri VARCHAR(10), 
    trangThai NVARCHAR(20) DEFAULT 'TRONG' CHECK (trangThai IN ('TRONG','DADAT','GIUCHO')),
    PRIMARY KEY (maLT, maToa, viTri),
    FOREIGN KEY (maLT) REFERENCES LichTrinh(maLT) ON DELETE CASCADE,
    FOREIGN KEY (maToa) REFERENCES Toa(maToa)
);

-- =============================================
-- 4. BẢNG GIÁ
-- =============================================
CREATE TABLE GiaHeader (
    maGia VARCHAR(15) PRIMARY KEY,
    tenGia NVARCHAR(100), 
    moTa NVARCHAR(255),   
    ngayApDung DATE,
    ngayKetThuc DATE,
    maLT VARCHAR(15),
    FOREIGN KEY (maLT) REFERENCES LichTrinh(maLT) ON DELETE CASCADE
);

CREATE TABLE GiaDetail (
    maGia VARCHAR(15),
    maLoaiToa VARCHAR(10),
    maLoaiVe VARCHAR(10),
    maTuyen VARCHAR(10),
    gia DECIMAL(12,0) CHECK (gia >= 0), 
    PRIMARY KEY (maGia, maLoaiToa, maLoaiVe, maTuyen),
    FOREIGN KEY (maGia) REFERENCES GiaHeader(maGia),
    FOREIGN KEY (maLoaiToa) REFERENCES LoaiToa(maLoaiToa),
    FOREIGN KEY (maLoaiVe) REFERENCES LoaiVe(maLoai),
    FOREIGN KEY (maTuyen) REFERENCES Tuyen(maTuyen)
);

-- =============================================
-- 5. ĐỐI TÁC & NHÂN SỰ
-- =============================================
CREATE TABLE NhanVien (
    ID INT IDENTITY(1,1) NOT NULL,
    maNV AS ('NV' + RIGHT('0000' + CAST(ID AS VARCHAR(10)), 4)) PERSISTED PRIMARY KEY,
    tenNV NVARCHAR(100) NOT NULL,
    sdt NVARCHAR(20),
    email NVARCHAR(50),
    taiKhoan VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(50) NOT NULL,
    chucVu NVARCHAR(50), 
    trangThai NVARCHAR(20) CHECK (trangThai IN ('HOATDONG', 'NGUNGHOATDONG', 'NGHIPHEP')),
    An BIT DEFAULT 0,
    ngayVaoLam DATE
);

CREATE TABLE KhachHang (
    maKH VARCHAR(15) PRIMARY KEY,
    tenKH NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15) UNIQUE,
    cccd VARCHAR(20) UNIQUE,
    email VARCHAR(100)
);

-- =============================================
-- 6. KHUYẾN MÃI & CHI TIẾT
-- =============================================
CREATE TABLE KhuyenMai (
    ID INT IDENTITY(1,1) NOT NULL,
    MaKM AS ('KM' + RIGHT('0000' + CAST(ID AS VARCHAR(10)), 4)) PERSISTED PRIMARY KEY,
    TenKM NVARCHAR(255) NOT NULL,
    NgayBatDau DATE NOT NULL,
    NgayKetThuc DATE NOT NULL,
    TrangThai BIT DEFAULT 1,
    MoTa NVARCHAR(500),
    An BIT DEFAULT 0
);

CREATE TABLE KhuyenMaiDetail (
    ID INT IDENTITY(1,1) NOT NULL,
    MaKMDetail AS ('KMD' + RIGHT('0000' + CAST(ID AS VARCHAR(10)), 4)) PERSISTED PRIMARY KEY,
    MaKM VARCHAR(6) NOT NULL,
    LoaiKM VARCHAR(20) NOT NULL CHECK (LoaiKM IN ('GIAM_PHAN_TRAM', 'GIAM_TIEN')),
    GiaTri DECIMAL(12,0) NOT NULL,
    TrangThai BIT DEFAULT 1,
    MaTuyen VARCHAR(10) NOT NULL,
    maLoaiToa VARCHAR(10) NOT NULL,
    MaLoai VARCHAR(10) NOT NULL,
    An BIT DEFAULT 0,
    CONSTRAINT UQ_KMDetail UNIQUE (MaKM, MaTuyen, maLoaiToa, MaLoai),
    FOREIGN KEY (MaKM) REFERENCES KhuyenMai(MaKM) ON DELETE CASCADE,
    FOREIGN KEY (MaTuyen) REFERENCES Tuyen(maTuyen),
    FOREIGN KEY (MaLoai) REFERENCES LoaiVe(MaLoai),
    FOREIGN KEY (maLoaiToa) REFERENCES LoaiToa(maLoaiToa)
);

-- =============================================
-- 7. GIAO DỊCH (HÓA ĐƠN & VÉ)
-- =============================================
CREATE TABLE HoaDon (
    maHD VARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME DEFAULT GETDATE(),
    maNV VARCHAR(6), 
    maKH VARCHAR(15),
    tongTien DECIMAL(12,0) DEFAULT 0,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

CREATE TABLE Ve (
    maVe VARCHAR(20) PRIMARY KEY,
    maKH VARCHAR(15),
    maLT VARCHAR(15),
    maToa VARCHAR(25),    
    viTriGhe VARCHAR(10), 
    maLoaiVe VARCHAR(10),
    giaVe DECIMAL(12,0) CHECK (giaVe >= 0),
    trangThaiVe NVARCHAR(20) DEFAULT 'CHUASUDUNG' CHECK (trangThaiVe IN ('CHUASUDUNG','DASUDUNG','HETHAN','DAHOAN')),
    
    CONSTRAINT UQ_Ve_Cho_LichTrinh UNIQUE (maLT, maToa, viTriGhe),
    
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    FOREIGN KEY (maLoaiVe) REFERENCES LoaiVe(maLoai),
    FOREIGN KEY (maLT, maToa, viTriGhe) REFERENCES GheLichTrinh(maLT, maToa, viTri)
);

CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(20),
    maVe VARCHAR(20),
    MaKMDetail VARCHAR(7) NULL,
    tienGoc DECIMAL(12,0) DEFAULT 0,
    tienGiam DECIMAL(12,0) DEFAULT 0,
    thanhTien DECIMAL(12,0) DEFAULT 0,
    PRIMARY KEY (maHD, maVe),
    FOREIGN KEY (maHD) REFERENCES HoaDon(maHD) ON DELETE CASCADE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE,
    FOREIGN KEY (MaKMDetail) REFERENCES KhuyenMaiDetail(MaKMDetail)
);

GO

-- =========================================================================
-- ============================ BƠM DỮ LIỆU CHUẨN ==========================
-- =========================================================================

INSERT INTO Ga (maGa, tenGa, diaChi, tinhThanh, trangThai) VALUES 
('GA01', N'Ga Hà Nội', N'120 Lê Duẩn, Cửa Nam, Hoàn Kiếm', N'Hà Nội', 1),
('GA02', N'Ga Phủ Lý', N'Quốc lộ 1A, Hai Bà Trưng, TP. Phủ Lý', N'Hà Nam', 1),
('GA03', N'Ga Nam Định', N'Trần Đăng Ninh, TP. Nam Định', N'Nam Định', 1),
('GA04', N'Ga Ninh Bình', N'1 Ngõ 41 Hoàng Hoa Thám, Thanh Bình, TP. Ninh Bình', N'Ninh Bình', 1),
('GA05', N'Ga Thanh Hóa', N'19 Dương Đình Nghệ, Tân Sơn, TP. Thanh Hóa', N'Thanh Hóa', 1),
('GA06', N'Ga Vinh', N'Số 1 Lê Ninh, Quán Bàu, TP. Vinh', N'Nghệ An', 1),
('GA07', N'Ga Yên Trung', N'Thị trấn Đức Thọ, Huyện Đức Thọ', N'Hà Tĩnh', 1),
('GA08', N'Ga Đồng Hới', N'Tiểu khu 4, Phường Nam Lý, TP. Đồng Hới', N'Quảng Bình', 1),
('GA09', N'Ga Đông Hà', N'2 Lê Thánh Tôn, Phường 1, TP. Đông Hà', N'Quảng Trị', 1),
('GA10', N'Ga Huế', N'2 Bùi Thị Xuân, Phường Đúc, TP. Huế', N'Thừa Thiên Huế', 1),
('GA11', N'Ga Đà Nẵng', N'791 Hải Phòng, Tam Thuận, Thanh Khê', N'Đà Nẵng', 1),
('GA12', N'Ga Tam Kỳ', N'Đường Nguyễn Hoàng, An Xuân, TP. Tam Kỳ', N'Quảng Nam', 1),
('GA13', N'Ga Quảng Ngãi', N'204 Nguyễn Trãi, Nghĩa Lộ, TP. Quảng Ngãi', N'Quảng Ngãi', 1),
('GA14', N'Ga Diêu Trì', N'Thị trấn Diêu Trì, Huyện Tuy Phước', N'Bình Định', 1),
('GA15', N'Ga Quy Nhơn', N'Lê Hồng Phong, Phường Lê Lợi, TP. Quy Nhơn', N'Bình Định', 1),
('GA16', N'Ga Tuy Hòa', N'149 Lê Trung Kiên, Phường 2, TP. Tuy Hòa', N'Phú Yên', 1),
('GA17', N'Ga Nha Trang', N'17 Thái Nguyên, Phước Tân, TP. Nha Trang', N'Khánh Hòa', 1),
('GA18', N'Ga Tháp Chàm', N'Phan Đình Phùng, Đô Vinh, TP. Phan Rang - Tháp Chàm', N'Ninh Thuận', 1),
('GA19', N'Ga Bình Thuận', N'Xã Mương Mán, Huyện Hàm Thuận Nam', N'Bình Thuận', 1),
('GA20', N'Ga Phan Thiết', N'1 Lê Duẩn, Phong Nẫm, TP. Phan Thiết', N'Bình Thuận', 1),
('GA21', N'Ga Long Khánh', N'Trần Phú, Xuân An, TP. Long Khánh', N'Đồng Nai', 1),
('GA22', N'Ga Biên Hòa', N'Quảng trường Ga Biên Hòa, Trung Dũng, TP. Biên Hòa', N'Đồng Nai', 1),
('GA23', N'Ga Sài Gòn', N'1 Nguyễn Thông, Phường 9, Quận 3', N'TP. Hồ Chí Minh', 1),
('GA24', N'Ga Lào Cai', N'Tổ 15A, Phường Phố Mới, TP. Lào Cai', N'Lào Cai', 1),
('GA25', N'Ga Yên Bái', N'218 Trần Hưng Đạo, Hồng Hà, TP. Yên Bái', N'Yên Bái', 1),
('GA26', N'Ga Hải Phòng', N'75 Lương Khánh Thiện, Cầu Đất, Ngô Quyền', N'Hải Phòng', 1),
('GA27', N'Ga Lạng Sơn', N'Lê Lợi, Phường Vĩnh Trại, TP. Lạng Sơn', N'Lạng Sơn', 1),
('GA28', N'Ga Thái Nguyên', N'Quang Trung, Quang Trung, TP. Thái Nguyên', N'Thái Nguyên', 1),
('GA29', N'Ga Bắc Giang', N'Xương Giang, TP. Bắc Giang', N'Bắc Giang', 1),
('GA30', N'Ga Diễn Châu', N'Khối 4, Thị trấn Diễn Châu, Huyện Diễn Châu', N'Nghệ An', 1);

-- Bơm Cấu hình Khuôn Mẫu Loại Toa (Khớp Code UI)
INSERT INTO LoaiToa VALUES 
('G_CUNG', N'Ghế cứng', 16, 4, 'GHE'),   
('G_MEM', N'Ghế mềm', 12, 4, 'GHE'),     
('G_NAM', N'Giường nằm', 7, 4, 'GIUONG');

INSERT INTO LoaiVe VALUES ('LV01', N'Người lớn', 0), ('LV02', N'Trẻ em', 0.5), ('LV03', N'Sinh viên', 0.3);

INSERT INTO NhanVien (tenNV, sdt, email, taiKhoan, matKhau, chucVu, trangThai, ngayVaoLam) VALUES 
(N'Nguyễn Văn A', '0909090901', 'vana@tau.com', 'vana', '123', N'QUANLY', 'HOATDONG', '2025-01-01'),
(N'Lê Thị Bán Vé', '0909012323', 'banve@tau.com', 'banve', '123', N'NHANVIEN', 'HOATDONG', '2025-02-01'),
(N'Nguyễn Văn B', '0923122312', 'vanb@tau.com', 'vanb', '123', N'QUANLY', 'HOATDONG', '2025-01-01'),
(N'Phạm Quang Khải', '0963212321', 'khai@tau.com', 'khai', '123', N'NHANVIEN', 'HOATDONG', '2025-02-01');

INSERT INTO KhachHang (maKH, tenKH, sdt, cccd, email) VALUES 
('KH01', N'Trần Văn An', '0912111222', '079123456781', 'an@gmail.com');

INSERT INTO Tuyen (maTuyen, tenTuyen, thoiGianChay, gaDi, gaDen, trangThai) VALUES 
('T01', N'Sài Gòn - Hà Nội', 1800, 'GA23', 'GA01', 1),
('T02', N'Hà Nội - Sài Gòn', 1800, 'GA01', 'GA23', 1);

-- Tạo 2 Tàu
INSERT INTO Tau VALUES 
('TAU0001', N'Tàu SE1', 11, 'HOATDONG'), 
('TAU0002', N'Tàu SE2', 11, 'HOATDONG');
GO

-- Sản xuất 22 Toa và Gắn vào 2 Tàu (Dùng chuẩn mã TOAXXXX)
DECLARE @t_idx INT = 1;
DECLARE @global_toa_idx INT = 1;
WHILE @t_idx <= 2
BEGIN
    DECLARE @maT VARCHAR(15) = 'TAU' + RIGHT('0000' + CAST(@t_idx AS VARCHAR), 4);
    DECLARE @toa_idx INT = 1;
    
    WHILE @toa_idx <= 11
    BEGIN
        DECLARE @maToa VARCHAR(25) = 'TOA' + RIGHT('0000' + CAST(@global_toa_idx AS VARCHAR), 4);
        DECLARE @loai VARCHAR(10) = CASE WHEN @toa_idx <= 4 THEN 'G_CUNG' WHEN @toa_idx <= 8 THEN 'G_MEM' ELSE 'G_NAM' END;
        DECLARE @tenToa NVARCHAR(100) = CASE WHEN @toa_idx <= 4 THEN N'Toa ghế ngồi cứng' WHEN @toa_idx <= 8 THEN N'Toa ghế ngồi mềm chất lượng cao' ELSE N'Toa giường nằm điều hòa' END;
        DECLARE @maxGhe INT = CASE WHEN @loai = 'G_CUNG' THEN 64 WHEN @loai = 'G_MEM' THEN 48 ELSE 28 END;

        -- Thêm vào Kho
        INSERT INTO Toa (maToa, tenToa, soGhe, maLoaiToa, trangThai) 
        VALUES (@maToa, @tenToa, @maxGhe, @loai, 'SAN_SANG');
        
        -- Lắp ráp vào Tàu
        INSERT INTO ChiTietTau (maTau, maToa, thuTu)
        VALUES (@maT, @maToa, @toa_idx);
        
        SET @toa_idx = @toa_idx + 1;
        SET @global_toa_idx = @global_toa_idx + 1;
    END
    SET @t_idx = @t_idx + 1;
END
GO 

INSERT INTO ChuyenTau VALUES ('CT01', N'SE1: Sài Gòn - Hà Nội', 'TAU0001', 'T01');
INSERT INTO LichTrinh VALUES ('LT01', '2026-04-01', '08:00:00', '20:00:00', 'CT01');

INSERT INTO GiaHeader (maGia, tenGia, moTa, ngayApDung, ngayKetThuc, maLT) VALUES 
('GIA0001', N'Giá vé tháng 4/2026', N'Áp dụng cao điểm hè', '2026-01-01', '2026-12-31', 'LT01');

INSERT INTO GiaDetail (maGia, maLoaiToa, maLoaiVe, maTuyen, gia) VALUES 
('GIA0001', 'G_CUNG', 'LV01', 'T01', 500000),
('GIA0001', 'G_MEM',  'LV01', 'T01', 800000),
('GIA0001', 'G_NAM',  'LV01', 'T01', 1200000);
GO

-- =======================================================================
-- BƠM 30 GHẾ ĐÃ ĐẶT (ĐỂ JAVA BÔI ĐỎ TRÊN SƠ ĐỒ) - LUÔN DÙNG MÃ SỐ 1,2,3...
-- =======================================================================
DECLARE @i INT = 1;
WHILE @i <= 30
BEGIN
    DECLARE @hd VARCHAR(10) = 'HD' + RIGHT('000' + CAST(@i AS VARCHAR(3)), 3);
    DECLARE @ve VARCHAR(10) = 'V' + RIGHT('000' + CAST(@i AS VARCHAR(3)), 3);
    
    -- Chia vé vào 3 toa đầu (TOA0001, TOA0002, TOA0003)
    DECLARE @toa_num INT = CASE WHEN @i % 3 = 1 THEN 1 WHEN @i % 3 = 2 THEN 2 ELSE 3 END;
    DECLARE @maToa_HD VARCHAR(25) = 'TOA' + RIGHT('0000' + CAST(@toa_num AS VARCHAR), 4);
    
    -- Lưu vị trí bằng số nguyên chuẩn để UI Java đọc được (VD: '1', '2', '3'...)
    DECLARE @viTri VARCHAR(10) = CAST((@i / 3) + 1 AS VARCHAR(10)); 
    
    DECLARE @giaThucTe DECIMAL(12,0) = 500000; -- Tạm gán 500k cho nhanh
    
    -- Bơm ghế vào Lịch Trình
    INSERT INTO GheLichTrinh (maLT, maToa, viTri, trangThai) VALUES ('LT01', @maToa_HD, @viTri, 'DADAT');

    -- Bơm Hóa đơn
    INSERT INTO HoaDon (maHD, ngayLap, maNV, maKH, tongTien)
    VALUES (@hd, DATEADD(DAY, -(@i % 7), GETDATE()), 'NV0001', 'KH01', @giaThucTe);

    -- Bơm Vé
    INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe)
    VALUES (@ve, 'KH01', 'LT01', @maToa_HD, @viTri, 'LV01', @giaThucTe, 'CHUASUDUNG');

    INSERT INTO ChiTietHoaDon (maHD, maVe, MaKMDetail, tienGoc, tienGiam, thanhTien) 
    VALUES (@hd, @ve, NULL, @giaThucTe, 0, @giaThucTe);

    SET @i = @i + 1;
END
GO 
-- =============================================
-- BƠM KHUYẾN MÃI VÀ ÁP DỤNG TRỪ TIỀN (CHUẨN LOGIC CHITIET)
-- =============================================
INSERT INTO KhuyenMai (TenKM, NgayBatDau, NgayKetThuc, TrangThai, MoTa)
VALUES 
(N'Khuyến mãi hè 2026', '2026-06-01', '2026-08-31', 1, N'Giảm giá mùa hè cho mọi hành khách'),
(N'Ưu đãi Sinh viên', '2026-01-01', '2026-12-31', 1, N'Chương trình hỗ trợ sinh viên đi lại'),
(N'Flash Sale Cuối Tuần', '2026-04-01', '2026-05-31', 1, N'Giảm giá mạnh các tuyến ngắn');

DECLARE @KM_HE VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Khuyến mãi hè 2026');
DECLARE @KM_SV VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Ưu đãi Sinh viên');
DECLARE @KM_FS VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Flash Sale Cuối Tuần');

INSERT INTO KhuyenMaiDetail 
(MaKM, LoaiKM, GiaTri, TrangThai, MaTuyen, MaLoai, MaLoaiToa)
VALUES 
(@KM_HE, 'GIAM_PHAN_TRAM', 10, 1, 'T01', 'LV01', 'G_CUNG'),
(@KM_HE, 'GIAM_PHAN_TRAM', 15, 1, 'T02', 'LV02', 'G_MEM'),
(@KM_SV, 'GIAM_TIEN', 50000, 1, 'T01', 'LV01', 'G_MEM'),
(@KM_SV, 'GIAM_TIEN', 30000, 1, 'T01', 'LV03', 'G_NAM'),
(@KM_FS, 'GIAM_PHAN_TRAM', 25, 1, 'T01', 'LV02', 'G_CUNG'),
(@KM_FS, 'GIAM_TIEN', 100000, 1, 'T02', 'LV03', 'G_NAM');
GO

-- Lấy mã Khuyến mãi detail (Giảm 10% ghế cứng tuyến T01 của KM Mùa Hè)
DECLARE @KMD_He_Cung VARCHAR(7) = (
    SELECT MaKMDetail 
    FROM KhuyenMaiDetail 
    WHERE MaKM = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Khuyến mãi hè 2026') 
      AND maLoaiToa = 'G_CUNG'
);

-- 1. Áp dụng mã KMDetail vào Chi tiết Hóa Đơn và Trừ Tiền (Chỉ áp dụng cho 10 hóa đơn đầu)
UPDATE ChiTietHoaDon 
SET MaKMDetail = @KMD_He_Cung,
    tienGiam = tienGoc * 0.1,
    thanhTien = tienGoc - (tienGoc * 0.1)
WHERE maHD IN (SELECT TOP 10 maHD FROM HoaDon ORDER BY maHD ASC);
GO

-- 2. Đẩy TỔNG TIỀN ngược lên bảng Cha
UPDATE HoaDon 
SET tongTien = (SELECT SUM(thanhTien) FROM ChiTietHoaDon WHERE ChiTietHoaDon.maHD = HoaDon.maHD)
WHERE maHD IN (SELECT TOP 10 maHD FROM HoaDon ORDER BY maHD ASC);
GO

PRINT N'✅ Database đã được đồng bộ 100% với Code Java. Hãy chạy Java để thấy các ghế 1,2,3... tự động Bôi Đỏ!';

