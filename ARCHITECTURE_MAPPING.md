# SYSTEM LOGIC & INTEGRATION AUDIT

> **Mục đích:** tài liệu tham chiếu kiến trúc (Context Baseline) cho các thay đổi tiếp theo.
> Nội dung được đối chiếu với mã nguồn trong `src/main` và cấu hình dự án tại thời điểm kiểm
> tra; khi tài liệu khác hành vi mã nguồn, mã nguồn hiện tại là nguồn sự thật. Đây là ứng dụng
> mô phỏng giao dịch offline, không kết nối sàn giao dịch thật.

## 1. Tổng quan dự án

Giao dịch chứng khoán là ứng dụng desktop Java 17 / Maven minh họa giao dịch cổ phiếu và tám
mẫu thiết kế: Singleton, Facade, Adapter, Observer, Factory Method, Strategy, Chain of
Responsibility và Memento. Lõi nghiệp vụ giữ tiền, vị thế, quote, giao dịch và lệnh chờ trong
bộ nhớ; dữ liệu thị trường được tạo offline; giao diện người dùng là Java Swing.

**Không tìm thấy frontend Next.js, ứng dụng web, REST API, máy chủ backend hay cơ sở dữ liệu**
trong repository. Vì vậy, "Frontend" trong tài liệu này là giao diện Swing đang có, không phải
Next.js. `package.json`/Next.js không nằm trong cấu trúc dự án.

### Công nghệ và phụ thuộc

| Thành phần | Hiện trạng |
|---|---|
| Ngôn ngữ/runtime | Java 17 (`maven.compiler.release=17`) |
| Build | Maven; artifact `com.tradingsimulator:trading-simulator:1.0-SNAPSHOT` |
| UI | Swing + FlatLaf Dark, `CardLayout`, chạy trong cùng tiến trình với lõi nghiệp vụ |
| Look & Feel | FlatLaf 3.4.1; arc control và Segoe UI cấu hình ở `ThemeManager` |
| JSON | Jackson Databind 2.17.2 |
| Test | JUnit Jupiter 5.10.2, Surefire 3.2.5 |
| Nguồn giá | Mock, random walk có seed, hoặc CSV; không cần mạng |
| Trạng thái | Bộ nhớ tiến trình; JSON snapshot chỉ được lưu khi caller gọi repository |

## 2. Sơ đồ thành phần

```text
Main
 └─ MainWindow (Swing)
     ├─ TradingFacade ─────────────── ExchangeEngine (Singleton, state trong RAM)
     │    ├─ ValidationChain
     │    ├─ OrderBook (LIMIT / STOP_LOSS đang chờ)
     │    ├─ MarketAdapter ────────── IMarketDataProvider
     │    └─ IBotStrategy
     ├─ MarketSessionController
     │    ├─ Swing Timer → MarketAdapter.refresh(symbol)
     │    ├─ PriceObserver → xử lý quote → facade.matchPending
     │    ├─ BotController → Strategy → facade.submitMarketOrder
     │    └─ MarketSessionListener → cập nhật các màn hình
     ├─ ScreenManager (CardLayout) + SidebarNavigation
     └─ 6 màn hình Swing / các component trình bày

 TradingFacade.snapshot / restore
     └─ PortfolioMemento → PortfolioRepository → JSON / PortfolioState DTO

 TradingFacade data → PerformanceReport (báo cáo snapshot, không ghi vào engine)
```

Đây là kiến trúc desktop đơn tiến trình, không có ranh giới frontend/backend qua HTTP. UI và bot
gọi nghiệp vụ qua `TradingFacade`; `ExchangeEngine` được facade sử dụng để quản lý trạng thái.

## 3. Luồng khởi động và Backend (lõi nghiệp vụ)

### 3.1 Điểm vào ứng dụng

1. `com.tradingsimulator.Main.main` in thông báo phiên bản offline.
2. Reset Singleton `ExchangeEngine` với số dư ban đầu `10,000`; reset xóa vị thế, giao dịch và
   quote hiện tại.
