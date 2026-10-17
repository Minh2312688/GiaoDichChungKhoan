# Theo dõi tiến độ

## Giai đoạn 1 – Bản MVP (hoàn thành)

- [x] Khung Maven/Java 17 và lõi engine
- [x] Adapter, observer, chuỗi kiểm tra hợp lệ
- [x] Factory và chiến lược MA/RSI
- [x] Memento, lưu/khôi phục pending orders, quản lý giao diện (theme), các panel Swing
- [x] Kiểm thử tích hợp, xử lý pending orders và điểm khởi động offline

## Giai đoạn 2 – Module theo mẫu thiết kế (hoàn thành)

Phân công lại cho **4 thành viên**, mỗi người 2 mẫu thiết kế:

- [x] TV1 – Singleton (`ExchangeEngine`) + Factory Method (`OrderFactory`, `OrderBook`)
- [x] TV2 – Facade (`TradingFacade`) + Memento (`PortfolioMemento`, `persistence/*`, `reporting/*`)
- [x] TV3 – Adapter (`marketdata/adapters/*`) + Observer (`marketdata/observers/*`)
- [x] TV4 – Strategy (`tradingengine/botstrategies/*`) + Chain of Responsibility (`ValidationChain`, `IValidator`)

## Giai đoạn 3 – Giao diện đa màn hình nội bộ & Controller phiên (hoàn thành)

- [x] Điều hướng không tạo thêm JFrame: `ScreenId`, `ScreenManager` (`CardLayout`), `SidebarNavigation`
- [x] Điều phối dữ liệu tập trung qua EDT: `MarketSessionController` và `MarketSessionListener`
- [x] Tách màn hình độc lập: `DashboardPanel`, `TradingPanel`, `BotScreen`, `PendingOrdersScreen`, `HistoryScreen`, `SettingsPanel`
- [x] Hệ màu theme ngữ nghĩa: `panel`, `positive`, `negative`, `warning` kèm cơ chế fallback an toàn
- [x] Tích hợp hoàn chỉnh `MainWindow` và giữ cờ khởi động `--ui`

## Kiểm chứng

Lệnh: `mvn -o -Dmaven.repo.local=.m2 test` → **62 test, 0 lỗi**.

Các kiểm thử mới bao phủ:
1. Chuyển đổi màn hình qua `ScreenManager` và phản hồi callback từ `SidebarNavigation` không tạo thêm cửa sổ (`TestNavigation`).
2. Điều phối phiên giao dịch, khớp lệnh chờ tự động và phát sự kiện qua `MarketSessionController` (`TestMarketSessionController`).
3. Trình bày dữ liệu tổng quan, bảng lệnh chờ, lịch sử và form giao dịch (`TestScreens`).
4. Quản lý trạng thái bot, tách biệt với cài đặt chu kỳ cập nhật và theme (`TestBotScreen`).
5. Nạp màu ngữ nghĩa và fallback an toàn khi thiếu key (`TestTheme`).
6. Đăng ký đầy đủ 6 màn hình trên `MainWindow` (`TestUi`).

Giao diện: hỗ trợ 6 màn hình nội bộ, chạy offline mượt mà qua tham số `--ui`.
