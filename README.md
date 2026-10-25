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
mvn -o -Dmaven.repo.local=.m2 exec:java -Dexec.args=--ui  # mở giao diện Swing đa màn hình
```

Giao diện Swing hỗ trợ điều hướng nhiều màn hình nội bộ (`CardLayout` & `ScreenManager`) với thanh bên (`SidebarNavigation`) và điều phối phiên giao dịch tập trung (`MarketSessionController`):
- **Tổng quan (Dashboard):** Bảng giá trực tiếp, tiền mặt, bảng danh mục với số lượng và giá vốn trung bình của từng mã, số lệnh chờ và trạng thái bot. Nhấn đúp vào một mã trên bảng giá hoặc danh mục để mở form mua/bán nhanh cho mã đó.
- **Đặt lệnh (Trading):** Form đặt lệnh với validation chuỗi trách nhiệm.
- **Bot giao dịch (Bot):** Cấu hình chiến lược (MA, RSI, MACD, Bollinger Bands), bật/tắt và hiển thị RSI.
- **Lịch sử (History):** Xem các giao dịch đã khớp.
- **Lệnh chờ (Pending Orders):** Danh sách lệnh LIMIT / STOP-LOSS đang chờ khớp.
- **Cài đặt (Settings):** Tùy chỉnh theme giao diện và chu kỳ phiên giao dịch.

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
| Phan Thành Huy | **Singleton**, **Factory Method** | `core/ExchangeEngine`, `tradingengine/OrderFactory`, `tradingengine/OrderBook` |
| Dương Văn Minh | **Facade**, **Memento** | `core/TradingFacade`, `validationandportfolio/PortfolioMemento`, `persistence/*`, `reporting/*` |
| Lê Thị Ánh Nhung | **Adapter**, **Observer** | `marketdata/adapters/*`, `marketdata/observers/*` |
| Lý Ngọc Thảo Nguyên | **Strategy**, **Chain of Responsibility** | `tradingengine/botstrategies/*`, `validationandportfolio/ValidationChain` + các validator |

Chi tiết xem `docs/TEAM_WORK_DIVISION.md` (phân công) và `docs/KE_HOACH.md` (bảng kế hoạch).
Quy ước làm việc xem `CONTRIBUTING.md`.
