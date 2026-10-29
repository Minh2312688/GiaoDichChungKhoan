package com.tradingsimulator;

import com.tradingsimulator.auth.AuthService;
import com.tradingsimulator.auth.User;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.ui.MainWindow;
import com.tradingsimulator.ui.auth.AuthDialog;
import com.tradingsimulator.validationandportfolio.PortfolioMemento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TestAuth {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = AuthService.getInstance();
        authService.resetToDefault();
    }

    @Test
    void testDemoAccountDefaultData() {
        User demoUser = authService.getDemoUser();
        assertNotNull(demoUser, "Tài khoản demo phải tồn tại");
        assertEquals("demo", demoUser.getUsername());
        assertEquals("demo.trader@gmail.com", demoUser.getEmail());
        assertEquals("123", demoUser.getPassword());
        assertTrue(demoUser.getFullName().contains("Demo"));

        PortfolioMemento portfolio = demoUser.getPortfolio();
        assertNotNull(portfolio, "Portfolio của demo phải được nạp sẵn");
        assertEquals(new BigDecimal("25000.00"), portfolio.cash(), "Tiền mặt ban đầu là $25,000");

        // Kiểm tra danh sách chứng khoán đang giữ
        assertEquals(3, portfolio.positions().size(), "Demo phải có 3 mã nắm giữ");
        assertTrue(portfolio.positions().containsKey("AAPL"));
        assertEquals(50, portfolio.positions().get("AAPL").quantity());
        assertTrue(portfolio.positions().containsKey("BTC"));
        assertEquals(1, portfolio.positions().get("BTC").quantity());
        assertTrue(portfolio.positions().containsKey("ETH"));
        assertEquals(5, portfolio.positions().get("ETH").quantity());

        // Kiểm tra lịch sử giao dịch
        assertFalse(portfolio.trades().isEmpty(), "Phải có lịch sử giao dịch mẫu");
        assertEquals(4, portfolio.trades().size(), "Demo phải có 4 lệnh khớp mẫu");

        // Kiểm tra các lệnh chờ khớp
        assertFalse(portfolio.pendingOrders().isEmpty(), "Phải có danh sách lệnh chờ khớp mẫu");
        assertEquals(3, portfolio.pendingOrders().size(), "Demo phải có 3 lệnh chờ");
        assertTrue(portfolio.pendingOrders().stream().anyMatch(o -> o.type() == OrderType.LIMIT && o.side() == OrderSide.BUY));
        assertTrue(portfolio.pendingOrders().stream().anyMatch(o -> o.type() == OrderType.LIMIT && o.side() == OrderSide.SELL));
        assertTrue(portfolio.pendingOrders().stream().anyMatch(o -> o.type() == OrderType.STOP_LOSS));
    }

    @Test
    void testLoginSuccess() {
        User user = authService.login("demo", "123");
        assertNotNull(user);
        assertEquals("demo", user.getUsername());
        assertTrue(authService.getCurrentUser().isPresent());
        assertEquals("demo", authService.getCurrentUser().get().getUsername());
    }

    @Test
    void testLoginWithGmail() {
        User user = authService.login("demo.trader@gmail.com", "123");
        assertNotNull(user);
        assertEquals("demo", user.getUsername());
        assertEquals("demo.trader@gmail.com", user.getEmail());
    }

    @Test
    void testLoginWithGoogleSimulation() {
        // Mặc định đăng nhập vào demo user
        User user = authService.loginWithGoogle("");
        assertNotNull(user);
        assertEquals("demo", user.getUsername());
        assertEquals("demo.trader@gmail.com", user.getEmail());

        // Đăng nhập với Gmail mới tự động tạo tài khoản
        User newUser = authService.loginWithGoogle("investor99@gmail.com");
        assertNotNull(newUser);
        assertEquals("investor99", newUser.getUsername());
        assertEquals("investor99@gmail.com", newUser.getEmail());
    }

    @Test
    void testLoginCaseInsensitiveUsername() {
        User user = authService.login("DEMO", "123");
        assertNotNull(user);
        assertEquals("demo", user.getUsername());
    }

    @Test
    void testLoginFailureWrongPassword() {
        assertThrows(IllegalArgumentException.class, () -> authService.login("demo", "wrong_pass"));
    }

    @Test
    void testLoginFailureNonExistentUser() {
        assertThrows(IllegalArgumentException.class, () -> authService.login("no_such_user", "123"));
    }

    @Test
    void testRegisterSuccess() {
        User newUser = authService.register("trader1", "trader1@gmail.com", "pass123", "Trader Nguyen");
        assertNotNull(newUser);
        assertEquals("trader1", newUser.getUsername());
        assertEquals("trader1@gmail.com", newUser.getEmail());
        assertEquals("pass123", newUser.getPassword());
        assertEquals("Trader Nguyen", newUser.getFullName());

        // Kiểm tra tài khoản mới được cấp $10,000 tiền mặt và danh mục trống
        PortfolioMemento portfolio = newUser.getPortfolio();
        assertNotNull(portfolio);
        assertEquals(new BigDecimal("10000.00"), portfolio.cash());
        assertTrue(portfolio.positions().isEmpty());
        assertTrue(portfolio.trades().isEmpty());
        assertTrue(portfolio.pendingOrders().isEmpty());

        // Đăng nhập lại với tài khoản vừa tạo qua username và qua Gmail
        User loggedInUser = authService.login("trader1", "pass123");
        assertEquals("trader1", loggedInUser.getUsername());

        User loggedInEmail = authService.login("trader1@gmail.com", "pass123");
        assertEquals("trader1", loggedInEmail.getUsername());
    }

    @Test
    void testRegisterDuplicateUsernameFails() {
        assertThrows(IllegalArgumentException.class, () -> authService.register("demo", "demo2@gmail.com", "newpass", "Demo"));
    }

    @Test
    void testRegisterDuplicateEmailFails() {
        assertThrows(IllegalArgumentException.class, () -> authService.register("newuser", "demo.trader@gmail.com", "newpass", "Demo"));
    }

    @Test
    void testRegisterShortPasswordFails() {
        assertThrows(IllegalArgumentException.class, () -> authService.register("trader2", "trader2@gmail.com", "12", "Trader"));
    }

    @Test
    void testMainWindowUserIntegration() {
        MainWindow window = new MainWindow();
        assertNotNull(window.getUserBadgeLabel());
        assertTrue(window.getUserBadgeLabel().getText().contains("demo"));

        // Kiểm tra dữ liệu demo được áp dụng vào facade
        assertEquals(3, window.getFacade().positions().size());
        assertEquals(4, window.getFacade().trades().size());
        assertEquals(3, window.getFacade().pendingOrders().size());
        assertEquals(new BigDecimal("25000.00"), window.getFacade().cash());

        // Chuyển sang người dùng mới
        User newUser = authService.register("testuser", "test@gmail.com", "123", "Test User");
        window.applyUser(newUser);

        assertEquals("testuser", window.getCurrentUser().getUsername());
        assertTrue(window.getUserBadgeLabel().getText().contains("Test User"));
        assertEquals(new BigDecimal("10000.00"), window.getFacade().cash());
        assertTrue(window.getFacade().positions().isEmpty());
        assertTrue(window.getFacade().trades().isEmpty());
        assertTrue(window.getFacade().pendingOrders().isEmpty());

        window.dispose();
    }

    @Test
    void testAuthDialogComponents() {
        AuthDialog dialog = new AuthDialog(null);
        assertNotNull(dialog.getTabbedPane());
        assertEquals(2, dialog.getTabbedPane().getTabCount());
        assertEquals("demo", dialog.getLoginUsernameField().getText());
        assertEquals("123", new String(dialog.getLoginPasswordField().getPassword()));
        assertNotNull(dialog.getRegEmailField());
        assertFalse(dialog.isAuthenticated());
        dialog.dispose();
    }
}