3. Nếu môi trường không headless, chạy `new MainWindow().setVisible(true)` trên Swing EDT.
4. `MainWindow` tạo facade nếu không được truyền vào, nạp các mã AAPL/BTC/ETH với giá khởi tạo
   lần lượt `150.00`, `45000.00`, `3000.00`, và tạo `RandomWalkMarketDataProvider` seed `42`
   (spread mặc định `0.05`).
5. Gắn adapter vào facade, tạo `BotController`, `MarketSessionController`, sáu màn hình và
   đăng ký listener. Timer cập nhật bắt đầu với chu kỳ 2,000 ms.

### 3.2 Luồng đặt lệnh đồng bộ

**Lệnh MARKET:**

1. UI tạo `Order` (qua `OrderFactory` trong form) rồi gọi `TradingFacade.submit(order)`; bot gọi
   `submitMarketOrder(symbol, side, quantity)`.
2. Facade yêu cầu quote đã được nạp vào engine; nếu không có quote thì ném
   `IllegalArgumentException`.
3. `ValidationChain.defaultChain()` lần lượt chạy `BalanceValidator`, `PositionValidator`,
   `QuantityLimitValidator` (tối đa 1,000,000) và `PriceRangeValidator`.
4. `ExchangeEngine.execute` chọn ask cho BUY, bid cho SELL; tính tổng `price * quantity`, làm
   tròn 2 chữ số với `HALF_UP`, cập nhật tiền và vị thế, ghi `Trade` rồi trả trade cho caller.
5. UI làm mới các bảng hoặc BotController cập nhật trạng thái bot.

Vị thế mua mới được tính giá vốn trung bình theo tổng chi phí đã làm tròn; bán giảm số lượng
nhưng giữ giá vốn trung bình, và khi hết vị thế thì average price về 0. Không có phí, thuế,
short selling, hay mô hình khớp một phần.

**Lệnh LIMIT / STOP_LOSS:**

1. Trong UI, loại lệnh không phải MARKET được đưa vào `TradingFacade.submitPending`, không
   được kiểm tra số dư/vị thế tại thời điểm đưa vào hàng chờ.
2. `OrderBook` lưu lệnh theo symbol; lệnh MARKET bị từ chối nếu thêm vào hàng chờ.
3. Với quote mới, `OrderBook.triggered` chọn lệnh đã đạt điều kiện: LIMIT BUY khi ask <= giá
   đặt, LIMIT SELL khi bid >= giá đặt; STOP_LOSS SELL khi bid <= ngưỡng, STOP_LOSS BUY khi
   ask >= ngưỡng.
4. Facade lần lượt validate rồi thực thi từng lệnh đã kích hoạt. Lệnh khớp thành công bị gỡ
   khỏi sổ. Nếu validation/execution ném `IllegalArgumentException`, lệnh ở lại sổ và vòng lặp
   tiếp tục với lệnh khác. Lệnh được khớp theo giá bid/ask hiện tại, không phải giá đặt.

### 3.3 Luồng quote và phiên thị trường

1. `MarketSessionController` dùng `javax.swing.Timer`; mỗi tick gọi `refreshOnce()`, lần lượt
   gọi `MarketAdapter.refresh(symbol)` cho từng symbol.
2. Adapter lấy quote từ provider rồi phát quote đến observer đã đăng ký. MainWindow đăng ký
   `PriceObserver(this::handleQuote)` khi tạo controller.
3. `handleQuote` thực thi trên EDT (hoặc dùng `invokeAndWait` nếu được gọi ngoài EDT): cập nhật
   quote vào facade/engine, thử khớp lệnh chờ cho symbol đó, gọi bot nếu có, rồi phát quote cho
   các listener giao diện.
4. MainWindow cập nhật bảng giá, tiền, vị thế, số lệnh chờ, trạng thái bot, RSI, lịch sử và
   status bar. `refreshOnce()` duyệt theo symbol nhưng từng quote được xử lý riêng.

Provider:

- `MockMarketDataProvider`: giá cố định từ map, spread 0.05.
- `RandomWalkMarketDataProvider`: biến động có seed; bước mặc định 1%, spread 0.05, giá làm
  tròn hai chữ số, giữ giá cũ nếu tính ra giá không dương.
- `CsvMarketDataProvider`: đọc `symbol,price` từ `InputStream`, bỏ dòng trống/comment/header,
  spread cấu hình; lỗi dữ liệu hoặc I/O được báo qua exception.
