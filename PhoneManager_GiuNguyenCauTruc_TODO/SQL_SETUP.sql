-- ============================================================
-- PhoneManagerDB - BO KHUNG PHAN CONG 4 THANH VIEN
--
-- VO: giu lai phan khoi tao database, NguoiDung va tich hop.
-- KHUYEN, HOAI, MINH: ma SQL cu da duoc xoa va thay bang TODO
-- ngay tai vi tri can bo sung.
--
-- CANH BAO: script nay xoa cac bang cu. Chi chay tren database thu nghiem.
-- ============================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'PhoneManagerDB')
    CREATE DATABASE PhoneManagerDB;
GO

USE PhoneManagerDB;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO

-- ============================================================
-- PHAN TICH HOP DO VO QUAN LY: DON DEP SCHEMA CU
-- ============================================================

IF OBJECT_ID('trg_ChiTietHoaDon_Insert', 'TR') IS NOT NULL
    DROP TRIGGER trg_ChiTietHoaDon_Insert;
GO

IF OBJECT_ID('trg_ChiTietPhieuNhap_Insert', 'TR') IS NOT NULL
    DROP TRIGGER trg_ChiTietPhieuNhap_Insert;
GO

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
-- PHAN CUA VO: NGUOI DUNG, DANG NHAP VA PHAN QUYEN
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
-- TODO [MINH]: BO SUNG BANG KHACH HANG TAI DAY
--
-- Can thuc hien:
-- - CREATE TABLE KhachHang.
-- - Khoa chinh, rang buoc so dien thoai/email va trang thai neu co.
-- - Bao dam cac cot khop voi KhachHang.java va KhachHangDAO.java.
-- ============================================================

-- TODO [MINH]: Viet SQL bang KhachHang ben duoi comment nay.

-- ============================================================
-- TODO [KHUYEN]: BO SUNG BANG HANG SAN XUAT TAI DAY
--
-- Can thuc hien:
-- - CREATE TABLE HangSanXuat.
-- - Ten hang duy nhat, quoc gia va mo ta.
-- ============================================================

-- TODO [KHUYEN]: Viet SQL bang HangSanXuat ben duoi comment nay.

-- ============================================================
-- TODO [HOAI]: BO SUNG BANG NHA CUNG CAP TAI DAY
--
-- Can thuc hien:
-- - CREATE TABLE NhaCungCap.
-- - Thong tin lien he va trang thai hop tac.
-- - Bao dam cot khop voi NhaCungCap.java/NhaCungCapDAO.java.
-- ============================================================

-- TODO [HOAI]: Viet SQL bang NhaCungCap ben duoi comment nay.

-- ============================================================
-- TODO [KHUYEN]: BO SUNG BANG DIEN THOAI TAI DAY
--
-- Can thuc hien:
-- - CREATE TABLE DienThoai sau HangSanXuat va NhaCungCap.
-- - Hang, model, gia nhap, gia ban, ton kho, da ban, RAM, mau sac.
-- - Rang buoc gia/so luong va trang thai kinh doanh.
-- - Khoa ngoai can thiet va index tim kiem.
-- ============================================================

-- TODO [KHUYEN]: Viet SQL bang DienThoai ben duoi comment nay.

-- ============================================================
-- TODO [HOAI]: BO SUNG PHIEU NHAP VA CHI TIET PHIEU NHAP
--
-- Can thuc hien:
-- - CREATE TABLE PhieuNhap.
-- - CREATE TABLE ChiTietPhieuNhap.
-- - Khoa ngoai den NhaCungCap, NguoiDung va DienThoai.
-- - Rang buoc so luong/don gia va ma phieu khong trung.
-- ============================================================

-- TODO [HOÀI]: Viết SQL hai bảng nhập hàng bên dưới comment này.

-- ============================================================
-- TODO [MINH]: BO SUNG HOA DON, CHI TIET HOA DON VA BAO HANH
--
-- Can thuc hien:
-- - CREATE TABLE HoaDon.
-- - CREATE TABLE ChiTietHoaDon.
-- - CREATE TABLE BaoHanh.
-- - Khoa ngoai den KhachHang, NguoiDung va DienThoai.
-- - Rang buoc so luong/don gia, ma hoa don va thoi han bao hanh.
-- ============================================================

-- TODO [MINH]: Viet SQL cac bang ban hang ben duoi comment nay.

-- ============================================================
-- TODO [HOAI]: BO SUNG TRIGGER/TRANSACTION HO TRO NHAP HANG
--
-- Can thuc hien:
-- - Cap nhat ton kho sau khi them ChiTietPhieuNhap.
-- - Dong bo gia nhap neu nghiep vu yeu cau.
-- - Bao dam loi lam transaction rollback, khong cap nhat do dang.
-- ============================================================

-- TODO [HOAI]: Viet trigger hoac thu tuc nhap hang ben duoi comment nay.

-- ============================================================
-- TODO [MINH]: BO SUNG TRIGGER/TRANSACTION HO TRO BAN HANG
--
-- Can thuc hien:
-- - Kiem tra va khoa ton kho khi thanh toan.
-- - Tru ton kho, tang da ban va cap nhat trang thai.
-- - Tinh tong hoa don va rollback khi khong du hang.
-- ============================================================

-- TODO [MINH]: Viet trigger hoac thu tuc ban hang ben duoi comment nay.

-- ============================================================
-- DU LIEU MAU PHAN CUA VO
-- ============================================================

INSERT INTO NguoiDung(username, matkhau, hoten, vaitro, email, sdt, trangthai) VALUES
('admin',     '123456', N'Quản Trị Viên', 'admin',    'admin@phone.vn', '0900000001', 'Hoat dong'),
('nhanvien1', '123456', N'Nguyễn Văn An', 'nhanvien', 'nva@phone.vn',   '0900000002', 'Hoat dong'),
('nhanvien2', '123456', N'Trần Thị Bình', 'nhanvien', 'ttb@phone.vn',   '0900000003', 'Hoat dong');
GO

-- ============================================================
-- TODO [KHUYEN]: BO SUNG DU LIEU MAU
-- HangSanXuat va DienThoai.
-- ============================================================

-- TODO [KHUYEN]: Viet INSERT du lieu mau ben duoi comment nay.

-- ============================================================
-- TODO [HOAI]: BO SUNG DU LIEU MAU
-- NhaCungCap, PhieuNhap va ChiTietPhieuNhap.
-- ============================================================

-- TODO [HOAI]: Viet INSERT du lieu mau ben duoi comment nay.

-- ============================================================
-- TODO [MINH]: BO SUNG DU LIEU MAU
-- KhachHang, HoaDon, ChiTietHoaDon va BaoHanh.
-- ============================================================

-- TODO [MINH]: Viet INSERT du lieu mau ben duoi comment nay.

-- ============================================================
-- KIEM TRA PHAN CUA VO
-- ============================================================

SELECT N'NguoiDung' AS Bang, COUNT(*) AS SoBanGhi
FROM NguoiDung;
GO

-- TODO [KHUYEN]: Them truy van kiem tra dien thoai va ton kho.
-- TODO [HOAI]: Them truy van kiem tra nha cung cap va lich su nhap.
-- TODO [MINH]: Them truy van kiem tra hoa don, doanh thu, von va loi nhuan.

-- Tai khoan mau:
-- admin/123456 | nhanvien1/123456 | nhanvien2/123456
