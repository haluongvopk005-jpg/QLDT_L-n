
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'PhoneManagerDB')
    CREATE DATABASE PhoneManagerDB;
GO

USE PhoneManagerDB;
GO

-- Các tùy chọn SET cần thiết cho computed column PERSISTED và index.
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- Xóa trigger nếu đã tồn tại
IF OBJECT_ID('trg_ChiTietHoaDon_Insert', 'TR') IS NOT NULL
    DROP TRIGGER trg_ChiTietHoaDon_Insert;
GO

IF OBJECT_ID('trg_ChiTietPhieuNhap_Insert', 'TR') IS NOT NULL
    DROP TRIGGER trg_ChiTietPhieuNhap_Insert;
GO

-- Xóa bảng cũ theo thứ tự khóa ngoại
IF OBJECT_ID('BaoHanh', 'U') IS NOT NULL DROP TABLE BaoHanh;
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('ChiTietPhieuNhap', 'U') IS NOT NULL DROP TABLE ChiTietPhieuNhap;
IF OBJECT_ID('PhieuNhap', 'U') IS NOT NULL DROP TABLE PhieuNhap;
IF OBJECT_ID('LichSuBan', 'U') IS NOT NULL DROP TABLE LichSuBan;
IF OBJECT_ID('DienThoai', 'U') IS NOT NULL DROP TABLE DienThoai;
IF OBJECT_ID('NhaCungCap', 'U') IS NOT NULL DROP TABLE NhaCungCap;
IF OBJECT_ID('HangSanXuat', 'U') IS NOT NULL DROP TABLE HangSanXuat;
IF OBJECT_ID('KhachHang', 'U') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('NguoiDung', 'U') IS NOT NULL DROP TABLE NguoiDung;
GO

-- ============================================================
--  1. NguoiDung
--  Giữ các cột cũ để DAO hiện tại đăng nhập và quản lý người dùng được.
-- ============================================================
CREATE TABLE NguoiDung (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    username    NVARCHAR(50)  NOT NULL UNIQUE,
    matkhau     NVARCHAR(100) NOT NULL,
    hoten       NVARCHAR(100),
    vaitro      NVARCHAR(20)  NOT NULL DEFAULT 'nhanvien',
    email       NVARCHAR(100),
    sdt         NVARCHAR(20),
    trangthai   NVARCHAR(20)  NOT NULL DEFAULT 'Hoat dong',
    ngay_tao    DATETIME      NOT NULL DEFAULT GETDATE(),
    CONSTRAINT CK_NguoiDung_VaiTro
        CHECK (vaitro IN ('admin', 'nhanvien')),
    CONSTRAINT CK_NguoiDung_TrangThai
        CHECK (trangthai IN ('Hoat dong', 'Khoa')),
    CONSTRAINT CK_NguoiDung_SDT
        CHECK (sdt IS NULL OR (sdt NOT LIKE '%[^0-9]%' AND LEN(sdt) IN (9,10)))
);
GO

-- ============================================================
--  2. KhachHang
-- ============================================================
CREATE TABLE KhachHang (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ho_ten      NVARCHAR(100) NOT NULL,
    sdt         NVARCHAR(20)  NOT NULL UNIQUE,
    email       NVARCHAR(100),
    dia_chi     NVARCHAR(255),
    ngay_tao    DATETIME      NOT NULL DEFAULT GETDATE(),
    trang_thai  NVARCHAR(20)  NOT NULL DEFAULT 'Hoat dong',
    CONSTRAINT CK_KhachHang_TrangThai
        CHECK (trang_thai IN ('Hoat dong', 'Khoa')),
    CONSTRAINT CK_KhachHang_SDT
        CHECK (sdt NOT LIKE '%[^0-9]%' AND LEN(sdt) IN (9,10))
);
GO

-- ============================================================
--  3. HangSanXuat
-- ============================================================
CREATE TABLE HangSanXuat (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    ten_hang    NVARCHAR(80)  NOT NULL UNIQUE,
    quoc_gia    NVARCHAR(80),
    mo_ta       NVARCHAR(255),
    trang_thai  NVARCHAR(20)  NOT NULL DEFAULT 'Hoat dong',
    CONSTRAINT CK_HangSanXuat_TrangThai
        CHECK (trang_thai IN ('Hoat dong', 'Ngung KD'))
);
GO