- Tệp `src/main/resources/market_data.csv` tồn tại nhưng **MainWindow hiện không dùng CSV**;
  UI mặc định dùng random walk.

### 3.4 Bot và chiến lược

`BotController.onQuote` lưu `quote.last()` riêng theo symbol, giữ tối đa 100 giá theo cấu hình
mặc định, và chỉ sinh tín hiệu khi bot được bật. Tín hiệu có thể là BUY/SELL hoặc null. Khi có
tín hiệu, bot gửi lệnh MARKET số lượng 1 qua facade; lỗi validation cập nhật trạng thái ERROR
rồi được ném tiếp. Status gồm DISABLED, WAITING_SIGNAL, ORDER_SUBMITTED và ERROR.

`BotPanel` chọn chiến lược và khi bật bot sẽ cài chiến lược vào facade. Có bốn chiến lược:

| Strategy | Logic tín hiệu |
|---|---|
| MovingAverageCrossoverStrategy | So sánh MA của hai cửa sổ liên tiếp; tăng BUY, giảm SELL |
| RsiThresholdStrategy | RSI Wilder; dưới ngưỡng thấp BUY, trên ngưỡng cao SELL |
| MacdStrategy | EMA ngắn - EMA dài; đường MACD đang tăng BUY, giảm SELL |
| BollingerBandsStrategy | Giá dưới dải dưới BUY, trên dải trên SELL |

Indicator dùng `double`; giá miền giao dịch dùng `BigDecimal`. RSI trên màn hình Bot được tính
riêng bởi `BotScreen`; formatter hiển thị hai chữ số thập phân. Không có cơ chế chống tín hiệu
lặp/giới hạn tần suất: tín hiệu vẫn được đánh giá trên từng quote.

### 3.5 Trạng thái, snapshot và báo cáo

- `ExchangeEngine` Singleton là chủ sở hữu runtime state: cash, positions, trades, quotes.
  Getter collection trả bản sao không sửa được; thao tác state đồng bộ bằng `synchronized`.
- `TradingFacade.snapshot()` tạo `PortfolioMemento` chứa cash, positions, trades, quotes và
  pending orders. `restore()` khôi phục engine và nạp lại pending orders.
- `PortfolioRepository` chuyển memento sang DTO JSON `PortfolioState`; DTO trên đĩa gồm cash,
  positions, trades và pending orders, **không chứa quotes**. Caller phải cung cấp map quotes
  khi dựng lại memento; trade timestamp được tạo mới khi khôi phục từ DTO.
- Repository chỉ cung cấp API save/load; ứng dụng khởi động không tự động đọc hoặc lưu file.
- `PerformanceReport.from(facade)` tạo báo cáo không lưu trạng thái. `netRealized` hiện được
  tính là tổng giá trị bán trừ tổng giá trị mua của toàn bộ lịch sử (không phải P&L đã hiện
  thực theo từng lot); market value dùng midpoint quote gần nhất, hoặc giá vốn khi thiếu quote;
  equity = cash + market value.

## 4. Frontend hiện tại: Java Swing

Frontend chạy trong cùng JVM và gọi facade trực tiếp, không có API mạng.

### Khung cửa sổ và điều hướng

`MainWindow` lắp header, sidebar, vùng nội dung, status bar; `ScreenManager` đăng ký sáu màn
hình trong `CardLayout`; `SidebarNavigation` chuyển màn hình mà không tạo JFrame con.

| ScreenId | Thành phần | Chức năng |
|---|---|---|
| DASHBOARD | `DashboardPanel` | Quote bid/ask, cash, vị thế, số lệnh chờ, trạng thái bot |
| TRADING | `TradingPanel` + `OrderFormPanel` | Tạo MARKET/LIMIT/STOP_LOSS và gửi/đưa vào hàng chờ |
| BOT | `BotScreen` + `BotPanel` | Chọn Strategy, bật/tắt bot, trạng thái và RSI |
| HISTORY | `HistoryScreen` + `OrderHistoryPanel` | Danh sách trade đã khớp |
| PENDING_ORDERS | `PendingOrdersScreen` | Danh sách LIMIT/STOP_LOSS đang chờ |
| SETTINGS | `SettingsPanel` | Theme mode và chu kỳ cập nhật nhập bởi người dùng |

