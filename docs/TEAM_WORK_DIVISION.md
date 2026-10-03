# Phân công công việc theo mẫu thiết kế

Nhóm có **4 thành viên**. Chương trình dùng đúng **8 mẫu thiết kế**, chia đều **mỗi người 2 mẫu**
để mỗi thành viên phụ trách trọn vẹn một nhóm mẫu và các lớp liên quan.

> Quy ước chung: mọi thao tác qua `TradingFacade`; tiền/giá giao dịch dùng `BigDecimal`; technical
> indicators dùng `double`, hiển thị làm tròn 2 hoặc 4 chữ số và so sánh bằng `<`/`>`; lỗi nhập liệu
> ném `IllegalArgumentException` với thông báo rõ ràng. Xem thêm `CONTRIBUTING.md`.
> Bảng kế hoạch theo tuần nằm ở `docs/KE_HOACH.md`.

## Bảng phân công tổng quan

| Thành viên | Mẫu thiết kế | Vai trò trong mẫu | Module chính | Kiểm thử |
|---|---|---|---|---|
| **Thành viên 1** | **Singleton**, **Factory Method** | `ExchangeEngine`, `OrderFactory`, `OrderBook` | `core/ExchangeEngine`, `tradingengine/OrderFactory`, `tradingengine/OrderBook`, `model/*` | `TestCore`, `TestPatterns` (factory), `TestOrderBook` |
| **Thành viên 2** | **Facade**, **Memento** | `TradingFacade`, `PortfolioMemento` | `core/TradingFacade`, `validationandportfolio/PortfolioMemento`, `persistence/*`, `reporting/*` | `TestIntegration`, `TestPersistence` |
| **Thành viên 3** | **Adapter**, **Observer** | `IMarketDataProvider`/`MarketAdapter`, `IPriceObserver`/`PriceObserver` | `marketdata/adapters/*`, `marketdata/observers/*` | `TestMarketData`, `TestPatterns` (adapter/observer) |
| **Thành viên 4** | **Strategy**, **Chain of Responsibility** | `IBotStrategy`, `ValidationChain`/`IValidator` | `tradingengine/botstrategies/*`, `validationandportfolio/ValidationChain` + validator | `TestStrategies`, `TestValidation`, `TestPatterns` (strategy/chain) |

## Ánh xạ 8 mẫu thiết kế

| # | Mẫu | Thành phần | Trách nhiệm | Thành viên |
|---|---|---|---|---|
| 1 | Singleton | `ExchangeEngine` | Một engine giao dịch duy nhất trong tiến trình | TV1 |
| 2 | Factory Method | `OrderFactory` | Tạo Market/Limit/Stop-loss order | TV1 |
| 3 | Facade | `TradingFacade` | API đơn giản cho UI/bot | TV2 |
| 4 | Memento | `PortfolioMemento` | Lưu/khôi phục snapshot tài sản | TV2 |
| 5 | Adapter | `IMarketDataProvider`, `MarketAdapter` | Chuẩn hóa dữ liệu quote | TV3 |
| 6 | Observer | `IPriceObserver`, `PriceObserver` | Phát quote tới UI và hệ thống | TV3 |
| 7 | Strategy | `IBotStrategy`, MA/RSI/MACD/Bollinger | Sinh tín hiệu giao dịch | TV4 |
| 8 | Chain of Responsibility | `ValidationChain`, `IValidator` | Balance, position, quantity, price-range | TV4 |

## Chi tiết từng thành viên

### Thành viên 1 – Singleton & Factory Method
- **Singleton:** `ExchangeEngine` giữ sổ lệnh tối giản, tiền mặt, vị thế, lịch sử khớp lệnh và
  bảng giá; chỉ có một thực thể qua `getInstance()`.
- **Factory Method:** `OrderFactory` tạo 3 loại lệnh (`market`, `limit`, `stopLoss`).
  `OrderBook` quản lý các lệnh `LIMIT`/`STOP_LOSS` chờ khớp và quyết định thời điểm kích hoạt.
- Phần UI phụ trách: `OrderFormPanel` (form đặt lệnh dùng factory).
- Mở rộng gợi ý: thêm loại lệnh mới, ưu tiên khớp theo giá – thời gian, partial fill.

### Thành viên 2 – Facade & Memento
- **Facade:** `TradingFacade` là cổng duy nhất cho UI/bot; validate → tạo lệnh → thực thi →
  cập nhật danh mục; thêm `submitPending`, `matchPending`, `pendingOrders`, `trades`, `quotes`.
- **Memento:** `PortfolioMemento` lưu snapshot (tiền, vị thế, lịch sử, bảng giá, lệnh chờ) và
  khôi phục không làm thay đổi đối tượng bên ngoài.
- Kèm phần lưu trữ `persistence/PortfolioRepository` (JSON/Jackson) và báo cáo
  `reporting/PerformanceReport` (lãi/lỗi, giá trị thị trường, vốn chủ sở hữu).
- Phần UI phụ trách: `PortfolioPanel`.
- Mở rộng gợi ý: tự động lưu định kỳ, xuất báo cáo CSV.

### Thành viên 3 – Adapter & Observer
- **Adapter:** `IMarketDataProvider` + `MarketAdapter` chuẩn hóa dữ liệu; các provider
  `MockMarketDataProvider`, `RandomWalkMarketDataProvider` (deterministic theo seed),
  `CsvMarketDataProvider` (đọc `market_data.csv`).
- **Observer:** `IPriceObserver` + `PriceObserver` phát quote tới các bên đăng ký (UI, hệ thống).
- Phần UI phụ trách: `PriceBoardPanel`, `OrderHistoryPanel`, `MainWindow` (đăng ký observer).
- Mở rộng gợi ý: thêm provider mới, thêm kênh thông báo (log, cảnh báo), timestamp/volume.

### Thành viên 4 – Strategy & Chain of Responsibility
- **Strategy:** `IBotStrategy`, `BaseStrategy` và các chiến lược `MovingAverageCrossoverStrategy`,
  `RsiThresholdStrategy`, `MacdStrategy`, `BollingerBandsStrategy`.
- **Chain of Responsibility:** `IValidator` là một mắt xích; `ValidationChain` chạy tuần tự và
  dừng ở lỗi đầu tiên. Các rule: `BalanceValidator`, `PositionValidator`,
  `QuantityLimitValidator`, `PriceRangeValidator`.
- Phần UI phụ trách: `BotPanel` (chọn chiến lược và bật/tắt bot).
- Mở rộng gợi ý: RSI thật (Wilder), thêm validator (giờ giao dịch, giá trị lệnh tối đa).

## Quy trình làm việc

1. Mỗi thành viên tạo nhánh riêng: `feature/tvX-<mô-tả>`.
2. Chỉ sửa tệp **thuộc mẫu thiết kế của mình**. Cần thay đổi tệp lõi thì báo trưởng nhóm.
3. Viết kiểm thử cho phần mình và đảm bảo `mvn test` xanh trước khi mở pull request.
4. Mở PR vào `main`, ghi rõ mẫu thiết kế và kiểm thử đã thêm.

## Tiêu chí hoàn thành (Definition of Done)

- Mã biên dịch và kiểm thử xanh: `mvn -o -Dmaven.repo.local=.m2 test`.
- Có ít nhất 1 kiểm thử mới cho phần mình và toàn bộ kiểm thử cũ vẫn pass.
- Không còn TODO/placeholder; lỗi nhập liệu ném `IllegalArgumentException` rõ ràng.
- JavaDoc ngắn ghi rõ **chủ sở hữu** và **mẫu thiết kế**.
