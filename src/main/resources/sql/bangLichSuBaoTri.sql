CREATE TABLE LichSuBaoTri (
    id INT IDENTITY(1,1) PRIMARY KEY,
    loaiTaiSan NVARCHAR(50) NOT NULL, -- 'TAU', 'TOA', 'GHE'
    maTaiSan VARCHAR(50) NOT NULL,    -- maTau, maToa, ho?c maToa_viTriGhe
    ngayBatDau DATETIME DEFAULT GETDATE(),
    ngayKetThuc DATETIME NULL,
    lyDo NVARCHAR(255) NOT NULL,
    chiPhi DECIMAL(18, 2) DEFAULT 0,
    nguoiThucHien VARCHAR(20) NULL    -- maNV ph? trách l?nh
);