Dashboard cho phép nhấn đúp vào mã trên bảng giá hoặc danh mục để mở form giao dịch nhanh.
Form kiểm tra cú pháp đầu vào, tạo order và hiển thị `IllegalArgumentException` dưới dạng
trạng thái form; facade không được gọi trực tiếp từ component form mà do MainWindow điều phối.

### Cập nhật UI

Session controller xử lý quote và gọi listener trên EDT; `MainWindow.onSessionQuote` đẩy dữ liệu
đến các màn hình. `refreshAllScreens()` làm mới tiền, vị thế, pending orders và lịch sử sau khi
gửi lệnh, nhưng quote mới được bổ sung vào bảng giá qua callback quote.

Theme được nạp bởi `ThemeManager` từ classpath resource `theme_config.json`; FlatLaf Dark được
thiết lập trước khi tạo component, với arc cho button/input và font Segoe UI. Palette gồm
background/foreground/accent, panel, positive/negative/warning, secondary text và border. Nếu
thiếu file hoặc lỗi parse, manager dùng palette mặc định. Tài liệu/config cũng có
`config/theme_config.json`; runtime hiện gọi tên resource `theme_config.json`, nên file
classpath trong `src/main/resources` là file được dùng.

Các bảng dùng row height 36, renderer xen kẽ màu hàng; bảng giá tính phần trăm thay đổi từ
midpoint bid/ask liền trước và nháy màu 500 ms khi bid/ask đổi. Màn giao dịch có sparkline tại
UI layer; màn bot có gauge RSI và hiển thị tín hiệu MA/MACD; lịch sử và lệnh chờ có bộ lọc nhanh.
Các thành phần này chỉ trình bày dữ liệu đã có, không thay đổi logic khớp lệnh hay Strategy.

## 5. Bản đồ mã nguồn

### Production