-- ============================================================
--  4. NhaCungCap
-- ============================================================
CREATE TABLE NhaCungCap (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    ten_nha_cung_cap NVARCHAR(150) NOT NULL UNIQUE,
    nguoi_lien_he   NVARCHAR(100),
    sdt             NVARCHAR(20),
    email           NVARCHAR(100),
    dia_chi         NVARCHAR(255),
    trang_thai      NVARCHAR(20) NOT NULL DEFAULT 'Hoat dong',
    CONSTRAINT CK_NhaCungCap_TrangThai
        CHECK (trang_thai IN ('Hoat dong', 'Ngung hop tac')),
    CONSTRAINT CK_NhaCungCap_SDT
        CHECK (sdt IS NULL OR (sdt NOT LIKE '%[^0-9]%' AND LEN(sdt) IN (9,10)))
);
GO

-- ============================================================
--  5. DienThoai
--  Giữ các cột ten_may, hang, model, gia_nhap, gia_ban, ton_kho,
--  da_ban, ram, mau_sac, trang_thai để ứng dụng hiện tại không bị lỗi.
--  hang_id và nha_cung_cap_id dùng cho schema mở rộng.
-- ============================================================
CREATE TABLE DienThoai (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    ten_may          NVARCHAR(150) NOT NULL,
    hang             NVARCHAR(80),
    hang_id          INT NULL,
    nha_cung_cap_id  INT NULL,
    model            NVARCHAR(80),
    imei_mau         NVARCHAR(30),
    bo_nho           NVARCHAR(30),
    ram              NVARCHAR(20),
    mau_sac          NVARCHAR(50),
    gia_nhap         BIGINT NOT NULL DEFAULT 0,
    gia_ban          BIGINT NOT NULL DEFAULT 0,
    ton_kho          INT    NOT NULL DEFAULT 0,
    da_ban           INT    NOT NULL DEFAULT 0,
    thoi_gian_bao_hanh_thang INT NOT NULL DEFAULT 12,
    trang_thai       NVARCHAR(30) NOT NULL DEFAULT 'Con hang',
    ngay_tao         DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_DienThoai_HangSanXuat
        FOREIGN KEY (hang_id) REFERENCES HangSanXuat(id)
        ON DELETE SET NULL,
    CONSTRAINT FK_DienThoai_NhaCungCap
        FOREIGN KEY (nha_cung_cap_id) REFERENCES NhaCungCap(id)
        ON DELETE SET NULL,
    CONSTRAINT CK_DienThoai_Gia
        CHECK (gia_nhap >= 0 AND gia_ban >= 0 AND gia_ban >= gia_nhap),
    CONSTRAINT CK_DienThoai_SoLuong
        CHECK (ton_kho >= 0 AND da_ban >= 0),
    CONSTRAINT CK_DienThoai_TrangThai
        CHECK (trang_thai IN ('Con hang', 'Het hang', 'Ngung KD'))
);
GO

-- ============================================================
--  6. PhieuNhap
-- ============================================================
CREATE TABLE PhieuNhap (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu         NVARCHAR(30) NOT NULL UNIQUE,
    nha_cung_cap_id  INT NULL,
    nguoi_dung_id    INT NULL,
    ngay_nhap        DATETIME NOT NULL DEFAULT GETDATE(),
    tong_tien        BIGINT   NOT NULL DEFAULT 0,
    ghi_chu          NVARCHAR(255),
    trang_thai       NVARCHAR(20) NOT NULL DEFAULT 'Hoan thanh',
    CONSTRAINT FK_PhieuNhap_NhaCungCap
        FOREIGN KEY (nha_cung_cap_id) REFERENCES NhaCungCap(id)
        ON DELETE SET NULL,
    CONSTRAINT FK_PhieuNhap_NguoiDung
        FOREIGN KEY (nguoi_dung_id) REFERENCES NguoiDung(id)
        ON DELETE SET NULL,
    CONSTRAINT CK_PhieuNhap_TongTien
        CHECK (tong_tien >= 0),
    CONSTRAINT CK_PhieuNhap_TrangThai
        CHECK (trang_thai IN ('Nhap tam', 'Hoan thanh', 'Da huy'))
);
GO

