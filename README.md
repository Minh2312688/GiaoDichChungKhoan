# Trình mô phỏng giao dịch chứng khoán (Trading Simulator)

Ứng dụng Java 17 / Maven chạy **offline**, mô phỏng giao dịch chứng khoán với:
`TradingFacade`, bộ chuyển đổi dữ liệu thị trường (Adapter), chuỗi kiểm tra hợp lệ
(Chain of Responsibility), nhà máy tạo lệnh (Factory Method), sổ lệnh chờ, các chiến lược
bot (Strategy), memento tài khoản, lưu trữ JSON và giao diện Swing.

Đây là bài tập nhóm môn **Mẫu thiết kế (Design Patterns)**, gồm **4 thành viên**, mỗi người
phụ trách **2 mẫu thiết kế**.

## Xây dựng & chạy (offline)

Thư viện phụ thuộc đã có sẵn trong `./.m2` nên build được không cần mạng:

```bash
mvn -o -Dmaven.repo.local=.m2 test       # biên dịch + chạy toàn bộ kiểm thử
mvn -o -Dmaven.repo.local=.m2 exec:java  # chạy ứng dụng (chế độ console)
mvn -o -Dmaven.repo.local=.m2 exec:java -Dexec.args=--ui  # mở giao diện Swing
```

> Không dùng `clean` khi offline: plugin `maven-clean-plugin` cần một phụ thuộc chưa có
> trong cache. Trình biên dịch vẫn tự dịch lại mọi tệp nguồn đã thay đổi.

Trên Windows có thể chạy `scripts/run-tests.ps1`.

Mọi giá và tiền đều dùng `BigDecimal`; dữ liệu thị trường không cần mạng.

Quy ước riêng cho bot: toàn bộ technical indicators (MA, RSI, MACD, Bollinger Bands) dùng
`double` trong phép tính để tối ưu hiệu năng dữ liệu lớn. Khi hiển thị lên giao diện, indicator
được làm tròn còn 2 hoặc 4 chữ số thập phân; điều kiện tín hiệu dùng `<` và `>`, không dùng `==`.

## Phân công theo mẫu thiết kế

| Thành viên | Mẫu thiết kế | Module chính |
|---|---|---|
| Thành viên 1 | **Singleton**, **Factory Method** | `core/ExchangeEngine`, `tradingengine/OrderFactory`, `tradingengine/OrderBook` |
| Thành viên 2 | **Facade**, **Memento** | `core/TradingFacade`, `validationandportfolio/PortfolioMemento`, `persistence/*`, `reporting/*` |
| Thành viên 3 | **Adapter**, **Observer** | `marketdata/adapters/*`, `marketdata/observers/*` |
| Thành viên 4 | **Strategy**, **Chain of Responsibility** | `tradingengine/botstrategies/*`, `validationandportfolio/ValidationChain` + các validator |

Chi tiết xem `docs/TEAM_WORK_DIVISION.md` (phân công) và `docs/KE_HOACH.md` (bảng kế hoạch).
Quy ước làm việc xem `CONTRIBUTING.md`.
