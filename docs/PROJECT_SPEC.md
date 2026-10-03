# Đặc tả dự án

Bản MVP cung cấp dữ liệu thị trường chạy offline, các loại lệnh market/limit/stop-loss, kế toán
danh mục, **8 mẫu thiết kế**, giao diện Swing và kiểm thử JUnit 5.

## Mục tiêu

Xây dựng ứng dụng Java Maven mô phỏng giao dịch cổ phiếu chạy offline, có thể biên dịch, kiểm
thử và khởi động từ dòng lệnh. Trọng tâm là luồng đặt lệnh qua `TradingFacade`, kiểm tra hợp lệ,
khớp lệnh giả lập, phát thông báo giá và lưu snapshot danh mục.

## Kiến trúc

Ứng dụng dùng Java 17, Swing cho giao diện và JUnit 5. Miền nghiệp vụ dùng `BigDecimal` cho
tiền/giá và các lớp bất biến cho quote, order, trade. `TradingFacade` là cổng duy nhất của
UI/bot; `ExchangeEngine` là Singleton giữ sổ lệnh tối giản và danh mục hiện tại.

Luồng chính: UI hoặc bot → facade → chuỗi kiểm tra hợp lệ → factory tạo lệnh → exchange engine
→ adapter/observer dữ liệu thị trường → snapshot danh mục. Dữ liệu thị trường mặc định là
provider mô phỏng deterministic để kiểm thử không cần mạng.

## Mẫu thiết kế

| Mẫu | Thành phần | Trách nhiệm |
|---|---|---|
| Singleton | `ExchangeEngine` | Một engine giao dịch trong tiến trình |
| Facade | `TradingFacade` | API đơn giản cho UI/bot |
| Adapter | `IMarketDataProvider`, `MarketAdapter` | Chuẩn hóa dữ liệu quote |
| Observer | `IPriceObserver`, `PriceObserver` | Phát quote tới UI và hệ thống |
| Factory Method | `OrderFactory` | Tạo Market/Limit/Stop-loss order |
| Strategy | `IBotStrategy`, MA/RSI/MACD/Bollinger | Sinh tín hiệu giao dịch |
| Chain of Responsibility | `ValidationChain`, `IValidator` | Balance, position, quantity, price-range |
| Memento | `PortfolioMemento` | Lưu/khôi phục snapshot tài sản |

## Phạm vi MVP

- Đặt lệnh BUY/SELL cho một hoặc nhiều symbol.
- Market order khớp theo quote hiện tại; limit order chỉ khớp khi điều kiện giá đạt;
  stop-loss được biểu diễn và kiểm tra ngưỡng.
- Danh mục gồm tiền mặt, vị thế theo symbol và lịch sử khớp lệnh.
- Provider mô phỏng sinh quote deterministic; adapter cho phép thay provider khác.
- Giao diện Swing có bảng giá, form đặt lệnh, panel bot, tổng kết danh mục và theme JSON.
- Kiểm thử các mẫu, luồng tích hợp và việc nạp theme.

## Ngoài phạm vi

Không kết nối sàn thật, không lưu trữ cơ sở dữ liệu, không đăng nhập, không giao dịch đa tài
sản phức tạp, không yêu cầu JavaFX hay mạng ngoài.

## Quy tắc lỗi

Nhập liệu không hợp lệ hoặc lệnh vi phạm kiểm tra sẽ ném `IllegalArgumentException` với thông
báo rõ ràng. Facade không nuốt lỗi; UI hiển thị lỗi cho người dùng. Thao tác snapshot trả bản
sao dữ liệu để tránh thay đổi ngoài ý muốn.

## Kiểm thử và tiêu chí hoàn thành

`mvn compile` và `mvn test` phải thành công. `mvn exec:java` phải khởi động được ứng dụng và
không cần dịch vụ bên ngoài. Kiểm thử bao phủ Singleton/Facade/Adapter/Observer/Factory/
Strategy/Chain/Memento và một luồng đặt lệnh hoàn chỉnh.
