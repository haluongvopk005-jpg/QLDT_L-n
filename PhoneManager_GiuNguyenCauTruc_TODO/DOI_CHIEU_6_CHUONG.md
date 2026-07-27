# Đối chiếu kiến thức sáu chương

## Chương 1 — Generics và Collections

- `List<DienThoai>`, `List<KhachHang>`, `List<NhaCungCap>` và `List<SaleLine>` bảo đảm
  an toàn kiểu dữ liệu.
- `ArrayList` lưu giỏ hàng và kết quả truy vấn.
- `LinkedHashMap<Integer, SaleLine>` gộp sản phẩm trùng trong giỏ và giữ thứ tự nhập.

## Chương 2 — Design Pattern và kiến trúc hướng sự kiện

- DAO Pattern tách câu lệnh SQL khỏi giao diện và mô hình dữ liệu.
- `UIHelper` cung cấp Factory Method cho button, field và bảng.
- Listener, lambda, `Runnable` và callback điều phối hành vi giao diện.
- Các package `ui`, `dao`, `model`, `config`, `util` thể hiện kiến trúc phân tầng.

## Chương 3 — Multithreading và Concurrency

- `UiTaskRunner<T>` sử dụng `SwingWorker<T, Void>` để chạy JDBC ngoài Event Dispatch Thread.
- Đăng nhập, tải danh sách, hóa đơn, báo cáo và Dashboard không chặn luồng giao diện.
- `BanHangDAO` sử dụng transaction, commit/rollback và `UPDLOCK`, `ROWLOCK` khi kiểm tra kho.
- Mã nghiệp vụ kết hợp thời gian và UUID để hạn chế trùng trong thao tác đồng thời.

## Chương 4 — Functional Programming

- Lambda được dùng cho listener, tác vụ nền và callback.
- `Callable<T>` và `Consumer<T>` là các functional interface của `UiTaskRunner`.
- Stream API dùng `filter()`, `findFirst()` và `toList()` trong nghiệp vụ giỏ hàng và lọc dữ liệu.

## Chương 5 — Kết nối cơ sở dữ liệu

- JDBC với `DriverManager`, `Connection`, `PreparedStatement` và `ResultSet`.
- `try-with-resources` quản lý vòng đời kết nối và câu lệnh.
- Transaction và batch áp dụng cho nhập hàng, bán hàng và chi tiết hóa đơn.
- SQL Server sử dụng khóa chính, khóa ngoại, ràng buộc, chỉ mục và trigger.

## Chương 6 — Lập trình giao diện

- Java Swing với `JFrame`, `JPanel`, `CardLayout`, `JTable`, dialog và event handling.
- `SwingUtilities.invokeLater()` khởi tạo giao diện trên Event Dispatch Thread.
- Thanh tiến trình và trạng thái xác thực cung cấp phản hồi trong lúc đăng nhập.
- Phân quyền quyết định menu, dữ liệu và thao tác được hiển thị cho từng vai trò.

## Phạm vi công nghệ

Dự án sử dụng Java Swing và JDBC phù hợp với ứng dụng desktop một máy. JavaFX,
Hibernate/JPA và message broker không thuộc phạm vi triển khai. Dữ liệu có quy mô nhỏ nên
Stream tuần tự phù hợp hơn `parallelStream()`.