| Package / file | Trách nhiệm |
|---|---|
| `Main.java` | Điểm vào, reset engine, mở Swing nếu môi trường có đồ họa |
| `core/ExchangeEngine.java` | Singleton, thực thi giao dịch, cập nhật danh mục và quotes |
| `core/TradingFacade.java` | API nghiệp vụ cho UI/bot; validation, order book, snapshot/strategy |
| `model/Order.java`, `OrderSide.java`, `OrderType.java` | Lệnh bất biến và enum chiều/loại |
| `model/Quote.java`, `Trade.java`, `Position.java` | Mô hình dữ liệu bất biến |
| `tradingengine/OrderFactory.java` | Tạo market/limit/stop-loss order |
| `tradingengine/OrderBook.java` | Lưu lệnh chờ và đánh giá điều kiện trigger |
| `tradingengine/botstrategies/IBotStrategy.java`, `BaseStrategy.java` | Hợp đồng và lớp cơ sở Strategy |
| `tradingengine/botstrategies/MovingAverageCrossoverStrategy.java` | Chiến lược MA crossover |
| `tradingengine/botstrategies/RsiThresholdStrategy.java` | Tính RSI và sinh tín hiệu |
| `tradingengine/botstrategies/MacdStrategy.java` | Chiến lược MACD |
| `tradingengine/botstrategies/BollingerBandsStrategy.java` | Chiến lược Bollinger Bands |
| `validationandportfolio/IValidator.java`, `ValidationChain.java` | Hợp đồng validator và chuỗi kiểm tra |
| `validationandportfolio/BalanceValidator.java` | Kiểm tra cash với lệnh BUY |
| `validationandportfolio/PositionValidator.java` | Kiểm tra số lượng đang nắm giữ với lệnh SELL |
| `validationandportfolio/QuantityLimitValidator.java` | Giới hạn số lượng lệnh |
| `validationandportfolio/PriceRangeValidator.java` | Kiểm tra giá tham chiếu dương |
| `validationandportfolio/PortfolioMemento.java` | Snapshot bất biến của phiên |
| `marketdata/adapters/IMarketDataProvider.java` | Hợp đồng nguồn quote |
| `marketdata/adapters/MarketAdapter.java` | Lấy quote và phát observer |
| `marketdata/adapters/MockMarketDataProvider.java` | Provider giá cố định |
| `marketdata/adapters/RandomWalkMarketDataProvider.java` | Provider random walk deterministic theo seed |
| `marketdata/adapters/CsvMarketDataProvider.java` | Provider đọc CSV từ InputStream |
| `marketdata/observers/IPriceObserver.java`, `PriceObserver.java` | Hợp đồng và observer chuyển tiếp quote |
| `ui/MainWindow.java` | Composition root giao diện và nối facade/controller/screens |
| `ui/ThemeManager.java` | Nạp, fallback và áp dụng theme |
| `ui/IndicatorDisplayFormatter.java` | Định dạng indicator |
| `ui/bot/BotController.java` | Lịch sử giá bot, gọi Strategy, gửi market order |
| `ui/components/BotPanel.java` | Điều khiển strategy/bot |
| `ui/components/OrderFormPanel.java` | Nhập lệnh và hiển thị lỗi form |
| `ui/components/OrderHistoryPanel.java` | Bảng trade |
| `ui/components/PortfolioPanel.java` | Bảng vị thế, callback mở giao dịch nhanh |
| `ui/components/PriceBoardPanel.java` | Bảng bid/ask, callback mở giao dịch nhanh |
| `ui/components/TradingTableStyle.java` | Renderer xen kẽ hàng và style dùng chung cho JTable |
| `ui/navigation/ScreenId.java`, `ScreenManager.java`, `SidebarNavigation.java` | Định danh, quản lý CardLayout, sidebar |
| `ui/screens/DashboardPanel.java` | Tổng quan và kết hợp bảng giá/danh mục |
| `ui/screens/TradingPanel.java` | Bao form giao dịch |
| `ui/screens/BotScreen.java` | Màn bot và hiển thị RSI |
| `ui/screens/HistoryScreen.java` | Màn lịch sử |
| `ui/screens/PendingOrdersScreen.java` | Màn lệnh đang chờ |
| `ui/screens/SettingsPanel.java` | Giá trị theme mode / chu kỳ nhập trên UI |
| `ui/session/MarketSessionController.java` | Timer phiên, quote pipeline, pending, bot, listener |
| `ui/session/MarketSessionListener.java` | Callback quote cho giao diện |
| `persistence/PortfolioRepository.java` | Chuyển DTO và lưu/đọc JSON |
| `persistence/PortfolioState.java`, `PositionState.java`, `TradeState.java`, `OrderState.java` | DTO JSON tách khỏi model runtime |
| `reporting/PerformanceReport.java` | Snapshot số liệu hiệu suất |

### Tài nguyên và kiểm thử

- `src/main/resources/market_data.csv`: bộ giá AAPL/BTC/ETH cho provider CSV.
- `src/main/resources/theme_config.json`: theme mặc định trên classpath.
- `config/theme_config.json`: bản cấu hình theme ở thư mục cấu hình; không phải resource mà
  `MainWindow` truyền cho `ThemeManager.load`.
- `src/test/java/com/tradingsimulator`: 16 lớp test theo các nhóm BotController, BotScreen,
  Core, Integration, MarketData, MarketSessionController, Navigation, OrderBook, OrderHistory,
  Patterns, Persistence, Screens, Strategies, Theme, UI và Validation.
- `scripts/run-tests.ps1`: chạy Maven test offline với `.m2` của dự án, ưu tiên Maven local và
  fallback Maven trong user wrapper cache.

## 6. Mẫu thiết kế và ranh giới trách nhiệm

| Mẫu | Triển khai | Ý nghĩa trong luồng |
|---|---|---|
| Singleton | `ExchangeEngine` | Một kho trạng thái engine trong JVM |
| Facade | `TradingFacade` | Cổng nghiệp vụ UI/bot sử dụng |
| Adapter | `IMarketDataProvider`, `MarketAdapter` | Đổi provider thành giao diện quote chung |
| Observer | `IPriceObserver`, `PriceObserver` | Đẩy quote từ adapter tới session |
| Factory Method | `OrderFactory` | Tập trung tạo các loại order |
| Strategy | `IBotStrategy` và bốn chiến lược | Thay thuật toán tín hiệu bot |
| Chain of Responsibility | `ValidationChain` + validators | Chạy lần lượt quy tắc lệnh |
| Memento | `PortfolioMemento`, repository | Snapshot/khôi phục trạng thái phiên |