-- ============================================================
--  7. ChiTietPhieuNhap
-- ============================================================
CREATE TABLE ChiTietPhieuNhap (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    phieu_nhap_id   INT NOT NULL,
    dien_thoai_id   INT NOT NULL,
    so_luong        INT NOT NULL,
    don_gia_nhap    BIGINT NOT NULL,
    thanh_tien      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ChiTietPhieuNhap_PhieuNhap
        FOREIGN KEY (phieu_nhap_id) REFERENCES PhieuNhap(id)
        ON DELETE CASCADE,
    CONSTRAINT FK_ChiTietPhieuNhap_DienThoai
        FOREIGN KEY (dien_thoai_id) REFERENCES DienThoai(id),
    CONSTRAINT CK_ChiTietPhieuNhap_SoLuong
        CHECK (so_luong > 0),
    CONSTRAINT CK_ChiTietPhieuNhap_Gia
        CHECK (don_gia_nhap >= 0)
);
GO

-- ============================================================
--  8. HoaDon
-- ============================================================
CREATE TABLE HoaDon (
    id             INT IDENTITY(1,1) PRIMARY KEY,
    ma_hoa_don     NVARCHAR(30) NOT NULL UNIQUE,
    khach_hang_id  INT NULL,
    nguoi_dung_id  INT NULL,
    ngay_ban       DATETIME NOT NULL DEFAULT GETDATE(),
    tong_tien      BIGINT   NOT NULL DEFAULT 0,
    giam_gia       BIGINT   NOT NULL DEFAULT 0,
    ghi_chu        NVARCHAR(255),
    trang_thai     NVARCHAR(20) NOT NULL DEFAULT 'Hoan thanh',
    CONSTRAINT FK_HoaDon_KhachHang
        FOREIGN KEY (khach_hang_id) REFERENCES KhachHang(id)
        ON DELETE SET NULL,
    CONSTRAINT FK_HoaDon_NguoiDung
        FOREIGN KEY (nguoi_dung_id) REFERENCES NguoiDung(id)
        ON DELETE SET NULL,
    CONSTRAINT CK_HoaDon_Tien
        CHECK (tong_tien >= 0 AND giam_gia >= 0),
    CONSTRAINT CK_HoaDon_TrangThai
        CHECK (trang_thai IN ('Nhap tam', 'Hoan thanh', 'Da huy'))
);
GO

-- ============================================================
--  9. ChiTietHoaDon
-- ============================================================
CREATE TABLE ChiTietHoaDon (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    hoa_don_id      INT NOT NULL,
    dien_thoai_id   INT NOT NULL,
    so_luong        INT NOT NULL,
    don_gia_ban     BIGINT NOT NULL,
    don_gia_nhap    BIGINT NOT NULL DEFAULT 0,
    thanh_tien      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ChiTietHoaDon_HoaDon
        FOREIGN KEY (hoa_don_id) REFERENCES HoaDon(id)
        ON DELETE CASCADE,
    CONSTRAINT FK_ChiTietHoaDon_DienThoai
        FOREIGN KEY (dien_thoai_id) REFERENCES DienThoai(id),
    CONSTRAINT CK_ChiTietHoaDon_SoLuong
        CHECK (so_luong > 0),
    CONSTRAINT CK_ChiTietHoaDon_Gia
        CHECK (don_gia_ban >= 0 AND don_gia_nhap >= 0 AND don_gia_ban >= don_gia_nhap)
);
GO

