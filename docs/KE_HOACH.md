# Bảng kế hoạch triển khai

Kế hoạch làm việc nhóm **4 thành viên** trong **4 tuần**, phân công theo **mẫu thiết kế**.
Ký hiệu trạng thái: ✅ hoàn thành · 🔄 đang làm · ⬜ chưa bắt đầu.

## 1. Mục tiêu & phạm vi

- Xây dựng trình mô phỏng giao dịch chạy offline (Java 17 + Maven), có thể biên dịch, kiểm thử
  và khởi động từ dòng lệnh.
- Vận dụng đúng **8 mẫu thiết kế**: Singleton, Factory Method, Facade, Memento, Adapter,
  Observer, Strategy, Chain of Responsibility.
- Mỗi thành viên phụ trách 2 mẫu, kèm kiểm thử; cả nhóm cùng ráp giao diện và nghiệm thu.

Quy chuẩn chỉ báo: toàn bộ Technical Indicators, bao gồm RSI, dùng `double` trong tính toán để
tối ưu hiệu năng dữ liệu lớn; khi hiển thị lên giao diện, giá trị được làm tròn 2 hoặc 4 chữ số.
Điều kiện kích hoạt tín hiệu dùng `<`/`>`, không dùng `==` cho số `double`.

## 2. Bảng phân công theo mẫu thiết kế

| Thành viên | Mẫu 1 | Mẫu 2 |
|---|---|---|
| Thành viên 1 | Singleton | Factory Method |
| Thành viên 2 | Facade | Memento |
| Thành viên 3 | Adapter | Observer |
| Thành viên 4 | Strategy | Chain of Responsibility |

## 3. Bảng kế hoạch theo tuần

| STT | Tuần | Công việc | Mẫu thiết kế | Thành viên | Sản phẩm bàn giao | Trạng thái |
|---|---|---|---|---|---|---|
| 1 | 1 | Dựng khung Maven/Java 17 và mô hình miền | — | Cả nhóm | `pom.xml`, `model/*` | ✅ |
| 2 | 1 | Engine một thực thể duy nhất | Singleton | TV1 | `core/ExchangeEngine` | ✅ |
| 3 | 1 | Tạo order Market/Limit/Stop-loss | Factory Method | TV1 | `tradingengine/OrderFactory` | ✅ |
| 4 | 1 | Sổ lệnh chờ khớp | Factory Method | TV1 | `tradingengine/OrderBook` | ✅ |
| 5 | 2 | Chuẩn hóa dữ liệu giá (mock/random/CSV) | Adapter | TV3 | `marketdata/adapters/*` | ✅ |
| 6 | 2 | API đơn giản cho UI/bot | Facade | TV2 | `core/TradingFacade` | ✅ |
| 7 | 2 | Lưu/khôi phục tài khoản (JSON) | Memento | TV2 | `PortfolioMemento`, `persistence/*` | ✅ |
| 8 | 2 | Báo cáo lãi/lỗ | Memento (hỗ trợ) | TV2 | `reporting/PerformanceReport` | ✅ |
| 9 | 3 | Phát/thu thông báo giá | Observer | TV3 | `marketdata/observers/*`, `PriceBoardPanel` | ✅ |
| 10 | 3 | Chuỗi kiểm tra hợp lệ | Chain of Responsibility | TV4 | `ValidationChain`, `IValidator` + validator | ✅ |
| 11 | 3 | Kiểm thử các mẫu | — | Cả nhóm | `TestPatterns`, `TestValidation` | ✅ |
| 12 | 4 | Chiến lược bot (MA/RSI/MACD/Bollinger) | Strategy | TV4 | `tradingengine/botstrategies/*` | ✅ |
| 13 | 4 | Ráp giao diện Swing | — | Cả nhóm | `ui/*`, `ui/components/*` | ✅ |
| 14 | 4 | Kiểm thử tích hợp & nghiệm thu | — | Cả nhóm | `TestIntegration`, `TestTheme`, `TestUi` | ✅ |

## 4. Checklist tiến độ

- [x] Khung dự án và mô hình miền (`Quote`, `Order`, `Trade`, `Position`, enum).
- [x] Singleton: `ExchangeEngine`.
- [x] Factory Method: `OrderFactory`, `OrderBook`.
- [x] Adapter: `IMarketDataProvider`, `MarketAdapter`, Mock/RandomWalk/CSV provider.
- [x] Facade: `TradingFacade`.
- [x] Memento: `PortfolioMemento` + lưu trữ JSON + báo cáo.
- [x] Observer: `IPriceObserver`, `PriceObserver`.
- [x] Chain of Responsibility: `ValidationChain` + các validator.
- [x] Strategy: MA/RSI/MACD/Bollinger.
- [x] Giao diện Swing: bảng giá, form lệnh, bot, danh mục, lịch sử lệnh, theme.
- [x] Kiểm thử: 49 test pass.

## 5. Mốc kiểm thử & nghiệm thu

```bash
mvn -o -Dmaven.repo.local=.m2 test        # 49 test, 0 lỗi
mvn -o -Dmaven.repo.local=.m2 exec:java    # khởi động chế độ console
```

- Tiêu chí: build xanh, không cần mạng, mọi thao tác tiền/giá dùng `BigDecimal`.
- Lỗi nhập liệu ném `IllegalArgumentException` với thông báo rõ ràng.
- Mỗi thành viên chứng minh phần mình bằng kiểm thử tương ứng trong `src/test`.
