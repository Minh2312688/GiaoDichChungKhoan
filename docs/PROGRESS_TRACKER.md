# Theo dõi tiến độ

## Giai đoạn 1 – Bản MVP (hoàn thành)

- [x] Khung Maven/Java 17 và lõi engine
- [x] Adapter, observer, chuỗi kiểm tra hợp lệ
- [x] Factory và chiến lược MA/RSI
- [x] Memento, quản lý giao diện (theme), các panel Swing
- [x] Kiểm thử tích hợp và điểm khởi động offline

## Giai đoạn 2 – Module theo mẫu thiết kế (hoàn thành)

Phân công lại cho **4 thành viên**, mỗi người 2 mẫu thiết kế:

- [x] TV1 – Singleton (`ExchangeEngine`) + Factory Method (`OrderFactory`, `OrderBook`)
- [x] TV2 – Facade (`TradingFacade`) + Memento (`PortfolioMemento`, `persistence/*`, `reporting/*`)
- [x] TV3 – Adapter (`marketdata/adapters/*`) + Observer (`marketdata/observers/*`)
- [x] TV4 – Strategy (`tradingengine/botstrategies/*`) + Chain of Responsibility (`ValidationChain`, `IValidator`)

## Kiểm chứng

Lệnh: `mvn -o -Dmaven.repo.local=.m2 test` → **33 test, 0 lỗi**.

Giao diện: đã áp theme, bảng giá, form đặt lệnh, điều khiển bot, tổng kết danh mục và lịch sử
lệnh. Giao diện bật qua tham số `--ui` để điểm khởi động mặc định vẫn phù hợp khi kiểm thử
không màn hình.

Xem bảng kế hoạch chi tiết ở `docs/KE_HOACH.md` và phân công ở `docs/TEAM_WORK_DIVISION.md`.