## 7. Hành vi cần lưu ý khi phát triển tiếp

Các điểm dưới đây là đối chiếu giữa tài liệu hiện có và mã nguồn, không phải cam kết hành vi
được kiểm thử ở mọi trường hợp:

1. **`--ui` không được kiểm tra trong `Main`.** `Main` hiện mở Swing ở mọi lần chạy có graphics
   environment, bất kể tham số; khi headless chỉ in thông báo và không chạy một vòng lặp console.
   Do đó câu lệnh README mô tả chế độ console và cờ `--ui` không phản ánh đúng nhánh thực thi.
2. **Settings chưa nối với runtime.** `SettingsPanel` có thể cập nhật giá trị delay/theme mode,
   nhưng MainWindow chỉ đọc delay lúc khởi động Timer; không có callback để áp delay mới vào
   timer. Chọn theme mode cũng chưa áp theme khác; theme hiện được nạp một lần từ JSON.
3. **Một số lỗi bị bỏ qua trong pipeline phiên.** `MarketSessionController` bắt và bỏ qua
   exception khi cập nhật quote, match pending, gọi bot, notify listener, refresh provider và
   fallback EDT. `TradingFacade.matchPending` cũng giữ lệnh khi validation lỗi nhưng không
   trả thông tin lỗi cho UI. Đây là hành vi code hiện tại; cần cân nhắc trước khi sửa luồng lỗi.
4. **Snapshot JSON không phải bản sao đầy đủ của memento.** Quotes và timestamp trade không
   được lưu vào DTO, dù memento runtime có quotes.
5. **Tên `netRealized` có thể gây hiểu nhầm.** Công thức hiện là tổng SELL trừ tổng BUY của
   lịch sử, không tính giá vốn các vị thế đã bán; không nên coi đây là P&L kế toán chuẩn.
6. **Provider CSV/config CSV chưa phải lựa chọn mặc định.** UI hiện khởi tạo random walk seed
   `42`; muốn dùng CSV cần tự tạo provider/adapter và nối vào composition root.
7. Mã giao diện và controller chạy cùng tiến trình với engine; không được truy cập engine trực
   tiếp từ thành phần giao diện mới. Ưu tiên mở rộng facade và giữ các quy tắc tiền/giá dùng
   `BigDecimal`; indicator dùng `double` theo quy ước dự án.

## 8. Build, test và khởi chạy

Theo `pom.xml` và `scripts/run-tests.ps1`, lệnh test offline:

```powershell
.\scripts\run-tests.ps1
```

Kết quả kiểm chứng trong lần UI redesign này: **67 tests, 0 failures, 0 errors, 0 skipped**; Maven
build thành công ở chế độ offline.

Hoặc dùng Maven đã cài:

```powershell
mvn -o -Dmaven.repo.local=.m2 test
mvn -o -Dmaven.repo.local=.m2 compile
```

Maven exec plugin dùng `com.tradingsimulator.Main` làm main class. Với mã nguồn hiện tại, chạy
`mvn -o -Dmaven.repo.local=.m2 exec:java` sẽ mở giao diện khi máy có graphics environment; tham
số `--ui` không làm thay đổi hành vi. Không dùng `mvn clean` trong môi trường offline theo lưu ý
trong README/CONTRIBUTING do plugin clean có thể không có sẵn trong cache.

## 9. Duy trì tài liệu baseline

Khi thay đổi composition root, luồng quote/order, nguồn dữ liệu, state/persistence, Strategy,
điều hướng hoặc cài đặt UI, hãy cập nhật các phần tương ứng trong file này. Phân biệt rõ:

- **Mã chạy hiện tại**: mô tả từ source đã kiểm tra.
- **Thiết kế mong muốn**: yêu cầu hoặc tài liệu chưa được triển khai.
- **Hạn chế/điểm cần quyết định**: sai khác cần được xác nhận trước khi thay đổi hành vi.

Không ghi nhận Next.js/backend web là thành phần của dự án cho tới khi các module này thực sự
được thêm vào repository.
