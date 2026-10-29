package com.tradingsimulator.ui;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.marketdata.adapters.RandomWalkMarketDataProvider;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.reporting.PerformanceReport;
import com.tradingsimulator.ui.bot.BotController;
import com.tradingsimulator.ui.components.OrderFormPanel;
import com.tradingsimulator.ui.navigation.ScreenId;
import com.tradingsimulator.ui.navigation.ScreenManager;
import com.tradingsimulator.ui.navigation.SidebarNavigation;
import com.tradingsimulator.ui.screens.BotScreen;
import com.tradingsimulator.ui.screens.DashboardPanel;
import com.tradingsimulator.ui.screens.HistoryScreen;
import com.tradingsimulator.ui.screens.PendingOrdersScreen;
import com.tradingsimulator.auth.AuthService;
import com.tradingsimulator.auth.User;
import com.tradingsimulator.ui.auth.AuthDialog;
import com.tradingsimulator.ui.screens.SettingsPanel;
import com.tradingsimulator.ui.screens.TradingPanel;
import com.tradingsimulator.ui.session.MarketSessionController;
import com.tradingsimulator.validationandportfolio.PortfolioMemento;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Cửa sổ chính của ứng dụng Swing với điều hướng đa màn hình (CardLayout/ScreenManager)
 * và điều phối phiên giao dịch tập trung qua MarketSessionController.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer, phần giao diện).</p>
 */
public final class MainWindow extends JFrame {
  private static final Color SURFACE = new Color(0x1e, 0x23, 0x29);
  private static final Color BORDER = new Color(0x2b, 0x31, 0x3a);
  private final TradingFacade facade;
  private final MarketAdapter adapter;
  private final Set<String> symbols;
  private final BotController botController;
  private final MarketSessionController sessionController;

  private final ScreenManager screenManager;
  private final SidebarNavigation sidebarNavigation;

  private final DashboardPanel dashboardPanel;
  private final TradingPanel tradingPanel;
  private final BotScreen botScreen;
  private final HistoryScreen historyScreen;
  private final PendingOrdersScreen pendingOrdersScreen;
  private final SettingsPanel settingsPanel;
  private final JLabel statusBar;
  private final JLabel cashMetric;
  private final JLabel equityMetric;
  private final JLabel pnlMetric;
  private final JLabel userBadgeLabel;
  private User currentUser;
  private final LivePulseIcon pulseIcon;
  private final javax.swing.Timer pulseTimer;

  public MainWindow() {
    this(new TradingFacade(ExchangeEngine.getInstance()));
  }