-- ============================================================
--  10. BaoHanh
-- ============================================================
CREATE TABLE BaoHanh (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    hoa_don_id      INT NULL,
    khach_hang_id   INT NULL,
    dien_thoai_id   INT NOT NULL,
    imei            NVARCHAR(30) NOT NULL UNIQUE,
    ngay_bat_dau    DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    ngay_ket_thuc   DATE NOT NULL,
    noi_dung        NVARCHAR(255),
    trang_thai      NVARCHAR(20) NOT NULL DEFAULT 'Con hieu luc',
    CONSTRAINT FK_BaoHanh_HoaDon
        FOREIGN KEY (hoa_don_id) REFERENCES HoaDon(id)
        ON DELETE SET NULL,
    CONSTRAINT FK_BaoHanh_KhachHang
        FOREIGN KEY (khach_hang_id) REFERENCES KhachHang(id)
        ON DELETE SET NULL,
    CONSTRAINT FK_BaoHanh_DienThoai
        FOREIGN KEY (dien_thoai_id) REFERENCES DienThoai(id)
        ON DELETE CASCADE,
    CONSTRAINT CK_BaoHanh_Ngay
        CHECK (ngay_ket_thuc >= ngay_bat_dau),
    CONSTRAINT CK_BaoHanh_TrangThai
        CHECK (trang_thai IN ('Con hieu luc', 'Het han', 'Da huy'))
);
GO

-- Index hỗ trợ tìm kiếm/thống kê
CREATE INDEX IX_DienThoai_Hang ON DienThoai(hang);
CREATE INDEX IX_DienThoai_TrangThai ON DienThoai(trang_thai);
CREATE INDEX IX_HoaDon_NgayBan ON HoaDon(ngay_ban);
CREATE INDEX IX_PhieuNhap_NgayNhap ON PhieuNhap(ngay_nhap);
CREATE INDEX IX_BaoHanh_Imei ON BaoHanh(imei);
GO

-- ============================================================
--  Trigger cập nhật tồn kho khi nhập hàng
-- ============================================================
CREATE TRIGGER trg_ChiTietPhieuNhap_Insert
ON ChiTietPhieuNhap
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE ctpn
    SET ctpn.thanh_tien = CONVERT(BIGINT, ctpn.so_luong) * ctpn.don_gia_nhap
    FROM ChiTietPhieuNhap ctpn
    INNER JOIN inserted i ON i.id = ctpn.id;

    UPDATE dt
    SET
        dt.ton_kho = dt.ton_kho + x.so_luong,
        dt.gia_nhap = CASE WHEN x.don_gia_nhap > 0 THEN x.don_gia_nhap ELSE dt.gia_nhap END,
        dt.trang_thai = CASE
            WHEN dt.trang_thai = 'Ngung KD' THEN dt.trang_thai
            ELSE 'Con hang'
        END
    FROM DienThoai dt
    INNER JOIN (
        SELECT dien_thoai_id, SUM(so_luong) AS so_luong, MAX(don_gia_nhap) AS don_gia_nhap
        FROM inserted
        GROUP BY dien_thoai_id
    ) x ON x.dien_thoai_id = dt.id;

    UPDATE pn
    SET pn.tong_tien = x.tong_tien
    FROM PhieuNhap pn
    INNER JOIN (
        SELECT phieu_nhap_id, SUM(thanh_tien) AS tong_tien
        FROM ChiTietPhieuNhap
        GROUP BY phieu_nhap_id
    ) x ON x.phieu_nhap_id = pn.id
    WHERE pn.id IN (SELECT DISTINCT phieu_nhap_id FROM inserted);
END;
GO

