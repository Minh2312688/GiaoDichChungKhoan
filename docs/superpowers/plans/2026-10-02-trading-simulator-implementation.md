# Kế hoạch triển khai Trading Simulator MVP

> **Dành cho người thực thi:** dùng kỹ năng `superpowers:subagent-driven-development` (khuyến
> nghị) hoặc `superpowers:executing-plans` để triển khai kế hoạch này theo từng nhiệm vụ. Đánh
> dấu tiến độ bằng cú pháp checkbox (`- [ ]`).

**Mục tiêu:** Xây dựng MVP Java Maven mô phỏng giao dịch offline, có core engine, market-data
adapter/observer, validation, factory/strategy, portfolio memento, Swing UI và JUnit 5.

**Kiến trúc:** Mô hình miền dùng `BigDecimal`; `TradingFacade` điều phối toàn bộ lệnh; các mẫu
thiết kế được tách theo package. Provider dữ liệu thị trường mô phỏng deterministic giúp kiểm
thử tích hợp và chạy demo không cần mạng.

**Công nghệ:** Java 17, Maven, Swing, Jackson, JUnit 5.

**Đặc tả:** `docs/superpowers/specs/2026-10-02-trading-simulator-design.md`

## Ràng buộc chung

- Java source/target là 17.
- Mọi thao tác tiền và giá dùng `BigDecimal`.
- UI/bot chỉ gọi `TradingFacade`, không truy cập trực tiếp engine trong luồng xử lý.
- Build/test phải chạy offline sau khi phụ thuộc đã có trong cache Maven.
- Kiểm tra hợp lệ thất bại bằng `IllegalArgumentException` có thông báo rõ ràng.

---

### Nhiệm vụ 1: Khung Maven và domain/core engine

**Tệp:** `pom.xml`, `README.md`, `core/ExchangeEngine.java`, `core/TradingFacade.java`, các model
domain trong `src/main/java/com/tradingsimulator/model/`.

- [x] Tạo pom Java 17, Jackson, JUnit 5, plugin compiler/surefire/exec.
- [x] Tạo bất biến `Quote`, `Order`, enum `OrderSide`, `OrderType`, `Trade`, `Position`.
- [x] Viết test cho market order, engine Singleton và facade.
- [x] Cài engine khớp market order theo quote và cập nhật tiền mặt/vị thế.
- [x] Chạy `mvn -q -DskipTests compile`.

### Nhiệm vụ 2: Market data adapter, observer và chuỗi kiểm tra hợp lệ

**Tệp:** `marketdata/adapters/*`, `marketdata/observers/*`, `validationandportfolio/ValidationChain.java`.

- [x] Viết test provider deterministic, chuẩn hóa adapter, thông báo observer.
- [x] Cài giao diện provider, mock provider, adapter và sổ đăng ký observer.
- [x] Cài kiểm tra số dư, giới hạn khối lượng/vị thế và khoảng giá.
- [x] Nối kiểm tra vào facade trước khi engine nhận lệnh.
- [x] Chạy test các package liên quan.

### Nhiệm vụ 3: Nhà máy lệnh và chiến lược bot

**Tệp:** `tradingengine/OrderFactory.java`, `tradingengine/botstrategies/*`.

- [x] Viết test tạo đủ Market/Limit/Stop-loss và tín hiệu MA/RSI.
- [x] Cài factory kèm kiểm tra đầu vào.
- [x] Cài giao diện strategy, lớp cơ sở, MA crossover và ngưỡng RSI.
- [x] Cho facade nhận tín hiệu strategy nhưng không tự đặt lệnh nếu bot tắt.
- [x] Chạy toàn bộ test miền nghiệp vụ.

### Nhiệm vụ 4: Memento danh mục và giao diện/theme

**Tệp:** `PortfolioMemento.java`, `ui/ThemeManager.java`, `ui/components/*`, `ui/MainWindow.java`,
`Main.java`, `config/theme_config.json`, `assets/`.

- [x] Viết test snapshot/restore và nạp theme JSON.
- [x] Cài snapshot bất biến và khôi phục qua engine/facade.
- [x] Cài các panel Swing: bảng giá, form đặt lệnh, panel bot, tổng kết danh mục.
- [x] Cài cửa sổ chính và theme manager có giá trị mặc định khi thiếu cấu hình.
- [x] Bổ sung README hướng dẫn chạy.

### Nhiệm vụ 5: Kiểm thử tích hợp, tài liệu và nghiệm thu

**Tệp:** `src/test/java/com/tradingsimulator/TestPatterns.java`, `TestIntegration.java`,
`TestTheme.java`, `docs/PROJECT_SPEC.md`, `docs/PROGRESS_TRACKER.md`, `docs/UI_CUSTOMIZATION_GUIDE.md`.

- [x] Viết test tích hợp quote → kiểm tra → factory → thực thi → danh mục.
- [x] Chạy `mvn compile`.
- [x] Chạy `mvn test` và sửa lỗi đến khi pass.
- [x] Chạy `mvn exec:java` với thời gian chờ ngắn để xác nhận điểm khởi động.
- [x] Đánh dấu checklist tiến độ và ghi lại lệnh kiểm chứng.

## Tự đánh giá

- Độ phủ đặc tả: core, 8 mẫu, giao diện, theme, provider offline, test và luồng xử lý đều có nhiệm vụ.
- Quét placeholder: không có TODO/TBD hay phụ thuộc vào nhiệm vụ chưa định nghĩa.
- Nhất quán kiểu: mọi nhiệm vụ dùng model domain từ Nhiệm vụ 1 và facade là ranh giới duy nhất.
