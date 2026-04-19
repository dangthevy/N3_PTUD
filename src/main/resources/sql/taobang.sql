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
-- 2. TÀU, TOA & LẮP RÁP
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
    ngayDen TIME,
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

-- Chặn việc 1 Toa bị gắn vào 2 Tàu cùng lúc
ALTER TABLE ChiTietTau ADD CONSTRAINT UQ_ChiTietTau_maToa UNIQUE (maToa);

-- Bảng lưu trữ Ghế Vật Lý bị hỏng cần Bảo Trì
CREATE TABLE GheBaoTri (
    maToa VARCHAR(25),
    viTri VARCHAR(10),
    ngayBaoTri DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (maToa, viTri),
    FOREIGN KEY (maToa) REFERENCES Toa(maToa) ON DELETE CASCADE
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
    maTuyen VARCHAR(10),
    gia DECIMAL(12,0) CHECK (gia >= 0),
    PRIMARY KEY (maGia, maLoaiToa, maTuyen),
    FOREIGN KEY (maGia) REFERENCES GiaHeader(maGia),
    FOREIGN KEY (maLoaiToa) REFERENCES LoaiToa(maLoaiToa),
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
    matKhau VARCHAR(70) NOT NULL,
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
    MaTuyen VARCHAR(10) NULL, -- NULL = Tất cả Tuyến
    maLoaiToa VARCHAR(10) NULL, -- NULL = Tất cả Loại Toa
    MaLoai VARCHAR(10) NULL, -- NULL = Tất cả Loại vé
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
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
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
    CONSTRAINT FK_Ve_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    FOREIGN KEY (maLoaiVe) REFERENCES LoaiVe(maLoai),
    FOREIGN KEY (maLT, maToa, viTriGhe) REFERENCES GheLichTrinh(maLT, maToa, viTri)
);

CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(20),
    maVe VARCHAR(20),
    tienGoc DECIMAL(12,0) DEFAULT 0,
    tienGiam DECIMAL(12,0) DEFAULT 0, -- Tiền giảm của tất cả KM áp dụng vào vé này
    thanhTien DECIMAL(12,0) DEFAULT 0,
    PRIMARY KEY (maHD, maVe),
    FOREIGN KEY (maHD) REFERENCES HoaDon(maHD) ON DELETE CASCADE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe) ON DELETE CASCADE
);

CREATE TABLE ChiTiet_KhuyenMai (
    maHD VARCHAR(20),
    maVe VARCHAR(20),
    MaKMDetail VARCHAR(7),
    tienGiamCuaKM DECIMAL(12,0) DEFAULT 0, -- Tiền giảm 1 KM
    PRIMARY KEY (maHD, maVe, MaKMDetail),
    FOREIGN KEY (maHD, maVe) REFERENCES ChiTietHoaDon(maHD, maVe) ON DELETE CASCADE,
    FOREIGN KEY (MaKMDetail) REFERENCES KhuyenMaiDetail(MaKMDetail)
);
GO

-- =========================================================================
-- ============================ BƠM DỮ LIỆU CHUẨN ==========================
-- =========================================================================