-- ============================================================
--  Trigger cập nhật tồn kho và đã bán khi bán hàng
-- ============================================================
CREATE TRIGGER trg_ChiTietHoaDon_Insert
ON ChiTietHoaDon
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE cthd
    SET cthd.thanh_tien = CONVERT(BIGINT, cthd.so_luong) * cthd.don_gia_ban
    FROM ChiTietHoaDon cthd
    INNER JOIN inserted i ON i.id = cthd.id;

    UPDATE dt
    SET
        dt.ton_kho = dt.ton_kho - x.so_luong,
        dt.da_ban = dt.da_ban + x.so_luong,
        dt.trang_thai = CASE
            WHEN dt.trang_thai = 'Ngung KD' THEN dt.trang_thai
            WHEN dt.ton_kho - x.so_luong <= 0 THEN 'Het hang'
            ELSE 'Con hang'
        END
    FROM DienThoai dt
    INNER JOIN (
        SELECT dien_thoai_id, SUM(so_luong) AS so_luong
        FROM inserted
        GROUP BY dien_thoai_id
    ) x ON x.dien_thoai_id = dt.id;

    UPDATE hd
    SET hd.tong_tien = x.tong_tien
    FROM HoaDon hd
    INNER JOIN (
        SELECT hoa_don_id, SUM(thanh_tien) AS tong_tien
        FROM ChiTietHoaDon
        GROUP BY hoa_don_id
    ) x ON x.hoa_don_id = hd.id
    WHERE hd.id IN (SELECT DISTINCT hoa_don_id FROM inserted);
END;
GO

-- ============================================================
--  DỮ LIỆU MẪU
-- ============================================================

INSERT INTO NguoiDung(username, matkhau, hoten, vaitro, email, sdt, trangthai) VALUES
('admin',     '123456', N'Quản Trị Viên',  'admin',    'admin@phone.vn',  '0900000001', 'Hoat dong'),
('nhanvien1', '123456', N'Nguyễn Văn An',  'nhanvien', 'nva@phone.vn',    '0900000002', 'Hoat dong'),
('nhanvien2', '123456', N'Trần Thị Bình',  'nhanvien', 'ttb@phone.vn',    '0900000003', 'Hoat dong');
GO

INSERT INTO KhachHang(ho_ten, sdt, email, dia_chi) VALUES
(N'Lê Minh Hoàng',     '0911111111', 'hoang.le@example.com',  N'Quận 1, TP.HCM'),
(N'Phạm Thu Hương',    '0922222222', 'huong.pham@example.com',N'Quận Bình Thạnh, TP.HCM'),
(N'Đỗ Quốc Việt',      '0933333333', 'viet.do@example.com',   N'Quận Cầu Giấy, Hà Nội'),
(N'Nguyễn Mai Anh',    '0944444444', 'maianh@example.com',    N'Quận Hải Châu, Đà Nẵng'),
(N'Trần Gia Bảo',      '0955555555', 'baotran@example.com',   N'Thủ Đức, TP.HCM'),
(N'Võ Khánh Linh',     '0966666666', 'linh.vo@example.com',   N'Quận Ninh Kiều, Cần Thơ');
GO

INSERT INTO HangSanXuat(ten_hang, quoc_gia, mo_ta) VALUES
('Apple',   N'Mỹ',       N'iPhone và hệ sinh thái iOS'),
('Samsung', N'Hàn Quốc', N'Dòng Galaxy cao cấp và tầm trung'),
('Xiaomi',  N'Trung Quốc', N'Điện thoại hiệu năng cao giá tốt'),
('OPPO',    N'Trung Quốc', N'Điện thoại camera và sạc nhanh'),
('Vivo',    N'Trung Quốc', N'Dòng X và V series'),
('Google',  N'Mỹ',       N'Pixel chạy Android gốc'),
('OnePlus', N'Trung Quốc', N'Dòng flagship hiệu năng cao'),
('Realme',  N'Trung Quốc', N'Dòng phổ thông và gaming'),
('Nokia',   N'Phần Lan', N'Dòng bền pin, phổ thông');
GO

INSERT INTO NhaCungCap(ten_nha_cung_cap, nguoi_lien_he, sdt, email, dia_chi) VALUES
(N'Công ty TNHH Di Động Việt', N'Nguyễn Đức Long', '0281111111', 'sales@ddv.vn',       N'Quận 3, TP.HCM'),
(N'FPT Trading',              N'Trần Minh Quân',  '0242222222', 'contact@fpttrading.vn', N'Cầu Giấy, Hà Nội'),
(N'Synnex FPT',               N'Lê Khánh Duy',    '0283333333', 'info@synnexfpt.vn', N'Tân Bình, TP.HCM'),
(N'Petrosetco Distribution',  N'Phạm Hoài Nam',   '0284444444', 'sales@psd.vn',      N'Quận 1, TP.HCM'),
(N'Digiworld',                N'Vũ Minh Châu',    '0285555555', 'contact@dgw.vn',    N'Thủ Đức, TP.HCM');
GO

