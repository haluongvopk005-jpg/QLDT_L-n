# PHÂN CHIA NHIỆM VỤ 4 THÀNH VIÊN

> **Ghi chú cho bản TODO:** Các số dòng bên dưới mô tả mã nguồn hoàn chỉnh ban
> đầu, không phải số dòng hiện tại. Tên và vị trí của đủ 36 tệp vẫn được giữ
> nguyên. 12 tệp của Võ còn nguyên nội dung; 24 tệp của Khuyến, Hoài và Minh đã
> được thay bằng khung lớp cùng comment `TODO [TÊN]` để từng bạn viết lại trực
> tiếp trong đúng tệp được giao.

## 1. Võ — Trưởng nhóm: Đăng nhập, phân quyền, database và tích hợp

**Mã nguồn phụ trách:** 12 tệp Java, khoảng 1.615 dòng.

- `App.java` dòng 1–30: khởi động chương trình và chuẩn bị kết nối nền.
- `AppConfig.java` dòng 1–56: cấu hình SQL Server và giao diện chung.
- `DatabaseConnection.java` dòng 1–34: JDBC driver và kết nối database.
- `NguoiDung.java` dòng 1–76; `NguoiDungDAO.java` dòng 1–108: tài khoản, vai trò, đăng nhập và khóa tài khoản.
- `LoginPanel.java` dòng 1–257: giao diện và xác thực đăng nhập bất đồng bộ.
- `RegisterAccountDialog.java` dòng 1–247: đăng ký tài khoản.
- `UserPanel.java` dòng 1–184; `UserFormDialog.java` dòng 1–231: quản trị người dùng.
- `MainFrame.java` dòng 1–175: menu, phân quyền và tích hợp các màn hình.
- `UIHelper.java` dòng 1–175; `UiTaskRunner.java` dòng 1–42: tiện ích giao diện và xử lý nền.
- Quản lý `pom.xml`, `SQL_SETUP.sql`, script chạy, tài liệu và bản tích hợp cuối.

**Nghiệp vụ:** đăng nhập, đăng ký, phân quyền Admin/Nhân viên, kết nối SQL Server,
khóa tài khoản, điều hướng màn hình, xử lý nền và ghép toàn bộ module.

## 2. Khuyến — Quản lý điện thoại và tồn kho

**Mã nguồn phụ trách:** 4 tệp Java, khoảng 915 dòng.

- `DienThoai.java` dòng 1–97: model điện thoại và trạng thái sản phẩm.
- `DienThoaiDAO.java` dòng 1–175: truy vấn, thống kê, thêm, sửa và ngừng kinh doanh.
- `PhonePanel.java` dòng 1–393: danh sách, tìm kiếm, phân quyền và xuất PDF.
- `PhoneFormDialog.java` dòng 1–250: form, kiểm tra giá, tồn kho và dữ liệu điện thoại.
- SQL liên quan: `SQL_SETUP.sql` dòng 93–103, 128–160, 398–408 và 418–434.

**Nghiệp vụ:** quản lý điện thoại, hãng, giá nhập–giá bán, tồn kho, số đã bán,
trạng thái kinh doanh, phân quyền giá nhập và xuất danh sách PDF.

## 3. Hoài — Nhà cung cấp và nhập hàng

**Mã nguồn phụ trách:** 7 tệp Java, khoảng 696 dòng.

- `NhaCungCap.java` dòng 1–32; `NhaCungCapDAO.java` dòng 1–52.
- `NhaCungCapPanel.java` dòng 1–62; `NhaCungCapFormDialog.java` dòng 1–58.
- `NhapHangDAO.java` dòng 1–65: transaction nhập hàng và lịch sử nhập.
- `NhapHangPanel.java` dòng 1–403: giao diện, kiểm tra và thực hiện nhập hàng.
- `BusinessCodeGenerator.java` dòng 1–24: sinh mã nghiệp vụ không trùng.
- SQL liên quan: `SQL_SETUP.sql` dòng 107–121, 164–207, 297–335 và 410–457.

**Nghiệp vụ:** quản lý nhà cung cấp, ngừng hợp tác, lập phiếu nhập, kiểm tra giá nhập,
cập nhật tồn kho trong transaction và xem lịch sử nhập hàng.

## 4. Minh — Khách hàng, bán hàng, hóa đơn và báo cáo

**Mã nguồn phụ trách:** 13 tệp Java, khoảng 1.452 dòng.

- `KhachHang.java` dòng 1–28; `KhachHangDAO.java` dòng 1–52.
- `KhachHangPanel.java` dòng 1–62; `KhachHangFormDialog.java` dòng 1–57.
- `BanHangDAO.java` dòng 1–121; `BanHangPanel.java` dòng 1–152.
- `HoaDon.java` dòng 1–27; `HoaDonDAO.java` dòng 1–39; `HoaDonPanel.java` dòng 1–88.
- `BaoCaoDAO.java` dòng 1–81; `BaoCaoPanel.java` dòng 1–500.
- `DashboardPanel.java` dòng 1–162; `PdfFonts.java` dòng 1–83.
- SQL liên quan: `SQL_SETUP.sql` dòng 75–89, 211–293, 339–378, 389–396 và 459–534.

**Nghiệp vụ:** quản lý khách hàng, giỏ hàng, thanh toán, khóa tồn kho, hóa đơn,
PDF, Dashboard và báo cáo doanh thu–vốn–lợi nhuận.

## Quy trình tích hợp

1. Thành viên giữ nguyên package `com.phonemanager` và cấu trúc `src/main/java`.
2. Võ tổng hợp các tệp vào project chính, xử lý xung đột và kiểm tra Maven.
3. Chạy `SQL_SETUP.sql` trên môi trường thử nghiệm.
4. Biên dịch toàn bộ 36 tệp Java và chạy kiểm thử kết nối/DAO.
5. Đóng gói `target/PhoneManager.jar` và chạy thử hai vai trò trước khi bàn giao.