INSERT INTO Ga (maGa, tenGa, diaChi, tinhThanh, trangThai) VALUES
('GA01', N'Ga Sài Gòn',     N'1 Nguyễn Thông, Phường 9, Quận 3',                   N'TP. Hồ Chí Minh',   1),
('GA02', N'Ga Phủ Lý',      N'Quốc lộ 1A, Hai Bà Trưng, TP. Phủ Lý',               N'Hà Nam',             1),
('GA03', N'Ga Nam Định',    N'Trần Đăng Ninh, TP. Nam Định',                        N'Nam Định',           1),
('GA04', N'Ga Ninh Bình',   N'1 Ngõ 41 Hoàng Hoa Thám, Thanh Bình, TP. Ninh Bình', N'Ninh Bình',         1),
('GA05', N'Ga Thanh Hóa',   N'19 Dương Đình Nghệ, Tân Sơn, TP. Thanh Hóa',         N'Thanh Hóa',         1),
('GA06', N'Ga Vinh',        N'Số 1 Lê Ninh, Quán Bàu, TP. Vinh',                   N'Nghệ An',           1),
('GA07', N'Ga Yên Trung',   N'Thị trấn Đức Thọ, Huyện Đức Thọ',                    N'Hà Tĩnh',           1),
('GA08', N'Ga Đồng Hới',    N'Tiểu khu 4, Phường Nam Lý, TP. Đồng Hới',            N'Quảng Bình',        1),
('GA09', N'Ga Đông Hà',     N'2 Lê Thánh Tôn, Phường 1, TP. Đông Hà',              N'Quảng Trị',         1),
('GA10', N'Ga Huế',         N'2 Bùi Thị Xuân, Phường Đúc, TP. Huế',                N'Thừa Thiên Huế',    1),
('GA11', N'Ga Đà Nẵng',     N'791 Hải Phòng, Tam Thuận, Thanh Khê',                N'Đà Nẵng',           1),
('GA12', N'Ga Tam Kỳ',      N'Đường Nguyễn Hoàng, An Xuân, TP. Tam Kỳ',            N'Quảng Nam',         1),
('GA13', N'Ga Quảng Ngãi',  N'204 Nguyễn Trãi, Nghĩa Lộ, TP. Quảng Ngãi',         N'Quảng Ngãi',        1),
('GA14', N'Ga Diêu Trì',    N'Thị trấn Diêu Trì, Huyện Tuy Phước',                 N'Bình Định',         1),
('GA15', N'Ga Quy Nhơn',    N'Lê Hồng Phong, Phường Lê Lợi, TP. Quy Nhơn',        N'Bình Định',         1),
('GA16', N'Ga Tuy Hòa',     N'149 Lê Trung Kiên, Phường 2, TP. Tuy Hòa',           N'Phú Yên',           1),
('GA17', N'Ga Nha Trang',   N'17 Thái Nguyên, Phước Tân, TP. Nha Trang',           N'Khánh Hòa',         1),
('GA18', N'Ga Tháp Chàm',   N'Phan Đình Phùng, Đô Vinh, TP. Phan Rang - Tháp Chàm',N'Ninh Thuận',       1),
('GA19', N'Ga Bình Thuận',  N'Xã Mương Mán, Huyện Hàm Thuận Nam',                  N'Bình Thuận',        1),
('GA20', N'Ga Phan Thiết',  N'1 Lê Duẩn, Phong Nẫm, TP. Phan Thiết',              N'Bình Thuận',        1),
('GA21', N'Ga Long Khánh',  N'Trần Phú, Xuân An, TP. Long Khánh',                  N'Đồng Nai',          1),
('GA22', N'Ga Biên Hòa',    N'Quảng trường Ga Biên Hòa, Trung Dũng, TP. Biên Hòa',N'Đồng Nai',          1),
('GA23', N'Ga Hà Nội',      N'120 Lê Duẩn, Cửa Nam, Hoàn Kiếm',                    N'Hà Nội',             1),
('GA24', N'Ga Lào Cai',     N'Tổ 15A, Phường Phố Mới, TP. Lào Cai',                N'Lào Cai',           1),
('GA25', N'Ga Yên Bái',     N'218 Trần Hưng Đạo, Hồng Hà, TP. Yên Bái',           N'Yên Bái',           1),
('GA26', N'Ga Hải Phòng',   N'75 Lương Khánh Thiện, Cầu Đất, Ngô Quyền',          N'Hải Phòng',         1),
('GA27', N'Ga Lạng Sơn',    N'Lê Lợi, Phường Vĩnh Trại, TP. Lạng Sơn',            N'Lạng Sơn',          1),
('GA28', N'Ga Thái Nguyên', N'Quang Trung, Quang Trung, TP. Thái Nguyên',          N'Thái Nguyên',       1),
('GA29', N'Ga Bắc Giang',   N'Xương Giang, TP. Bắc Giang',                          N'Bắc Giang',         1),
('GA30', N'Ga Diễn Châu',   N'Khối 4, Thị trấn Diễn Châu, Huyện Diễn Châu',       N'Nghệ An',           1);

-- Cấu hình Khuôn Mẫu Loại Toa
INSERT INTO LoaiToa VALUES
('G_CUNG', N'Ghế cứng',   16, 4, 'GHE'),
('G_MEM',  N'Ghế mềm',    12, 4, 'GHE'),
('G_NAM',  N'Giường nằm',  7, 4, 'GIUONG');

INSERT INTO LoaiVe VALUES
('LV01', N'Người lớn',  0),
('LV02', N'Trẻ em',     0.5),
('LV03', N'Sinh viên',  0.3);