INSERT INTO DienThoai(
    ten_may, hang, hang_id, nha_cung_cap_id, model, bo_nho, ram, mau_sac,
    gia_nhap, gia_ban, ton_kho, da_ban, thoi_gian_bao_hanh_thang, trang_thai
) VALUES
(N'iPhone 15 Pro Max',        'Apple',   1, 1, 'A3295',   '256GB', '8GB',  N'Titan Đen',    28000000, 34990000, 0, 0, 12, 'Con hang'),
(N'iPhone 15',                'Apple',   1, 1, 'A3090',   '128GB', '6GB',  N'Hồng',         17000000, 22990000, 0, 0, 12, 'Con hang'),
(N'Samsung Galaxy S24 Ultra', 'Samsung', 2, 2, 'SM-S928', '256GB', '12GB', N'Titan Gray',   22000000, 27990000, 0, 0, 12, 'Con hang'),
(N'Samsung Galaxy A55',       'Samsung', 2, 2, 'SM-A556', '128GB', '8GB',  N'Xanh',         8000000,  10990000, 0, 0, 12, 'Con hang'),
(N'Xiaomi 14 Ultra',          'Xiaomi',  3, 3, '2402OPN', '512GB', '16GB', N'Đen',          21000000, 26990000, 0, 0, 18, 'Con hang'),
(N'OPPO Find X8 Pro',         'OPPO',    4, 4, 'PJD110',  '256GB', '12GB', N'Xanh Navy',    18000000, 23990000, 0, 0, 12, 'Con hang'),
(N'Vivo X100 Pro',            'Vivo',    5, 5, 'V2309',   '512GB', '16GB', N'Đen',          17000000, 22490000, 0, 0, 12, 'Het hang'),
(N'Google Pixel 9 Pro',       'Google',  6, 3, 'GKX7N',   '256GB', '16GB', N'Xanh Băng',    19000000, 24990000, 0, 0, 12, 'Con hang'),
(N'OnePlus 12',               'OnePlus', 7, 5, 'PH82110', '256GB', '12GB', N'Xanh Ngọc',    14000000, 18990000, 0, 0, 12, 'Ngung KD'),
(N'Realme GT6',               'Realme',  8, 4, 'RMX3851', '256GB', '12GB', N'Bạc',          7000000,  9990000,  0, 0, 12, 'Con hang'),
(N'Nokia G42 5G',             'Nokia',   9, 2, 'TA-1581', '128GB', '6GB',  N'Xanh Lá',      4000000,  5990000,  0, 0, 12, 'Con hang'),
(N'Xiaomi Redmi Note 13',     'Xiaomi',  3, 3, '2312DRA', '128GB', '8GB',  N'Xanh Bạc Hà',  5000000,  6990000,  0, 0, 12, 'Con hang');
GO

INSERT INTO PhieuNhap(ma_phieu, nha_cung_cap_id, nguoi_dung_id, ngay_nhap, ghi_chu) VALUES
('PN001', 1, 1, DATEADD(day, -60, GETDATE()), N'Nhập đợt iPhone'),
('PN002', 2, 2, DATEADD(day, -55, GETDATE()), N'Nhập Samsung và Nokia'),
('PN003', 3, 2, DATEADD(day, -50, GETDATE()), N'Nhập Xiaomi, Pixel'),
('PN004', 4, 3, DATEADD(day, -45, GETDATE()), N'Nhập OPPO, Realme'),
('PN005', 5, 1, DATEADD(day, -40, GETDATE()), N'Nhập Vivo, OnePlus');
GO

