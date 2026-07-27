# PhoneManager

> **Bản phân công TODO:** Cấu trúc project và đủ 36 tên tệp Java được giữ như bản
> gốc. 12 tệp của Võ được giữ nguyên mã nguồn. Mã nghiệp vụ trong 4 tệp của
> Khuyến, 7 tệp của Hoài và 13 tệp của Minh đã được xóa; trong từng tệp có comment
> `TODO [TÊN]` ghi rõ phần phải bổ sung. `SQL_SETUP.sql` cũng được xử lý theo cùng
> nguyên tắc. Các lớp giao diện trống chỉ giữ constructor/phương thức tích hợp tối
> thiểu để project có thể biên dịch trong lúc chia việc.

Ứng dụng desktop quản lý cửa hàng điện thoại, phát triển bằng Java Swing và SQL Server.
Hệ thống hỗ trợ nghiệp vụ kho, nhập hàng, bán hàng, hóa đơn, khách hàng, nhà cung cấp,
người dùng và báo cáo doanh thu.

## Chức năng

- Đăng nhập và phân quyền Quản trị viên/Nhân viên.
- Quản lý danh mục điện thoại, hãng sản xuất và trạng thái kinh doanh.
- Quản lý khách hàng và nhà cung cấp.
- Lập phiếu nhập, cập nhật giá nhập và tồn kho.
- Lập hóa đơn bán hàng, kiểm tra tồn kho và cập nhật số lượng đã bán.
- Tra cứu hóa đơn, xem chi tiết và xuất PDF.
- Thống kê doanh thu, vốn, lợi nhuận, tồn kho và hiệu quả theo hãng.
- Khóa hoặc ngừng hoạt động bản ghi để bảo toàn lịch sử nghiệp vụ.

## Công nghệ và kiến trúc

- Java 17, Java Swing và Maven.
- SQL Server, JDBC, `PreparedStatement` và transaction.
- Kiến trúc phân tầng: `ui` → `dao` → SQL Server; `model` truyền dữ liệu giữa các tầng.
- `SwingWorker` thông qua `UiTaskRunner<T>` cho tác vụ cơ sở dữ liệu chạy nền.
- Collections, Generics, Lambda, Stream API và các mẫu thiết kế DAO/Factory/Callback.
- iText 5.5.13.3 để xuất tài liệu PDF.

## Yêu cầu môi trường

- JDK 17.
- SQL Server đang lắng nghe tại cổng TCP 1433.
- SQL Server Management Studio hoặc `sqlcmd`.
- IntelliJ IDEA có Maven tích hợp, hoặc Maven cài riêng.

## Cấu hình cơ sở dữ liệu

Giá trị mặc định:

| Thuộc tính | Giá trị |
|---|---|
| Máy chủ | `localhost` |
| Cổng | `1433` |
| Database | `PhoneManagerDB` |
| Người dùng | `sa` |
| Mật khẩu | `123456` |

Có thể cấu hình bằng biến môi trường, không cần thay đổi mã nguồn:

| Biến môi trường | Ý nghĩa |
|---|---|
| `PHONE_MANAGER_DB_HOST` | Máy chủ SQL Server |
| `PHONE_MANAGER_DB_PORT` | Cổng kết nối |
| `PHONE_MANAGER_DB_NAME` | Tên database |
| `PHONE_MANAGER_DB_USER` | Tài khoản SQL Server |
| `PHONE_MANAGER_DB_PASSWORD` | Mật khẩu SQL Server |

Ví dụ PowerShell:

```powershell
$env:PHONE_MANAGER_DB_PASSWORD = "mat-khau-sql-server"
```

## Khởi tạo dữ liệu

Chạy `SQL_SETUP.sql` bằng SQL Server Management Studio, hoặc chạy
`setup_database.bat` khi máy đã có `sqlcmd`.

> `SQL_SETUP.sql` tạo lại database và dữ liệu minh họa. Không chạy trên dữ liệu cần giữ
> nếu chưa sao lưu.

## Chạy ứng dụng

### Từ IntelliJ IDEA

1. Chọn **Open** và mở thư mục chứa `pom.xml`.
2. Chờ Maven tải thư viện.
3. Chạy `src/main/java/com/phonemanager/App.java`.

### Từ tệp JAR

Chạy `run_app.bat`, hoặc:

```powershell
java -Dfile.encoding=UTF-8 -jar target/PhoneManager.jar
```

### Đóng gói bằng Maven

```powershell
mvn clean package
```

Tệp thực thi được tạo tại `target/PhoneManager.jar`.

## Tài khoản minh họa

- Quản trị viên: `admin` / `123456`.
- Nhân viên: `nhanvien1` / `123456`.

## Cấu trúc mã nguồn

```text
src/main/java/com/phonemanager/
├── config/    Cấu hình ứng dụng
├── dao/       Truy vấn và transaction JDBC
├── model/     Mô hình dữ liệu
├── ui/        Giao diện Java Swing
└── util/      Tiện ích dùng chung
```

## Thành viên thực hiện

- Võ: trưởng nhóm, đăng nhập, phân quyền, database và tích hợp.
- Khuyến: quản lý điện thoại và tồn kho.
- Hoài: nhà cung cấp và nhập hàng.
- Minh: khách hàng, bán hàng, hóa đơn và báo cáo.

Tài liệu nghiệp vụ nằm trong `HUONG_DAN_SU_DUNG.md`. Nội dung kỹ thuật theo chương
môn học nằm trong `DOI_CHIEU_6_CHUONG.md`. Chi tiết file và dòng code của từng thành
viên nằm trong `PHAN_CONG_4_THANH_VIEN.md`.