-- =============================================
-- NHÂN VIÊN (giữ nguyên hash mật khẩu từ bản mới)
-- =============================================
INSERT INTO NhanVien (tenNV, sdt, email, taiKhoan, matKhau, chucVu, trangThai, ngayVaoLam) VALUES
(N'Nguyễn Văn A',    '0909090901', 'vana@tau.com',   'vana',   'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'NHANVIEN', 'HOATDONG', '2025-01-01'),
(N'Lê Thị Bán Vé',  '0909012323', 'banve@tau.com',  'banve',  'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'NHANVIEN', 'HOATDONG', '2025-02-01'),
(N'Nguyễn Văn B',   '0923122312', 'vanb@tau.com',   'vanb',   'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'NHANVIEN', 'HOATDONG', '2025-01-01'),
(N'Phạm Quang Khải','0963212321', 'khai@tau.com',   'khai',   'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'QUANLY',   'HOATDONG', '2025-02-01'),
(N'Phạm Quốc Vinh', '0923452812', 'vinh@tau.com',   'vinh',   'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'QUANLY',   'HOATDONG', '2025-02-01'),
(N'Đặng Thế Vỹ',    '0925723812', 'vy@tau.com',     'vy',     'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'QUANLY',   'HOATDONG', '2025-02-01'),
(N'Nguyễn Vủ Thiện','0958472305', 'thien@tau.com',  'thien',  'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'QUANLY',   'HOATDONG', '2025-02-01'),
(N'Phạm Thái Bảo',  '0935782312', 'bao@tau.com',    'bao',    'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'QUANLY',   'HOATDONG', '2025-02-01'),
(N'admin',           '0963212322', 'admin@tau.com',  'admin',  'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', N'ADMIN',    'HOATDONG', '2025-02-01');

INSERT INTO KhachHang (maKH, tenKH, sdt, cccd, email) VALUES
('KH01', N'Trần Văn An', '0912111222', '079123456781', 'an@gmail.com');

-- =============================================
-- TÀU (6 tàu)
-- =============================================
INSERT INTO Tau VALUES
('TAU0001', N'Tàu SE1',  11, 'HOATDONG'),
('TAU0002', N'Tàu SE2',  13, 'HOATDONG'),
('TAU0003', N'Tàu SE3',  11, 'HOATDONG'),
('TAU0004', N'Tàu SE4',  15, 'HOATDONG'),
('TAU0005', N'Tàu SPT1', 11, 'HOATDONG'),
('TAU0006', N'Tàu SPT2', 11, 'HOATDONG');
GO

-- =============================================
-- TOA & LẮP RÁP (6 tàu x 11 toa = 66 toa)
-- =============================================
DECLARE @t_idx INT = 1;
DECLARE @global_toa_idx INT = 1;
WHILE @t_idx <= 6
BEGIN
    DECLARE @maT VARCHAR(15) = 'TAU' + RIGHT('0000' + CAST(@t_idx AS VARCHAR), 4);
    DECLARE @toa_idx INT = 1;
    WHILE @toa_idx <= 11
    BEGIN
        DECLARE @maToa VARCHAR(25)    = 'TOA' + RIGHT('0000' + CAST(@global_toa_idx AS VARCHAR), 4);
        DECLARE @loai VARCHAR(10)     = CASE WHEN @toa_idx <= 4 THEN 'G_CUNG' WHEN @toa_idx <= 8 THEN 'G_MEM' ELSE 'G_NAM' END;
        DECLARE @tenToa NVARCHAR(100) = CASE WHEN @toa_idx <= 4 THEN N'Toa ghế ngồi cứng' WHEN @toa_idx <= 8 THEN N'Toa ghế ngồi mềm chất lượng cao' ELSE N'Toa giường nằm điều hòa' END;
        DECLARE @maxGhe INT           = CASE WHEN @loai = 'G_CUNG' THEN 64 WHEN @loai = 'G_MEM' THEN 48 ELSE 28 END;

        INSERT INTO Toa (maToa, tenToa, soGhe, maLoaiToa, trangThai)
        VALUES (@maToa, @tenToa, @maxGhe, @loai, 'SAN_SANG');

        INSERT INTO ChiTietTau (maTau, maToa, thuTu)
        VALUES (@maT, @maToa, @toa_idx);

        SET @toa_idx = @toa_idx + 1;
        SET @global_toa_idx = @global_toa_idx + 1;
    END
    SET @t_idx = @t_idx + 1;
END
GO

-- =============================================
-- TUYẾN (12 tuyến đa dạng)
-- =============================================
INSERT INTO Tuyen (maTuyen, tenTuyen, thoiGianChay, gaDi, gaDen, trangThai) VALUES
('T01', N'Sài Gòn - Hà Nội',      1800, 'GA01', 'GA23', 1),
('T02', N'Hà Nội - Sài Gòn',      1800, 'GA23', 'GA01', 1),
('T03', N'Sài Gòn - Nha Trang',    480, 'GA01', 'GA17', 1),
('T04', N'Nha Trang - Sài Gòn',    480, 'GA17', 'GA01', 1),
('T05', N'Hà Nội - Đà Nẵng',       780, 'GA23', 'GA11', 1),
('T06', N'Đà Nẵng - Hà Nội',       780, 'GA11', 'GA23', 1),
('T07', N'Hà Nội - Vinh',          240, 'GA23', 'GA06', 1),
('T08', N'Vinh - Hà Nội',          240, 'GA06', 'GA23', 1),
('T09', N'Hà Nội - Huế',           660, 'GA23', 'GA10', 1),
('T10', N'Huế - Hà Nội',           660, 'GA10', 'GA23', 1),
('T11', N'Đà Nẵng - Sài Gòn',     1020, 'GA11', 'GA01', 1),
('T12', N'Sài Gòn - Đà Nẵng',     1020, 'GA01', 'GA11', 1);

-- =============================================
-- CHUYẾN TÀU (12 chuyến)
-- =============================================
INSERT INTO ChuyenTau VALUES
('CT01', N'SE1: Sài Gòn - Hà Nội',     'TAU0001', 'T01'),
('CT02', N'SE2: Hà Nội - Sài Gòn',     'TAU0002', 'T02'),
('CT03', N'SE3: Sài Gòn - Nha Trang',  'TAU0003', 'T03'),
('CT04', N'SE4: Nha Trang - Sài Gòn',  'TAU0004', 'T04'),
('CT05', N'SPT1: Hà Nội - Đà Nẵng',    'TAU0005', 'T05'),
('CT06', N'SPT2: Đà Nẵng - Hà Nội',    'TAU0006', 'T06'),
('CT07', N'TN1: Hà Nội - Vinh',        'TAU0001', 'T07'),
('CT08', N'TN2: Vinh - Hà Nội',        'TAU0002', 'T08'),
('CT09', N'TN3: Hà Nội - Huế',         'TAU0003', 'T09'),
('CT10', N'TN4: Huế - Hà Nội',         'TAU0004', 'T10'),
('CT11', N'TN5: Đà Nẵng - Sài Gòn',   'TAU0005', 'T11'),
('CT12', N'TN6: Sài Gòn - Đà Nẵng',   'TAU0006', 'T12');

-- =============================================
-- LỊCH TRÌNH (nhiều ngày, nhiều chuyến)
-- =============================================
INSERT INTO LichTrinh VALUES
-- Tuyến SG-HN / HN-SG
('LT01',  '2026-04-01', '08:00:00', '20:00:00', 'CT01'),
('LT02',  '2026-04-02', '08:00:00', '20:00:00', 'CT02'),
('LT03',  '2026-04-22', '08:00:00', '20:00:00', 'CT01'),
('LT04',  '2026-04-25', '14:00:00', '02:00:00', 'CT02'),
('LT05',  '2026-04-30', '08:00:00', '20:00:00', 'CT01'),
-- Tuyến SG-Nha Trang / NT-SG
('LT06',  '2026-04-10', '06:00:00', '14:00:00', 'CT03'),
('LT07',  '2026-04-15', '07:00:00', '15:00:00', 'CT04'),
('LT08',  '2026-04-20', '06:00:00', '14:00:00', 'CT03'),
-- Tuyến HN-Đà Nẵng / ĐN-HN
('LT09',  '2026-04-05', '07:30:00', '20:30:00', 'CT05'),
('LT10',  '2026-04-12', '07:30:00', '20:30:00', 'CT06'),
('LT11',  '2026-04-18', '07:30:00', '20:30:00', 'CT05'),
-- Tuyến HN-Vinh / Vinh-HN
('LT12',  '2026-04-07', '06:00:00', '10:00:00', 'CT07'),
('LT13',  '2026-04-14', '06:00:00', '10:00:00', 'CT08'),
('LT14',  '2026-04-21', '06:00:00', '10:00:00', 'CT07'),
-- Tuyến HN-Huế / Huế-HN
('LT15',  '2026-04-08', '07:00:00', '18:00:00', 'CT09'),
('LT16',  '2026-04-16', '07:00:00', '18:00:00', 'CT10'),
('LT17',  '2026-04-24', '07:00:00', '18:00:00', 'CT09'),
-- Tuyến ĐN-SG / SG-ĐN
('LT18',  '2026-04-09', '08:00:00', '05:00:00', 'CT11'),
('LT19',  '2026-04-17', '08:00:00', '05:00:00', 'CT12'),
-- Tháng 5/2026
('LT20',  '2026-05-01', '08:00:00', '20:00:00', 'CT01'),
('LT21',  '2026-05-02', '08:00:00', '20:00:00', 'CT02'),
('LT22',  '2026-05-05', '06:00:00', '14:00:00', 'CT03'),
('LT23',  '2026-05-10', '07:30:00', '20:30:00', 'CT05');

-- =============================================
-- BẢNG GIÁ HEADER & DETAIL
-- Cho phép maLT NULL để dùng header chung
-- =============================================
ALTER TABLE GiaHeader ALTER COLUMN maLT VARCHAR(15) NULL;
GO

INSERT INTO GiaHeader (maGia, tenGia, moTa, ngayApDung, ngayKetThuc, maLT) VALUES
('GIA_T01_T2', N'Giá chung tuyến Bắc-Nam 2026',     N'Áp dụng tuyến T01/T02 cả năm',             '2026-01-01', '2026-12-31', NULL);

INSERT INTO GiaDetail (maGia, maLoaiToa, maTuyen, gia) VALUES

-- ===== T01 (SG - HN) =====
('GIA_T01_T2', 'G_CUNG', 'T01', 520000),
('GIA_T01_T2', 'G_MEM',  'T01', 820000),
('GIA_T01_T2', 'G_NAM',  'T01', 1250000),

-- ===== T02 (HN - SG) =====
('GIA_T01_T2', 'G_CUNG', 'T02', 520000),
('GIA_T01_T2', 'G_MEM',  'T02', 820000),
('GIA_T01_T2', 'G_NAM',  'T02', 1250000),

-- ===== T03 / T04 (SG - Nha Trang) =====
('GIA_T01_T2', 'G_CUNG', 'T03', 200000),
('GIA_T01_T2', 'G_MEM',  'T03', 350000),
('GIA_T01_T2', 'G_NAM',  'T03', 550000),
('GIA_T01_T2', 'G_CUNG', 'T04', 200000),
('GIA_T01_T2', 'G_MEM',  'T04', 350000),
('GIA_T01_T2', 'G_NAM',  'T04', 550000),

-- ===== T05 / T06 (HN - ĐN) =====
('GIA_T01_T2', 'G_CUNG', 'T05', 280000),
('GIA_T01_T2', 'G_MEM',  'T05', 450000),
('GIA_T01_T2', 'G_NAM',  'T05', 700000),
('GIA_T01_T2', 'G_CUNG', 'T06', 280000),
('GIA_T01_T2', 'G_MEM',  'T06', 450000),
('GIA_T01_T2', 'G_NAM',  'T06', 700000),

-- ===== T07 / T08 (HN - Vinh) =====
('GIA_T01_T2', 'G_CUNG', 'T07', 120000),
('GIA_T01_T2', 'G_MEM',  'T07', 200000),
('GIA_T01_T2', 'G_NAM',  'T07', 300000),
('GIA_T01_T2', 'G_CUNG', 'T08', 120000),
('GIA_T01_T2', 'G_MEM',  'T08', 200000),
('GIA_T01_T2', 'G_NAM',  'T08', 300000),

-- ===== T09 / T10 (HN - Huế) =====
('GIA_T01_T2', 'G_CUNG', 'T09', 250000),
('GIA_T01_T2', 'G_MEM',  'T09', 400000),
('GIA_T01_T2', 'G_NAM',  'T09', 620000),
('GIA_T01_T2', 'G_CUNG', 'T10', 250000),
('GIA_T01_T2', 'G_MEM',  'T10', 400000),
('GIA_T01_T2', 'G_NAM',  'T10', 620000),

-- ===== T11 / T12 (ĐN - SG) =====
('GIA_T01_T2', 'G_CUNG', 'T11', 360000),
('GIA_T01_T2', 'G_MEM',  'T11', 580000),
('GIA_T01_T2', 'G_NAM',  'T11', 880000),
('GIA_T01_T2', 'G_CUNG', 'T12', 360000),
('GIA_T01_T2', 'G_MEM',  'T12', 580000),
('GIA_T01_T2', 'G_NAM',  'T12', 880000);
GO

-- =======================================================================
-- BƠM 30 GHẾ ĐÃ ĐẶT CHO LT01 (để Java tô đỏ sơ đồ)
-- =======================================================================
DECLARE @i INT = 1;
WHILE @i <= 30
BEGIN
    DECLARE @hd VARCHAR(10)       = 'HD' + RIGHT('000' + CAST(@i AS VARCHAR(3)), 3);
    DECLARE @ve VARCHAR(10)       = 'V' + RIGHT('000' + CAST(@i AS VARCHAR(3)), 3);
    DECLARE @toa_num INT          = CASE WHEN @i % 3 = 1 THEN 1 WHEN @i % 3 = 2 THEN 2 ELSE 3 END;
    DECLARE @maToa_HD VARCHAR(25) = 'TOA' + RIGHT('0000' + CAST(@toa_num AS VARCHAR), 4);
    DECLARE @viTri VARCHAR(10)    = CAST((@i / 3) + 1 AS VARCHAR(10));
    DECLARE @giaThucTe DECIMAL(12,0) = 500000;

    INSERT INTO GheLichTrinh (maLT, maToa, viTri, trangThai) VALUES ('LT01', @maToa_HD, @viTri, 'DADAT');

    INSERT INTO HoaDon (maHD, ngayLap, maNV, maKH, tongTien)
    VALUES (@hd, DATEADD(DAY, -(@i % 7), GETDATE()), 'NV0001', 'KH01', @giaThucTe);

    INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe)
    VALUES (@ve, 'KH01', 'LT01', @maToa_HD, @viTri, 'LV01', @giaThucTe, 'CHUASUDUNG');

    INSERT INTO ChiTietHoaDon (maHD, maVe, tienGoc, tienGiam, thanhTien)
    VALUES (@hd, @ve, @giaThucTe, 0, @giaThucTe);

    SET @i = @i + 1;
END
GO

-- =============================================
-- KHUYẾN MÃI & DETAIL
-- =============================================
INSERT INTO KhuyenMai (TenKM, NgayBatDau, NgayKetThuc, TrangThai, MoTa) VALUES
(N'Khuyến mãi hè 2026',           '2026-06-01', '2026-08-31', 1, N'Giảm giá mùa hè cho mọi hành khách'),
(N'Ưu đãi Sinh viên',             '2026-01-01', '2026-12-31', 1, N'Chương trình hỗ trợ sinh viên đi lại'),
(N'Ưu đãi vé trẻ em',             '2026-01-01', '2026-12-31', 1, N'Chương trình giảm giá cho trẻ em'),
(N'Flash Sale Cuối Tuần',         '2026-04-01', '2026-05-31', 1, N'Giảm giá mạnh các tuyến ngắn'),
(N'Khuyến mãi Tết Nguyên Đán 2026','2026-02-01','2026-02-28', 1, N'Ưu đãi dịp Tết cho các tuyến phổ biến'),
(N'Ưu đãi Giỗ Tổ Hùng Vương 2026','2026-04-01','2026-04-10', 1, N'Khuyến mãi dịp lễ đầu tháng 4'),
(N'Khuyến mãi 30/4 - 1/5',        '2026-04-25','2026-05-05', 1, N'Ưu đãi dịp lễ 30/4 và 1/5'),
(N'Ưu đãi Mùa Du Lịch Tháng 5',  '2026-05-01', '2026-05-31', 1, N'Khuyến mãi cho hành khách đi du lịch trong tháng 5');
GO

DECLARE @KM_HE    VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Khuyến mãi hè 2026');
DECLARE @KM_SV    VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Ưu đãi Sinh viên');
DECLARE @KM_TE    VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Ưu đãi vé trẻ em');
DECLARE @KM_FS    VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Flash Sale Cuối Tuần');
DECLARE @KM_TET   VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Khuyến mãi Tết Nguyên Đán 2026');
DECLARE @KM_GIOTO VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Ưu đãi Giỗ Tổ Hùng Vương 2026');
DECLARE @KM_304   VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Khuyến mãi 30/4 - 1/5');
DECLARE @KM_T5    VARCHAR(6) = (SELECT MaKM FROM KhuyenMai WHERE TenKM = N'Ưu đãi Mùa Du Lịch Tháng 5');

INSERT INTO KhuyenMaiDetail (MaKM, LoaiKM, GiaTri, TrangThai, MaTuyen, maLoaiToa, MaLoai) VALUES
-- Sinh viên: giảm 200k, áp dụng loại vé LV03, tất cả tuyến
(@KM_SV,    'GIAM_TIEN',      200000, 1, NULL,  NULL,     'LV03'),
-- Trẻ em: giảm 300k, áp dụng loại vé LV02
(@KM_TE,    'GIAM_TIEN',      300000, 1, NULL,  NULL,     'LV02'),
-- Hè 2026: giảm 10% ghế nằm & mềm tuyến T01
(@KM_HE,    'GIAM_PHAN_TRAM',     10, 1, 'T01', 'G_NAM',  NULL),
(@KM_HE,    'GIAM_PHAN_TRAM',     10, 1, 'T01', 'G_MEM',  NULL),
-- Flash sale cuối tuần: giảm 20% ghế cứng tất cả tuyến
(@KM_FS,    'GIAM_PHAN_TRAM',     20, 1, NULL,  'G_CUNG', NULL),
-- Tết: giảm 15% ghế mềm tuyến T02; giảm 200k ghế cứng tất cả tuyến
(@KM_TET,   'GIAM_PHAN_TRAM',     15, 1, 'T02', 'G_MEM',  NULL),
(@KM_TET,   'GIAM_TIEN',      200000, 1, NULL,  'G_CUNG', NULL),
-- Giỗ Tổ: giảm 10% ghế cứng tất cả tuyến
(@KM_GIOTO, 'GIAM_PHAN_TRAM',     10, 1, NULL,  'G_CUNG', NULL),
-- 30/4: giảm 20% ghế nằm tuyến T03; giảm 150k tất cả
(@KM_304,   'GIAM_PHAN_TRAM',     20, 1, 'T03', 'G_NAM',  NULL),
(@KM_304,   'GIAM_TIEN',      150000, 1, NULL,  NULL,     NULL),
-- Tháng 5: giảm 12% ghế mềm tuyến T04; giảm 100k ghế cứng
(@KM_T5,    'GIAM_PHAN_TRAM',     12, 1, 'T04', 'G_MEM',  NULL),
(@KM_T5,    'GIAM_TIEN',      100000, 1, NULL,  'G_CUNG', NULL);
GO

PRINT N'✅ Database đã được đồng bộ 100% với Code Java. Hãy chạy Java để thấy các ghế 1,2,3... tự động Bôi Đỏ!';
GO

-- =========================================================================
-- ĐỒNG BỘ DỮ LIỆU KHÁCH HÀNG (TRẠNG THÁI & ID)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('KhachHang') AND name = 'trangThai')
BEGIN
    ALTER TABLE KhachHang ADD trangThai INT DEFAULT 1;
END
GO

UPDATE KhachHang SET trangThai = 1;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('KhachHang') AND name = 'id')
BEGIN
    ALTER TABLE KhachHang ADD id INT IDENTITY(1,1);
END
GO

SELECT
    maKH AS old_maKH,
    'KH' + RIGHT('00000000' + CAST(id AS VARCHAR(8)), 8) AS new_maKH
INTO #tempMap
FROM KhachHang;
GO

ALTER TABLE Ve    DROP CONSTRAINT FK_Ve_KhachHang;
ALTER TABLE HoaDon DROP CONSTRAINT FK_HoaDon_KhachHang;
GO

UPDATE v SET v.maKH = t.new_maKH FROM Ve v    JOIN #tempMap t ON v.maKH = t.old_maKH;
UPDATE h SET h.maKH = t.new_maKH FROM HoaDon h JOIN #tempMap t ON h.maKH = t.old_maKH;
UPDATE k SET k.maKH = t.new_maKH FROM KhachHang k JOIN #tempMap t ON k.maKH = t.old_maKH;
GO

ALTER TABLE Ve    ADD CONSTRAINT FK_Ve_KhachHang    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH);
ALTER TABLE HoaDon ADD CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH);
GO

DROP TABLE #tempMap;
GO

-- =========================================================================
-- BỔ SUNG KHÁCH HÀNG (30 khách hàng)
-- =========================================================================
INSERT INTO KhachHang (maKH, tenKH, sdt, cccd, email, trangThai) VALUES
('KH00000002', N'Lê Minh Tâm',      '0988123456', '079123456782', 'tamle@gmail.com',     1),
('KH00000003', N'Nguyễn Hoàng Nam', '0977456789', '079123456783', 'namnguyen@gmail.com', 1),
('KH00000004', N'Phạm Mỹ Linh',     '0966789123', '079123456784', 'linhpham@gmail.com',  1),
('KH00000005', N'Đặng Quốc Bảo',    '0955000111', '079123456785', 'baodang@gmail.com',   1);
GO

DECLARE @k INT = 6;
WHILE @k <= 30
BEGIN
    DECLARE @maKH_New  VARCHAR(20)   = 'KH' + RIGHT('00000000' + CAST(@k AS VARCHAR), 8);
    DECLARE @tenKH_New NVARCHAR(100) = N'Khách Hàng Mẫu ' + CAST(@k AS VARCHAR);
    DECLARE @sdt_New   VARCHAR(15)   = '090' + RIGHT('0000000' + CAST(@k AS VARCHAR), 7);
    DECLARE @cccd_New  VARCHAR(15)   = '079' + RIGHT('000000000' + CAST(@k AS VARCHAR), 9);
    IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKH = @maKH_New)
    BEGIN
        INSERT INTO KhachHang (maKH, tenKH, sdt, cccd, email, trangThai)
        VALUES (@maKH_New, @tenKH_New, @sdt_New, @cccd_New, CAST(@k AS VARCHAR) + '@gmail.com', 1);
    END
    SET @k = @k + 1;
END
GO

-- =========================================================================
-- BƠM GHẾ & VÉ CHO LT03/LT04/LT05 (6 vé – NV0004, NV0005)
-- =========================================================================
DECLARE @cnt INT = 1;
WHILE @cnt <= 6
BEGIN
    DECLARE @mhd VARCHAR(20) = 'HD_SPEC_' + CAST(@cnt AS VARCHAR);
    DECLARE @mve VARCHAR(20) = 'V_SPEC_'  + CAST(@cnt AS VARCHAR);
    DECLARE @nv  VARCHAR(10) = CASE WHEN @cnt % 2 = 1 THEN 'NV0004' ELSE 'NV0005' END;
    DECLARE @lt  VARCHAR(10) = CASE WHEN @cnt <= 2 THEN 'LT03' WHEN @cnt <= 4 THEN 'LT04' ELSE 'LT05' END;
    DECLARE @kh  VARCHAR(20) = 'KH00000002';

    INSERT INTO GheLichTrinh (maLT, maToa, viTri, trangThai) VALUES (@lt, 'TOA0001', CAST(@cnt + 15 AS VARCHAR), 'DADAT');
    INSERT INTO HoaDon (maHD, ngayLap, maNV, maKH, tongTien) VALUES (@mhd, GETDATE(), @nv, @kh, 500000);
    INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe)
    VALUES (@mve, @kh, @lt, 'TOA0001', CAST(@cnt + 15 AS VARCHAR), 'LV01', 500000, 'CHUASUDUNG');
    INSERT INTO ChiTietHoaDon (maHD, maVe, tienGoc, tienGiam, thanhTien)
    VALUES (@mhd, @mve, 500000, 0, 500000);

    SET @cnt = @cnt + 1;
END
GO

-- =========================================================================
-- BƠM 100 VÉ ĐA DẠNG (nhiều lịch trình, nhiều khách)
-- =========================================================================
DECLARE @cnt INT = 1;
DECLARE @kh_random  VARCHAR(20);
DECLARE @nv_random  VARCHAR(10);
DECLARE @lt_random  VARCHAR(10);
DECLARE @toa_random VARCHAR(25);
DECLARE @giaVe      DECIMAL(12,0);

WHILE @cnt <= 100
BEGIN
    DECLARE @mhd VARCHAR(20) = 'HD_V100_' + CAST(@cnt AS VARCHAR);
    DECLARE @mve VARCHAR(20) = 'V_100_'   + CAST(@cnt AS VARCHAR);

    SET @nv_random = CASE WHEN @cnt % 2 = 1 THEN 'NV0004' ELSE 'NV0005' END;

    DECLARE @random_kh_num INT = (ABS(CHECKSUM(NEWID())) % 30) + 1;
    SET @kh_random = 'KH' + RIGHT('00000000' + CAST(@random_kh_num AS VARCHAR), 8);

    SET @lt_random = CASE
        WHEN @cnt % 6 = 1 THEN 'LT03'
        WHEN @cnt % 6 = 2 THEN 'LT04'
        WHEN @cnt % 6 = 3 THEN 'LT05'
        WHEN @cnt % 6 = 4 THEN 'LT06'
        WHEN @cnt % 6 = 5 THEN 'LT09'
        ELSE 'LT12'
    END;

    IF (@cnt % 2 = 0)
    BEGIN
        SET @toa_random = 'TOA0001'; -- G_CUNG
        SET @giaVe = 500000;
    END
    ELSE
    BEGIN
        SET @toa_random = 'TOA0005'; -- G_MEM
        SET @giaVe = 800000;
    END

    DECLARE @viTriGhe VARCHAR(10) = CAST((@cnt / 2) + 50 AS VARCHAR);

    IF NOT EXISTS (SELECT 1 FROM GheLichTrinh WHERE maLT = @lt_random AND maToa = @toa_random AND viTri = @viTriGhe)
    BEGIN
        INSERT INTO GheLichTrinh (maLT, maToa, viTri, trangThai)
        VALUES (@lt_random, @toa_random, @viTriGhe, 'DADAT');

        INSERT INTO HoaDon (maHD, ngayLap, maNV, maKH, tongTien)
        VALUES (@mhd, DATEADD(DAY, -(@cnt % 5), '2026-04-19'), @nv_random, @kh_random, @giaVe);

        INSERT INTO Ve (maVe, maKH, maLT, maToa, viTriGhe, maLoaiVe, giaVe, trangThaiVe)
        VALUES (@mve, @kh_random, @lt_random, @toa_random, @viTriGhe, 'LV01', @giaVe, 'CHUASUDUNG');

        INSERT INTO ChiTietHoaDon (maHD, maVe, tienGoc, tienGiam, thanhTien)
        VALUES (@mhd, @mve, @giaVe, 0, @giaVe);
    END

    SET @cnt = @cnt + 1;
END
GO

PRINT N'✅ Hoàn tất! Database BanVeTau đã sẵn sàng.';
GO