INSERT INTO ChiTietPhieuNhap(phieu_nhap_id, dien_thoai_id, so_luong, don_gia_nhap) VALUES
(1, 1, 170, 28000000),
(1, 2, 280, 17000000),
(2, 3, 120, 22000000),
(2, 4, 420,  8000000),
(2, 11,120,  4000000),
(3, 5,  65, 21000000),
(3, 8,  40, 19000000),
(3, 12,600,  5000000),
(4, 6,  65, 18000000),
(4, 10,210,  7000000),
(5, 7,  20, 17000000),
(5, 9,  10, 14000000);
GO

INSERT INTO HoaDon(ma_hoa_don, khach_hang_id, nguoi_dung_id, ngay_ban, ghi_chu) VALUES
('HD001', 1, 2, DATEADD(day, -30, GETDATE()), N'Bán lô khách doanh nghiệp'),
('HD002', 2, 2, DATEADD(day, -25, GETDATE()), N'Bán online'),
('HD003', 3, 3, DATEADD(day, -20, GETDATE()), N'Bán tại cửa hàng'),
('HD004', 4, 2, DATEADD(day, -15, GETDATE()), N'Khách mua số lượng lớn'),
('HD005', 5, 1, DATEADD(day, -10, GETDATE()), N'Bán theo chương trình khuyến mãi'),
('HD006', 6, 3, DATEADD(day,  -7, GETDATE()), N'Bán lẻ'),
('HD007', 1, 2, DATEADD(day,  -5, GETDATE()), N'Khách quay lại'),
('HD008', 2, 3, DATEADD(day,  -3, GETDATE()), N'Bán cuối tuần'),
('HD009', 3, 2, DATEADD(day,  -2, GETDATE()), N'Xả hàng'),
('HD010', 4, 1, DATEADD(day,  -1, GETDATE()), N'Bán hôm qua');
GO

INSERT INTO ChiTietHoaDon(hoa_don_id, dien_thoai_id, so_luong, don_gia_ban, don_gia_nhap) VALUES
(1,  1,  40, 34990000, 28000000),
(1,  2,  80, 22990000, 17000000),
(1,  4, 100, 10990000,  8000000),
(2,  1,  50, 34990000, 28000000),
(2,  3,  40, 27990000, 22000000),
(2, 12, 150,  6990000,  5000000),
(3,  2,  70, 22990000, 17000000),
(3, 10,  75,  9990000,  7000000),
(4,  4, 120, 10990000,  8000000),
(4,  5,  50, 26990000, 21000000),
(5,  3,  50, 27990000, 22000000),
(5, 11,  80,  5990000,  4000000),
(6,  6,  40, 23990000, 18000000),
(6,  8,  30, 24990000, 19000000),
(7,  1,  30, 34990000, 28000000),
(7,  2,  50, 22990000, 17000000),
(7,  7,  20, 22490000, 17000000),
(8,  4,  80, 10990000,  8000000),
(8, 10,  75,  9990000,  7000000),
(9,  9,  10, 18990000, 14000000),
(9, 12, 100,  6990000,  5000000),
(10,12, 150,  6990000,  5000000);
GO

-- Cap nhat tong tien tu chi tiet
UPDATE pn
SET tong_tien = x.tong_tien
FROM PhieuNhap pn
INNER JOIN (
    SELECT phieu_nhap_id, SUM(thanh_tien) AS tong_tien
    FROM ChiTietPhieuNhap
    GROUP BY phieu_nhap_id
) x ON x.phieu_nhap_id = pn.id;
GO

UPDATE hd
SET tong_tien = x.tong_tien
FROM HoaDon hd
INNER JOIN (
    SELECT hoa_don_id, SUM(thanh_tien) AS tong_tien
    FROM ChiTietHoaDon
    GROUP BY hoa_don_id
) x ON x.hoa_don_id = hd.id;
GO

