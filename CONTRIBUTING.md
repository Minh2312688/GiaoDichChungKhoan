# Quy ước đóng góp (Contributing)

## Yêu cầu môi trường

- JDK 17 (đã cấu hình `maven.compiler.release=17`).
- Maven (hoặc dùng Maven tích hợp trong tiện ích VS Code Java).
- Toàn bộ phụ thuộc đã có trong cache `./.m2` nên **build chạy offline**.

## Lệnh build & kiểm thử

```bash
# Biên dịch và chạy toàn bộ kiểm thử (offline, dùng kho cục bộ của dự án)
mvn -o -Dmaven.repo.local=.m2 test

# Chỉ biên dịch
mvn -o -Dmaven.repo.local=.m2 compile

# Chạy ứng dụng offline (chế độ console)
mvn -o -Dmaven.repo.local=.m2 exec:java

# Mở giao diện Swing
mvn -o -Dmaven.repo.local=.m2 exec:java -Dexec.args=--ui
```

> **Lưu ý:** khi offline, không dùng `clean` (plugin `maven-clean-plugin` cần tải phụ thuộc
> chưa có trong cache). Trình biên dịch Maven vẫn tự dịch lại các tệp đã thay đổi.

Trên Windows có thể dùng script `scripts/run-tests.ps1`.

## Quy ước viết mã

- Tiền và giá **luôn dùng `BigDecimal`**; làm tròn bằng `RoundingMode.HALF_UP`.
- Đối tượng miền (domain) bất biến (`record`) khi có thể.
- UI/bot chỉ gọi `TradingFacade`; không truy cập `ExchangeEngine` trực tiếp.
- Lỗi nhập liệu/vi phạm nghiệp vụ ném `IllegalArgumentException` với thông báo rõ ràng.
- Mỗi lớp có JavaDoc ngắn ghi rõ **chủ sở hữu** và **mẫu thiết kế**, ví dụ:
  `Chủ sở hữu: Thành viên 3 (mẫu Adapter)`.

## Quy trình Git

1. Mỗi thành viên tạo nhánh riêng: `feature/tvX-<mô-tả>` (ví dụ `feature/tv4-macd-strategy`).
2. Commit nhỏ, thông điệp rõ ràng: `tv4: thêm chiến lược MACD và kiểm thử`.
3. Chạy `scripts/run-tests.ps1` (hoặc lệnh `mvn` ở trên) trước khi push.
4. Mở pull request vào `main`; chỉ sửa tệp **thuộc mẫu thiết kế của mình**.
5. Các tệp lõi dùng chung (`TradingFacade`, `ExchangeEngine`, `PortfolioMemento`, `pom.xml`)
   do trưởng nhóm cập nhật. Xem `docs/TEAM_WORK_DIVISION.md`.

## Cấu trúc package

```
com.tradingsimulator
├── core                # ExchangeEngine (Singleton), TradingFacade (Facade)
├── model               # Quote, Order, Trade, Position, các enum
├── marketdata
│   ├── adapters        # IMarketDataProvider + các provider (Adapter)
│   └── observers       # IPriceObserver, PriceObserver (Observer)
├── tradingengine       # OrderFactory (Factory Method), OrderBook
│   └── botstrategies   # IBotStrategy, lớp cơ sở + các chiến lược (Strategy)
├── validationandportfolio  # ValidationChain + IValidator (Chain of Responsibility),
│                           # PortfolioMemento (Memento)
├── persistence         # PortfolioRepository (lưu JSON bằng Jackson)
├── reporting           # PerformanceReport (báo cáo lãi/lỗ)
└── ui                  # ThemeManager, MainWindow
    └── components      # PriceBoardPanel, OrderFormPanel, BotPanel,
                        # PortfolioPanel, OrderHistoryPanel
```
