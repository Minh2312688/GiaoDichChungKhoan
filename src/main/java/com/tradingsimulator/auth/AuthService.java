package com.tradingsimulator.auth;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.validationandportfolio.PortfolioMemento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dịch vụ xác thực và lưu trữ tài khoản trong bộ nhớ (In-memory).
 * Cung cấp tài khoản demo và dữ liệu mẫu đầy đủ để phục vụ quay video demo.
 */
public final class AuthService {
    private static final AuthService INSTANCE = new AuthService();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private User currentUser;

    public static AuthService getInstance() {
        return INSTANCE;
    }

    private AuthService() {
        resetToDefault();
    }

    /**
     * Khởi tạo lại danh sách tài khoản mặc định kèm tài khoản demo và dữ liệu mẫu.
     */
    public synchronized void resetToDefault() {
        users.clear();
        User demoUser = createDemoUser();
        users.put(demoUser.getUsername().toLowerCase(), demoUser);
        currentUser = demoUser;
    }

    /**
     * Tạo tài khoản demo "demo" / "123" với danh mục mẫu phong phú.
     */
    public static User createDemoUser() {
        PortfolioMemento demoPortfolio = createDemoPortfolio();
        return new User("demo", "123", "Nguyễn Văn Demo (Trader)", demoPortfolio);
    }

    /**
     * Tạo dữ liệu danh mục mẫu:
     * - Tiền mặt: $25,000.00
     * - Danh sách chứng khoán đang giữ (Positions): AAPL (50), BTC (1), ETH (5)
     * - Lịch sử mua/bán (Trades): 4 giao dịch
     * - Lệnh chờ khớp (Pending Orders): 3 lệnh Limit & Stop-loss
     */
    public static PortfolioMemento createDemoPortfolio() {
        BigDecimal cash = new BigDecimal("25000.00");

        // 1. Danh sách chứng khoán đang giữ
        Map<String, Position> positions = new HashMap<>();
        positions.put("AAPL", new Position("AAPL", 50, new BigDecimal("145.00")));
        positions.put("BTC", new Position("BTC", 1, new BigDecimal("42000.00")));
        positions.put("ETH", new Position("ETH", 5, new BigDecimal("2850.00")));

        // 2. Lịch sử mua/bán đã khớp
        Instant now = Instant.now();
        List<Trade> trades = List.of(
            new Trade("AAPL", OrderSide.BUY, 60, new BigDecimal("144.50"),
                new BigDecimal("8670.00"), now.minus(2, ChronoUnit.HOURS)),
            new Trade("AAPL", OrderSide.SELL, 10, new BigDecimal("152.00"),
                new BigDecimal("1520.00"), now.minus(75, ChronoUnit.MINUTES)),
            new Trade("BTC", OrderSide.BUY, 1, new BigDecimal("42000.00"),
                new BigDecimal("42000.00"), now.minus(1, ChronoUnit.DAYS)),
            new Trade("ETH", OrderSide.BUY, 5, new BigDecimal("2850.00"),
                new BigDecimal("14250.00"), now.minus(3, ChronoUnit.HOURS))
        );

        // 3. Các lệnh chờ khớp (giá đặt chưa khớp với giá thị trường hiện tại: AAPL ~150, BTC ~45000, ETH ~3000)
        List<Order> pendingOrders = List.of(
            new Order("AAPL", OrderSide.BUY, OrderType.LIMIT, 20, new BigDecimal("140.00")),
            new Order("BTC", OrderSide.SELL, OrderType.LIMIT, 1, new BigDecimal("48000.00")),
            new Order("ETH", OrderSide.SELL, OrderType.STOP_LOSS, 2, new BigDecimal("2600.00"))
        );

        return new PortfolioMemento(cash, positions, trades, Map.of(), pendingOrders);
    }

    /**
     * Đăng nhập với username và password.
     */
    public synchronized User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        User user = users.get(username.trim().toLowerCase());
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        this.currentUser = user;
        return user;
    }

    /**
     * Đăng ký tài khoản mới.
     * Cấp số dư ban đầu $10,000.00, danh mục và lịch sử trống.
     */
    public synchronized User register(String username, String password, String fullName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.");
        }
        if (password == null || password.length() < 3) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 3 ký tự.");
        }
        String name = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : username.trim();
        PortfolioMemento freshPortfolio = new PortfolioMemento(
            new BigDecimal("10000.00"),
            Map.of(),
            List.of(),
            Map.of(),
            List.of()
        );
        User newUser = new User(key, password, name, freshPortfolio);
        users.put(key, newUser);
        return newUser;
    }

    public synchronized Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized boolean hasUser(String username) {
        return username != null && users.containsKey(username.trim().toLowerCase());
    }

    public synchronized User getDemoUser() {
        return users.get("demo");
    }
}
