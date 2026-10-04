# Thiết kế giao diện Swing nhiều màn hình

## Mục tiêu

Tái cấu trúc giao diện Swing hiện tại từ bố cục một màn hình dạng `GridLayout(2, 2)` thành một cửa sổ chính có nhiều màn hình nội bộ. Mục tiêu là giảm mật độ thông tin, tạo thứ bậc rõ ràng và cho phép mở rộng thêm chức năng mà không mở nhiều cửa sổ desktop.

Phạm vi chỉ thay đổi lớp trình bày và cách điều phối cập nhật UI. `TradingFacade`, mô hình giao dịch, persistence và các chiến lược giao dịch vẫn giữ nguyên API hiện tại.

## Kiến trúc

```text
MainWindow
├── HeaderPanel
├── SidebarNavigation
├── CardLayout / ScreenManager
│   ├── DashboardPanel
│   ├── TradingPanel
│   ├── BotScreen
│   ├── HistoryScreen
│   ├── PendingOrdersScreen
│   └── SettingsPanel
└── StatusBar
```

`MainWindow` tạo khung cửa sổ, đăng ký các màn hình và kết nối điều hướng. Mỗi màn hình chỉ chịu trách nhiệm trình bày dữ liệu và phát sự kiện thao tác của người dùng.

## Các màn hình

### Dashboard

Hiển thị tổng quan nhanh:

- Giá gần nhất của các mã đang theo dõi.
- Tiền mặt hiện tại.
- Vị thế đang nắm giữ.
- Số lượng lệnh chờ.
- Trạng thái bot.

### Đặt lệnh

Chứa `OrderFormPanel`, gồm mã tài sản, chiều MUA/BÁN, khối lượng, nút gửi lệnh và thông báo kết quả. Form không tự quản lý timer hoặc dữ liệu thị trường.

### Bot giao dịch

Chứa phần điều khiển bot hiện tại: chọn chiến lược, bật/tắt bot, RSI, trạng thái xử lý và lịch sử giá cần thiết cho chỉ báo.

### Lịch sử giao dịch

Hiển thị các giao dịch đã khớp với các cột thời gian, mã, chiều, khối lượng, giá và tổng tiền.

### Lệnh chờ

Hiển thị các lệnh `LIMIT` và `STOP_LOSS` đang chờ, gồm mã, chiều, khối lượng, giá đặt và trạng thái. Màn hình phản ánh kết quả sau mỗi lần `matchPending()`.

### Cài đặt

Cung cấp các tùy chọn giao diện trong phạm vi MVP mới: theme sáng/tối, chu kỳ cập nhật giá và định dạng số hiển thị. Các tùy chọn phải có giá trị mặc định an toàn nếu cấu hình không tồn tại.

## Điều hướng

Thanh bên dùng các mục:

```text
Tổng quan
Đặt lệnh
Bot giao dịch
Lịch sử
Lệnh chờ
Cài đặt
```

Điều hướng dùng `CardLayout` hoặc lớp `ScreenManager` bao bọc `CardLayout`. Chuyển màn hình không tạo thêm `JFrame`, không khởi tạo lại `TradingFacade` và không làm mất trạng thái bot.

## Điều phối dữ liệu

Tạo `MarketSessionController` làm đầu mối cho timer và cập nhật phiên giao dịch:

```text
Timer
 └── nhận quote
     ├── facade.updateQuote()
     ├── facade.matchPending()
     ├── botController.onQuote()
     ├── cập nhật Dashboard
     ├── cập nhật BotScreen
     └── cập nhật PendingOrdersScreen
```

Controller phải chạy cập nhật giao diện trên EDT. Các panel không gọi trực tiếp `adapter.refresh()` hoặc tự tạo timer riêng. Lỗi validation của một lệnh không được dừng timer hoặc ngăn các panel khác cập nhật.

## Theme và màu sắc

`ThemeManager` sẽ mở rộng thành hệ màu có ngữ nghĩa, được đọc từ `theme_config.json`:

- nền chính: xanh đen/xám đậm;
- panel: xám xanh tương phản với nền;
- màu nhấn: xanh dương;
- trạng thái mua/tăng: xanh lá;
- trạng thái bán/giảm: đỏ;
- lệnh chờ/cảnh báo: vàng;
- lỗi: đỏ đậm;
- chữ chính và chữ phụ có độ tương phản rõ.

Panel không hard-code màu trạng thái. Theme được áp dụng đệ quy cho các component và có fallback khi file cấu hình thiếu hoặc sai.

## Tương thích và xử lý lỗi

- Giữ nguyên tên class miền, enum, trường JSON và API của `TradingFacade`.
- Chỉ chuyển đổi giá trị enum sang nhãn tiếng Việt ở lớp hiển thị.
- Không để lỗi nhập liệu làm đóng cửa sổ hoặc dừng timer.
- Khi chuyển màn hình, dữ liệu được lấy từ controller/facade hiện tại, không tạo bản sao trạng thái.

## Kiểm thử nghiệm thu

Bổ sung kiểm thử cho:

- chuyển đổi giữa sáu màn hình;
- không tạo thêm `JFrame` khi điều hướng;
- Dashboard nhận quote mới;
- lệnh chờ được cập nhật sau `matchPending()`;
- bot vẫn giữ trạng thái khi chuyển màn hình;
- theme áp dụng được cho màn hình mới và có fallback;
- các test nghiệp vụ hiện tại tiếp tục pass.

Tiêu chí nghiệm thu: build offline thành công, toàn bộ test pass, giao diện có thể chạy bằng `--ui`, và timer chỉ được quản lý bởi một controller phiên giao dịch.