  public MainWindow(TradingFacade facade) {
    super(setupLookAndFeel());
    this.facade = facade;
    ThemeManager.load("theme_config.json").applyTo(getContentPane());

    // 1. Dữ liệu thị trường
    Map<String, BigDecimal> initialPrices = Map.of(
        "AAPL", new BigDecimal("150.00"),
        "BTC", new BigDecimal("45000.00"),
        "ETH", new BigDecimal("3000.00"));
    this.symbols = initialPrices.keySet();
    this.adapter = new MarketAdapter(new RandomWalkMarketDataProvider(initialPrices, 42L));
    this.facade.setMarketAdapter(this.adapter);
    for (String symbol : symbols) {
      this.facade.updateQuote(this.adapter.getQuote(symbol));
    }

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // 2. Controller và Bot
    this.botController = new BotController(facade);
    this.sessionController = new MarketSessionController(facade, adapter, symbols, botController);

    // 3. Khởi tạo các màn hình
    this.dashboardPanel = new DashboardPanel();
    this.tradingPanel = new TradingPanel(symbols);
    this.botScreen = new BotScreen(facade);
    this.historyScreen = new HistoryScreen();
    this.pendingOrdersScreen = new PendingOrdersScreen();
    this.settingsPanel = new SettingsPanel();

    // 4. Quản lý màn hình & sidebar
    this.screenManager = new ScreenManager();
    this.screenManager.register(ScreenId.DASHBOARD, dashboardPanel);
    this.screenManager.register(ScreenId.TRADING, tradingPanel);
    this.screenManager.register(ScreenId.BOT, botScreen);
    this.screenManager.register(ScreenId.HISTORY, historyScreen);
    this.screenManager.register(ScreenId.PENDING_ORDERS, pendingOrdersScreen);
    this.screenManager.register(ScreenId.SETTINGS, settingsPanel);

    this.sidebarNavigation = new SidebarNavigation(screenManager::show);

    // 5. Header và Status Bar
    JPanel headerPanel = new JPanel(new BorderLayout(18, 0));
    headerPanel.setBackground(SURFACE);
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
        BorderFactory.createEmptyBorder(10, 18, 10, 18)));
    JPanel titleGroup = new JPanel();
    titleGroup.setOpaque(false);
    titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
    JLabel titleLabel = new JLabel("TRADING SIMULATOR");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
    titleLabel.setForeground(new Color(0xf0, 0xb9, 0x0b));
    JLabel subtitle = new JLabel("OFFLINE MARKET  ·  PAPER TRADING");
    subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 10f));
    subtitle.setForeground(new Color(0x84, 0x8e, 0x9c));
    titleGroup.add(titleLabel);
    titleGroup.add(Box.createVerticalStrut(3));
    titleGroup.add(subtitle);
    headerPanel.add(titleGroup, BorderLayout.WEST);

    cashMetric = new JLabel("-");
    equityMetric = new JLabel("-");
    pnlMetric = new JLabel("-");
    JPanel metrics = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    metrics.setOpaque(false);
    metrics.add(metricCard("CASH", cashMetric));
    metrics.add(metricCard("EQUITY", equityMetric));
    metrics.add(metricCard("NET REALIZED", pnlMetric));
    headerPanel.add(metrics, BorderLayout.CENTER);

    // Bảng thông tin người dùng và nút Đăng xuất
    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    userPanel.setOpaque(false);

    userBadgeLabel = new JLabel("👤 Demo Trader (demo)");
    userBadgeLabel.setFont(userBadgeLabel.getFont().deriveFont(Font.BOLD, 12f));
    userBadgeLabel.setForeground(new Color(0xea, 0xec, 0xef));

    JPanel userCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
    userCard.setBackground(new Color(0x2b, 0x31, 0x3a));
    userCard.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    userCard.add(userBadgeLabel);

    JButton logoutButton = new JButton("Đăng xuất");
    logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD, 11f));
    logoutButton.setFocusPainted(false);
    logoutButton.setBackground(new Color(0x36, 0x3d, 0x47));
    logoutButton.setForeground(new Color(0xf6, 0x46, 0x5d));
    logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    logoutButton.setToolTipText("Đăng xuất hoặc chuyển sang tài khoản khác");
    logoutButton.addActionListener(event -> performLogout());

    userPanel.add(userCard);
    userPanel.add(logoutButton);
    headerPanel.add(userPanel, BorderLayout.EAST);

    pulseIcon = new LivePulseIcon();
    this.statusBar = new JLabel("Đang kết nối với thị trường mô phỏng", pulseIcon, SwingConstants.LEFT);
    this.statusBar.setForeground(new Color(0x84, 0x8e, 0x9c));
    this.statusBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
        BorderFactory.createEmptyBorder(8, 14, 8, 14)));
    pulseTimer = new javax.swing.Timer(520, event -> {
      pulseIcon.toggle();
      statusBar.repaint();
    });
    pulseTimer.start();

    // Bố cục tổng thể
    add(headerPanel, BorderLayout.NORTH);
    add(sidebarNavigation, BorderLayout.WEST);
    add(screenManager.view(), BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

    // 6. Xử lý đặt lệnh từ form
    tradingPanel.getOrderForm().onSubmit(this::submitOrder);
    dashboardPanel.onTradeRequest(this::showQuickTradeDialog);

    // 7. Lắng nghe session update từ controller
    sessionController.addListener(this::onSessionQuote);

    // 8. Khởi chạy timer phiên giao dịch qua controller
    sessionController.start(settingsPanel.refreshDelay());

    // 9. Nạp người dùng mặc định kèm dữ liệu mẫu
    User defaultUser = AuthService.getInstance().getCurrentUser().orElse(null);
    if (defaultUser != null) {
      applyUser(defaultUser);
    }

    setMinimumSize(new Dimension(1180, 740));
    pack();
    setLocationByPlatform(true);
    refreshAllScreens();
  }

  private void onSessionQuote(Quote quote) {
    dashboardPanel.setQuote(quote.symbol(), quote.bid().toPlainString(), quote.ask().toPlainString());
    tradingPanel.showQuote(quote);
    dashboardPanel.showCash(facade.cash().setScale(2, RoundingMode.HALF_UP).toPlainString());
    dashboardPanel.showPositions(facade.positions());
    dashboardPanel.showPendingCount(facade.pendingOrders().size());
    dashboardPanel.showBotStatus(botController.status().label());

    botScreen.showQuote(quote, botController.prices(quote.symbol()), botController.status());
    pendingOrdersScreen.showOrders(facade.pendingOrders());
    historyScreen.showTrades(facade.trades());

    statusBar.setText("MÔ PHỎNG  ·  " + quote.symbol() + " | Khớp: "
        + quote.last().toPlainString() + " | Lệnh chờ: " + facade.pendingOrders().size());
    refreshHeaderMetrics();
  }

  private void submitOrder(Order order) {
    if (order.type() == OrderType.MARKET) {
      facade.submit(order);
    } else {
      facade.submitPending(order);
    }
    refreshAllScreens();
  }

  private void showQuickTradeDialog(String symbol) {
    JDialog dialog = new JDialog(this, "Mua / Bán " + symbol, Dialog.ModalityType.APPLICATION_MODAL);
    OrderFormPanel orderForm = new OrderFormPanel(symbols);
    orderForm.setSymbol(symbol);
    orderForm.onSubmit(order -> {
      submitOrder(order);
      dialog.dispose();
    });

    JPanel content = new JPanel(new BorderLayout(8, 8));
    content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    JLabel hint = new JLabel("Chọn MUA hoặc BÁN, nhập khối lượng và gửi lệnh.");
    content.add(hint, BorderLayout.NORTH);
    content.add(orderForm, BorderLayout.CENTER);
    dialog.setContentPane(content);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setResizable(false);
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  public void refreshAllScreens() {
    dashboardPanel.showCash(facade.cash().setScale(2, RoundingMode.HALF_UP).toPlainString());
    dashboardPanel.showQuotes(facade.quotes().values().stream().toList());
    dashboardPanel.showPositions(facade.positions());
    dashboardPanel.showPendingCount(facade.pendingOrders().size());
    dashboardPanel.showBotStatus(botController.status().label());
    pendingOrdersScreen.showOrders(facade.pendingOrders());
    historyScreen.showTrades(facade.trades());
    refreshHeaderMetrics();
  }

  private void refreshHeaderMetrics() {
    PerformanceReport report = PerformanceReport.from(facade);
    cashMetric.setText(formatMoney(report.cash()));
    equityMetric.setText(formatMoney(report.equity()));
    pnlMetric.setText(formatMoney(report.netRealized()));
    pnlMetric.setForeground(report.netRealized().signum() >= 0
        ? new Color(0x0e, 0xcb, 0x81) : new Color(0xf6, 0x46, 0x5d));
  }

  private static JPanel metricCard(String title, JLabel value) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(SURFACE);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER),
        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    JLabel caption = new JLabel(title);
    caption.setFont(caption.getFont().deriveFont(Font.BOLD, 9f));
    caption.setForeground(new Color(0x84, 0x8e, 0x9c));
    value.setFont(value.getFont().deriveFont(Font.BOLD, 13f));
    value.setForeground(new Color(0xea, 0xec, 0xef));
    card.add(caption);
    card.add(Box.createVerticalStrut(3));
    card.add(value);
    return card;
  }

  private static String formatMoney(BigDecimal value) {
    return String.format(Locale.US, "%,.2f", value);
  }

  private static String setupLookAndFeel() {
    ThemeManager.setupLookAndFeel();
    return "Trình mô phỏng giao dịch";
  }

  @Override
  public void dispose() {
    if (pulseTimer != null) pulseTimer.stop();
    if (sessionController != null) sessionController.stop();
    super.dispose();
  }

  private static final class LivePulseIcon implements Icon {
    private boolean bright = true;

    private void toggle() {
      bright = !bright;
    }

    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x0e, 0xcb, 0x81, bright ? 255 : 110));
        g.fillOval(x + 4, y + 4, 8, 8);
      } finally {
        g.dispose();
      }
    }
  }

  public ScreenManager getScreenManager() {
    return screenManager;
  }

  public SidebarNavigation getSidebarNavigation() {
    return sidebarNavigation;
  }

  public MarketSessionController getSessionController() {
    return sessionController;
  }

  public DashboardPanel getDashboardPanel() {
    return dashboardPanel;
  }

  public TradingPanel getTradingPanel() {
    return tradingPanel;
  }

  public BotScreen getBotScreen() {
    return botScreen;
  }

  public HistoryScreen getHistoryScreen() {
    return historyScreen;
  }

  public PendingOrdersScreen getPendingOrdersScreen() {
    return pendingOrdersScreen;
  }

  public SettingsPanel getSettingsPanel() {
    return settingsPanel;
  }

  public JLabel getStatusBar() {
    return statusBar;
  }

  public TradingFacade getFacade() {
    return facade;
  }

  public void applyUser(User user) {
    if (user == null) {
      return;
    }
    this.currentUser = user;
    if (userBadgeLabel != null) {
      userBadgeLabel.setText("👤 " + user.getFullName() + " (" + user.getUsername() + ")");
    }
    if (user.getPortfolio() != null) {
      Map<String, Quote> targetQuotes = (user.getPortfolio().quotes() == null || user.getPortfolio().quotes().isEmpty())
          ? facade.quotes()
          : user.getPortfolio().quotes();
      PortfolioMemento memento = new PortfolioMemento(
          user.getPortfolio().cash(),
          user.getPortfolio().positions(),
          user.getPortfolio().trades(),
          targetQuotes,
          user.getPortfolio().pendingOrders()
      );
      facade.restore(memento);
    }
    refreshAllScreens();
  }

  public void performLogout() {
    if (currentUser != null) {
      currentUser.setPortfolio(facade.snapshot());
    }
    AuthService.getInstance().logout();

    AuthDialog authDialog = new AuthDialog(this);
    authDialog.setVisible(true);
    if (authDialog.isAuthenticated()) {
      applyUser(authDialog.getAuthenticatedUser());
      if (statusBar != null) {
        statusBar.setText("Đã đăng nhập: " + currentUser.getFullName() + " (" + currentUser.getUsername() + ")");
      }
    } else if (currentUser != null) {
      // Giữ lại phiên hiện tại nếu đóng hộp thoại mà không đăng nhập tài khoản khác
      AuthService.getInstance().setCurrentUser(currentUser);
    }
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public JLabel getUserBadgeLabel() {
    return userBadgeLabel;
  }
}
