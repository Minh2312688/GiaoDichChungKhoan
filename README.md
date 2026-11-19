# TRADING SIMULATOR

## Hướng dẫn cài đặt và sử dụng

Trình mô phỏng giao dịch chứng khoán chạy bằng Java 17, Maven và Swing.

## I. Giới thiệu

Trading Simulator là ứng dụng desktop mô phỏng giao dịch chứng khoán. Ứng dụng hiển thị giá mô phỏng, quản lý danh mục, gửi lệnh mua/bán và minh họa các chiến lược bot. Ứng dụng không kết nối sàn giao dịch.

- Đây là môi trường paper trading: không sử dụng tiền thật và không gửi lệnh tới sàn.
- Giá và biểu đồ trong ứng dụng là dữ liệu mô phỏng, không đại diện cho giá hiện tại ngoài thị trường.
- Tài khoản và danh mục demo được lưu trong bộ nhớ của lần chạy; không đảm bảo được lưu sau khi đóng ứng dụng.
- Sau khi Maven tải đủ thư viện, ứng dụng không cần Internet để lấy dữ liệu thị trường.

## II. Yêu cầu cài đặt

| Thành phần | Yêu cầu / Ghi chú |
|---|---|
| Hệ điều hành | Windows 10/11; hướng dẫn dùng PowerShell. Maven Wrapper cũng hỗ trợ macOS/Linux. |
| Java | JDK 17; kiểm tra bằng `java -version`. |
| Maven | Không cần cài Maven riêng; dự án có Maven Wrapper (`mvnw.cmd`). |
| Internet | Cần ở lần chạy đầu để tải Maven và các thư viện dự án. |
| Mã nguồn | Giải nén hoặc clone đầy đủ dự án, bao gồm `mvnw.cmd` và thư mục `.mvn`. |

## III. Cài đặt và khởi chạy

### 1. Chuẩn bị dự án

- Giải nén hoặc clone dự án, ví dụ vào `D:\GiaoDichChungKhoan`.
- Mở PowerShell tại thư mục gốc dự án. Trong VS Code, chọn **File → Open Folder**, mở thư mục dự án, rồi chọn **Terminal → New Terminal**.
- Kiểm tra Java 17:

```powershell
java -version
```

Nếu Java chưa được nhận diện hoặc phiên bản không phải 17, cài JDK 17, cấu hình `PATH`, rồi mở lại PowerShell.

### 2. Kiểm thử

Chạy tại thư mục gốc dự án:

```powershell
.\mvnw.cmd test
```

Lần đầu cần Internet để Maven Wrapper tải Maven 3.9.9 và các thư viện cần thiết. Kết quả `BUILD SUCCESS` cho biết biên dịch và kiểm thử đã hoàn tất.

### 3. Khởi chạy giao diện

```powershell
.\mvnw.cmd exec:java
```

Lệnh mở hộp thoại đăng nhập trên máy có giao diện desktop. Sau khi xác thực, cửa sổ Trading Simulator sẽ hiện ra. Đóng ứng dụng bằng nút **X**.

Trên macOS/Linux, dùng `sh ./mvnw` thay cho `.\mvnw.cmd`, ví dụ `sh ./mvnw test` và `sh ./mvnw exec:java`.

### 4. Script và lưu ý

Trên Windows có thể chạy kiểm thử bằng:

```powershell
.\scripts\run-tests.ps1
```

Không dùng mục tiêu `clean` khi đang offline nếu Maven Clean Plugin chưa có trong cache.

## IV. Đăng nhập và tài khoản

Màn hình đầu tiên có hai tab **Đăng nhập** và **Đăng ký**. Tài khoản demo:

| Trường | Thông tin |
|---|---|
| Tên đăng nhập | `Demo` hoặc `Demo.trader@gmail.com` |
| Mật khẩu | `123` |
| Danh mục mẫu | Tiền mặt 25.000,00; AAPL, BTC, ETH, FPT, HPG; có lịch sử và lệnh chờ. |

- Nhập tài khoản và mật khẩu rồi bấm **Đăng nhập**.
- Có thể đăng ký tài khoản mới; mật khẩu tối thiểu 3 ký tự, số dư ban đầu 10.000,00.
- Tài khoản đăng ký và danh mục không được đảm bảo còn sau khi đóng ứng dụng.

## V. Sử dụng các chức năng

### 1. Tổng quan (Dashboard)

Hiển thị bảng giá mô phỏng, tiền mặt, danh mục và số lệnh chờ/trạng thái bot. Có thể tìm hoặc lọc mã. Nhấn đúp vào mã trong bảng giá hoặc danh mục để mở form giao dịch nhanh.

