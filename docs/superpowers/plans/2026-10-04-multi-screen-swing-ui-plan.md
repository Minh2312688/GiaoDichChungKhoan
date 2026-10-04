# Multi-Screen Swing UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chuyển giao diện Swing từ `GridLayout(2, 2)` sang một cửa sổ nhiều màn hình nội bộ bằng `CardLayout`.

**Architecture:** `MainWindow` sở hữu `ScreenManager`, `SidebarNavigation` và một `MarketSessionController`. Controller duy nhất quản lý timer, quote, `matchPending()`, `BotController` và phát sự kiện cập nhật cho các panel. Các panel chỉ hiển thị dữ liệu hoặc phát sự kiện người dùng.

**Tech Stack:** Java 17, Swing, Maven offline, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-04-swing-multi-screen-design.md`

## Global Constraints

- Giữ nguyên API của `TradingFacade`, model, enum và persistence JSON.
- Không tạo thêm `JFrame` khi điều hướng; dùng một `CardLayout`.
- Chỉ `MarketSessionController` quản lý timer và gọi `adapter.refresh()`.
- Cập nhật Swing trên EDT; lỗi validation không được dừng timer.
- Chỉ dịch `BUY/SELL` ở lớp hiển thị thành `MUA/BÁN`.
- Theme đọc từ `theme_config.json` và có fallback an toàn.
- Mỗi task phải có test đỏ, test xanh và commit riêng.

---

### Task 1: ScreenManager và SidebarNavigation

**Files:**
- Create: `src/main/java/com/tradingsimulator/ui/navigation/ScreenId.java`
- Create: `src/main/java/com/tradingsimulator/ui/navigation/ScreenManager.java`
- Create: `src/main/java/com/tradingsimulator/ui/navigation/SidebarNavigation.java`
- Test: `src/test/java/com/tradingsimulator/TestNavigation.java`

**Interfaces:** `ScreenManager.register(ScreenId, Component)`, `show(ScreenId)`, `currentScreen()`, `view()`; `SidebarNavigation(Consumer<ScreenId>)`.

- [ ] Viết test `screenManagerShowsRegisteredScreenWithoutCreatingWindows`: đăng ký `DASHBOARD` và `BOT`, gọi `show(BOT)`, assert component hiển thị là panel bot và `currentScreen()` là `BOT`.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestNavigation test`; xác nhận test đỏ vì class chưa tồn tại.
- [ ] Implement một `CardLayout`, map `ScreenId.name()` với component, và sidebar phát `ScreenId` qua callback.
- [ ] Chạy lại test; xác nhận xanh.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/navigation src/test/java/com/tradingsimulator/TestNavigation.java; git commit -m "feat: add Swing screen navigation"`.

### Task 2: MarketSessionController

**Files:**
- Create: `src/main/java/com/tradingsimulator/ui/session/MarketSessionController.java`
- Test: `src/test/java/com/tradingsimulator/TestMarketSessionController.java`
- Use: `TradingFacade`, `MarketAdapter`, `BotController`.

**Interfaces:** constructor nhận `TradingFacade`, `MarketAdapter`, `Set<String>`, `BotController`; `addListener(MarketSessionListener)`; `refreshOnce()`; `start(int)`; `stop()`.

- [ ] Viết test với adapter deterministic và một pending order; gọi `refreshOnce()`, assert vị thế thay đổi và listener nhận quote.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestMarketSessionController test`; xác nhận đỏ vì controller chưa tồn tại.
- [ ] Implement listener quote: `updateQuote` → `matchPending` → `botController.onQuote` → notify listeners; bắt lỗi từng bước để timer tiếp tục.
- [ ] Chạy lại test; xác nhận xanh và `start()` không tạo timer thứ hai khi gọi lặp.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/session src/test/java/com/tradingsimulator/TestMarketSessionController.java; git commit -m "feat: centralize market session updates"`.

### Task 3: Dashboard, Trading và Pending Orders screens

**Files:**
- Create: `src/main/java/com/tradingsimulator/ui/screens/DashboardPanel.java`
- Create: `src/main/java/com/tradingsimulator/ui/screens/TradingPanel.java`
- Create: `src/main/java/com/tradingsimulator/ui/screens/PendingOrdersScreen.java`
- Create: `src/main/java/com/tradingsimulator/ui/screens/HistoryScreen.java`
- Modify: `OrderFormPanel.java`, `OrderHistoryPanel.java`
- Test: `src/test/java/com/tradingsimulator/TestScreens.java`

**Interfaces:** `DashboardPanel.showQuotes`, `showCash`, `showPositions`, `showPendingCount`, `showBotStatus`; `PendingOrdersScreen.showOrders(List<Order>)`; `HistoryScreen.showTrades(List<Trade>)`.

- [ ] Viết test assert dashboard hiển thị `Lệnh chờ: 1` và pending screen có một dòng sau khi nạp một order.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestScreens test`; xác nhận đỏ.
- [ ] Implement các panel bằng component hiện có; bảng dùng nhãn tiếng Việt và không tự sửa facade.
- [ ] Chạy lại test; xác nhận xanh.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/screens src/main/java/com/tradingsimulator/ui/components src/test/java/com/tradingsimulator/TestScreens.java; git commit -m "feat: add trading and portfolio screens"`.

### Task 4: BotScreen và SettingsPanel

**Files:**
- Create: `src/main/java/com/tradingsimulator/ui/screens/BotScreen.java`
- Create: `src/main/java/com/tradingsimulator/ui/screens/SettingsPanel.java`
- Modify: `BotPanel.java`
- Test: `src/test/java/com/tradingsimulator/TestBotScreen.java`

**Interfaces:** `BotScreen.showQuote(Quote, List<BigDecimal>, BotController.Status)`; `SettingsPanel.themeMode()`, `setThemeMode(String)`, `refreshDelay()`, `setRefreshDelay(int)`.

- [ ] Viết test assert bot screen giữ chiến lược, trạng thái bật/tắt và RSI khi nhận quote.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestBotScreen test`; xác nhận đỏ.
- [ ] Tách phần trình bày bot khỏi controller; settings mặc định theme tối và delay 2000 ms, từ chối delay không dương.
- [ ] Chạy lại test; xác nhận xanh.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/screens src/main/java/com/tradingsimulator/ui/components/BotPanel.java src/test/java/com/tradingsimulator/TestBotScreen.java; git commit -m "feat: separate bot and settings screens"`.

### Task 5: Theme ngữ nghĩa

**Files:** `ThemeManager.java`, `config/theme_config.json`, `src/main/resources/theme_config.json`, `TestTheme.java`.

- [ ] Viết test yêu cầu `panel()`, `positive()`, `negative()` và `warning()` có màu khi file cấu hình thiếu khóa.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestTheme test`; xác nhận đỏ.
- [ ] Thêm màu panel, tích cực, tiêu cực, cảnh báo; giữ tương thích với ba màu cũ và fallback khi JSON sai.
- [ ] Chạy lại test; xác nhận xanh.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/ThemeManager.java config/theme_config.json src/main/resources/theme_config.json src/test/java/com/tradingsimulator/TestTheme.java; git commit -m "feat: add semantic Swing theme colors"`.

### Task 6: Tích hợp MainWindow

**Files:** `MainWindow.java`, `Main.java`, `TestUi.java`, `README.md`.

- [ ] Viết test assert MainWindow đăng ký đủ sáu `ScreenId` và không tạo child `JFrame`.
- [ ] Chạy `mvn -o -B -Dmaven.repo.local=.m2 -Dtest=TestUi test`; xác nhận test phản ánh bố cục cũ.
- [ ] Thay `GridLayout` và observer/timer trực tiếp bằng screen manager, sidebar và `MarketSessionController`; giữ cờ `--ui`.
- [ ] Kết nối một session event tới Dashboard, BotScreen, HistoryScreen, PendingOrdersScreen và status bar.
- [ ] Chạy test UI; xác nhận xanh, sau đó cập nhật README về điều hướng nhiều màn hình.
- [ ] Commit `git add src/main/java/com/tradingsimulator/ui/MainWindow.java src/main/java/com/tradingsimulator/Main.java src/test/java/com/tradingsimulator/TestUi.java README.md; git commit -m "feat: integrate multi-screen Swing workspace"`.

### Task 7: Nghiệm thu và tài liệu

**Files:** `docs/KE_HOACH.md`, `docs/PROGRESS_TRACKER.md`, `docs/PROJECT_SPEC.md`, `docs/UI_CUSTOMIZATION_GUIDE.md`.

- [ ] Chạy `powershell -ExecutionPolicy Bypass -File scripts/run-tests.ps1`; xác nhận 0 failures và 0 errors.
- [ ] Chạy UI bằng `mvn -o -Dmaven.repo.local=.m2 exec:java -Dexec.args=--ui`, kiểm tra sáu màn hình, quote, bot, pending order và theme.
- [ ] Chạy `git diff --check` và `git status --short`; chỉ giữ thay đổi có chủ đích.
- [ ] Cập nhật số test và đánh dấu multi-screen UI, session controller, theme ngữ nghĩa, pending screen hoàn tất.
- [ ] Commit `git add docs/KE_HOACH.md docs/PROGRESS_TRACKER.md docs/PROJECT_SPEC.md docs/UI_CUSTOMIZATION_GUIDE.md; git commit -m "docs: complete multi-screen UI rollout"`.
