# Hướng dẫn sử dụng PhoneManager

## Đăng nhập

1. Chọn đúng vai trò Quản trị viên hoặc Nhân viên.
2. Nhập tên đăng nhập và mật khẩu.
3. Bấm **Đăng nhập** và chờ hệ thống xác thực.

## Quyền Quản trị viên

- Xem Dashboard, doanh thu, vốn và lợi nhuận.
- Quản lý điện thoại, người dùng, khách hàng và nhà cung cấp.
- Nhập hàng và theo dõi lịch sử phiếu nhập.
- Bán hàng, quản lý hóa đơn và xuất PDF.
- Xem báo cáo chi tiết theo sản phẩm và hãng.

## Quyền Nhân viên

- Xem danh sách điện thoại không bao gồm giá nhập.
- Quản lý khách hàng và thực hiện bán hàng.
- Xem hóa đơn và báo cáo bán hàng được phân quyền.
- Không truy cập dữ liệu vốn, lợi nhuận, nhà cung cấp hoặc nhập hàng.

## Quy trình nhập hàng

1. Mở **Nhập hàng**.
2. Chọn nhà cung cấp và điện thoại.
3. Nhập số lượng, giá nhập và nội dung mô tả nếu cần.
4. Kiểm tra thông tin rồi bấm **Nhập hàng**.
5. Hệ thống lập phiếu nhập và cập nhật tồn kho trong cùng transaction.

## Quy trình bán hàng

1. Mở **Bán hàng**.
2. Chọn khách hàng và điện thoại.
3. Nhập số lượng rồi thêm sản phẩm vào giỏ.
4. Kiểm tra tổng tiền và bấm **Thanh toán**.
5. Hệ thống tạo hóa đơn, lưu chi tiết và cập nhật tồn kho.

## Hóa đơn và báo cáo

- Chọn một hóa đơn để xem chi tiết.
- Dùng **Xuất PDF** để lưu chứng từ.
- Dashboard và báo cáo lấy dữ liệu từ hóa đơn hoàn thành.

## Nguyên tắc dữ liệu

- Điện thoại được chuyển sang trạng thái **Ngừng kinh doanh** thay cho xóa vật lý.
- Khách hàng và người dùng được khóa khi ngừng sử dụng.
- Nhà cung cấp được chuyển sang **Ngừng hợp tác**.
- Các trạng thái này giúp giữ nguyên hóa đơn, phiếu nhập và số liệu báo cáo.