INSERT INTO BaoHanh(
    hoa_don_id, khach_hang_id, dien_thoai_id, imei,
    ngay_bat_dau, ngay_ket_thuc, noi_dung, trang_thai
) VALUES
(1, 1, 1, 'IMEI000000000001', DATEADD(day, -30, CAST(GETDATE() AS DATE)), DATEADD(month, 12, DATEADD(day, -30, CAST(GETDATE() AS DATE))), N'Bảo hành máy iPhone 15 Pro Max', 'Con hieu luc'),
(2, 2, 3, 'IMEI000000000002', DATEADD(day, -25, CAST(GETDATE() AS DATE)), DATEADD(month, 12, DATEADD(day, -25, CAST(GETDATE() AS DATE))), N'Bảo hành Galaxy S24 Ultra', 'Con hieu luc'),
(4, 4, 5, 'IMEI000000000003', DATEADD(day, -15, CAST(GETDATE() AS DATE)), DATEADD(month, 18, DATEADD(day, -15, CAST(GETDATE() AS DATE))), N'Bảo hành Xiaomi 14 Ultra', 'Con hieu luc'),
(6, 6, 6, 'IMEI000000000004', DATEADD(day,  -7, CAST(GETDATE() AS DATE)), DATEADD(month, 12, DATEADD(day,  -7, CAST(GETDATE() AS DATE))), N'Bảo hành OPPO Find X8 Pro', 'Con hieu luc'),
(7, 1, 7, 'IMEI000000000005', DATEADD(day,  -5, CAST(GETDATE() AS DATE)), DATEADD(month, 12, DATEADD(day,  -5, CAST(GETDATE() AS DATE))), N'Bảo hành Vivo X100 Pro', 'Con hieu luc'),
(9, 3, 9, 'IMEI000000000006', DATEADD(day,  -2, CAST(GETDATE() AS DATE)), DATEADD(month, 12, DATEADD(day,  -2, CAST(GETDATE() AS DATE))), N'Bảo hành OnePlus 12', 'Con hieu luc');
GO

-- San pham ngung kinh doanh duoc giu trang thai rieng
UPDATE DienThoai
SET trang_thai = 'Ngung KD'
WHERE hang = 'OnePlus' AND model = 'PH82110';
GO

-- ============================================================
--  KIEM TRA NHANH
-- ============================================================

SELECT N'NguoiDung' AS Bang, COUNT(*) AS SoBanGhi FROM NguoiDung UNION ALL
SELECT N'KhachHang',       COUNT(*) FROM KhachHang UNION ALL
SELECT N'HangSanXuat',     COUNT(*) FROM HangSanXuat UNION ALL
SELECT N'NhaCungCap',      COUNT(*) FROM NhaCungCap UNION ALL
SELECT N'DienThoai',       COUNT(*) FROM DienThoai UNION ALL
SELECT N'PhieuNhap',       COUNT(*) FROM PhieuNhap UNION ALL
SELECT N'ChiTietPhieuNhap',COUNT(*) FROM ChiTietPhieuNhap UNION ALL
SELECT N'HoaDon',          COUNT(*) FROM HoaDon UNION ALL
SELECT N'ChiTietHoaDon',   COUNT(*) FROM ChiTietHoaDon UNION ALL
SELECT N'BaoHanh',         COUNT(*) FROM BaoHanh;
GO

SELECT
    COUNT(*) AS TongSanPham,
    SUM(CASE WHEN trang_thai = 'Con hang' THEN 1 ELSE 0 END) AS ConHang,
    SUM(CASE WHEN trang_thai = 'Het hang' THEN 1 ELSE 0 END) AS HetHang,
    SUM(CASE WHEN trang_thai = 'Ngung KD' THEN 1 ELSE 0 END) AS NgungKD,
    SUM(ton_kho) AS TongTonKho,
    SUM(da_ban) AS TongDaBan,
    SUM(gia_ban * da_ban) AS DoanhThu,
    SUM(gia_nhap * da_ban) AS Von,
    SUM((gia_ban - gia_nhap) * da_ban) AS LaiRong
FROM DienThoai;
GO

SELECT
    dt.id,
    dt.ten_may,
    dt.hang,
    dt.model,
    dt.ton_kho,
    dt.da_ban,
    dt.trang_thai
FROM DienThoai dt
ORDER BY dt.id;
GO

-- ============================================================
--  TAI KHOAN MAU
--  admin/123456 | nhanvien1/123456 | nhanvien2/123456
-- ============================================================