### 2. Đặt lệnh

Mở mục **Đặt lệnh**, chọn mã, chiều **MUA/BÁN**, loại lệnh và số lượng. Với lệnh có điều kiện, nhập thêm giá rồi bấm **Gửi lệnh**.

| Loại lệnh | Cách hoạt động |
|---|---|
| Thị trường (Market) | Khớp theo giá mô phỏng hiện tại nếu hợp lệ. |
| Giới hạn (Limit) | Đưa vào danh sách chờ theo mức giá đã nhập; có thể chưa khớp ngay. |
| Dừng lỗ (Stop-loss) | Đưa lệnh vào danh sách chờ với mức kích hoạt đã nhập. |

Lệnh **MUA** cần đủ tiền mô phỏng; lệnh **BÁN** cần đủ vị thế. Lỗi nhập liệu hiển thị bên dưới form.

### 3. Bot giao dịch

Mở **Bot giao dịch**, chọn **Trung bình động cắt nhau**, **Ngưỡng RSI**, **MACD** hoặc **Bollinger Bands**, rồi bật/tắt bot. Màn hình hiển thị trạng thái và chỉ báo khi có dữ liệu. Đây là chức năng minh họa, không phải khuyến nghị đầu tư.

### 4. Lịch sử và lệnh chờ

- **Lịch sử:** xem giao dịch đã khớp trong tài khoản/phiên hiện tại.
- **Lệnh chờ:** xem lệnh Limit và Stop-loss đang chờ; lọc theo tất cả, mua, bán hoặc đang chờ.

### 5. Cài đặt

Màn hình **Cài đặt** có lựa chọn theme và chu kỳ cập nhật giá. Bản hiện tại chưa áp dụng đổi theme trực tiếp lên toàn bộ giao diện; đổi chu kỳ không khởi động lại timer đang chạy.

## VI. Khắc phục sự cố

| Hiện tượng | Cách xử lý |
|---|---|
| Java không được nhận diện | Cài JDK 17, cấu hình `PATH`, mở lại PowerShell và chạy `java -version`. |
| Không tìm thấy `mvnw.cmd` | Kiểm tra đang ở thư mục gốc dự án và đã giải nén/clone đầy đủ, bao gồm `mvnw.cmd` và `.mvn`. |
| Lỗi tải Maven hoặc thư viện | Kiểm tra Internet/truy cập Maven Central rồi chạy lại lệnh. Lần đầu không thể tải dependency khi offline. |
| Không thấy giao diện | Chạy `.\mvnw.cmd exec:java` trong phiên desktop có giao diện; môi trường headless/terminal từ xa không hiển thị Swing. |
| Lệnh bị từ chối | Kiểm tra mã, chiều, số lượng, giá, số dư/vị thế và thông báo ở form. |

## Thông tin kỹ thuật

Mọi giá và tiền đều dùng `BigDecimal`; dữ liệu thị trường là dữ liệu mô phỏng. Các technical indicators (MA, RSI, MACD, Bollinger Bands) dùng `double` trong phép tính; khi hiển thị, indicator được làm tròn còn 2 hoặc 4 chữ số thập phân. Điều kiện tín hiệu dùng `<` và `>`, không dùng `==`.

Ứng dụng sử dụng `TradingFacade`, Adapter, Chain of Responsibility, Factory Method, Strategy, Memento, sổ lệnh chờ và lưu trữ JSON. Giao diện Swing điều hướng bằng `CardLayout` và `ScreenManager`, với `SidebarNavigation` và `MarketSessionController`.

## Phân công theo mẫu thiết kế

| Thành viên | Mẫu thiết kế | Module chính |
|---|---|---|
| Phan Thành Huy | **Singleton**, **Factory Method** | `core/ExchangeEngine`, `tradingengine/OrderFactory`, `tradingengine/OrderBook` |
| Dương Văn Minh | **Facade**, **Memento** | `core/TradingFacade`, `validationandportfolio/PortfolioMemento`, `persistence/*`, `reporting/*` |
| Lê Thị Ánh Nhung | **Adapter**, **Observer** | `marketdata/adapters/*`, `marketdata/observers/*` |
| Lý Ngọc Thảo Nguyên | **Strategy**, **Chain of Responsibility** | `tradingengine/botstrategies/*`, `validationandportfolio/ValidationChain` và các validator |

Chi tiết xem `docs/TEAM_WORK_DIVISION.md` (phân công) và `docs/KE_HOACH.md` (bảng kế hoạch). Quy ước làm việc xem `CONTRIBUTING.md`.
