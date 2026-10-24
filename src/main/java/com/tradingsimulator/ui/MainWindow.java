package com.tradingsimulator.ui;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.marketdata.adapters.RandomWalkMarketDataProvider;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.bot.BotController;
import com.tradingsimulator.ui.navigation.ScreenId;
import com.tradingsimulator.ui.navigation.ScreenManager;
import com.tradingsimulator.ui.navigation.SidebarNavigation;
import com.tradingsimulator.ui.screens.BotScreen;
import com.tradingsimulator.ui.screens.DashboardPanel;
import com.tradingsimulator.ui.screens.HistoryScreen;
import com.tradingsimulator.ui.screens.PendingOrdersScreen;
import com.tradingsimulator.ui.screens.SettingsPanel;
import com.tradingsimulator.ui.screens.TradingPanel;
import com.tradingsimulator.ui.session.MarketSessionController;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

/**
 * Cửa sổ chính của ứng dụng Swing với điều hướng đa màn hình (CardLayout/ScreenManager)
 * và điều phối phiên giao dịch tập trung qua MarketSessionController.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer, phần giao diện).</p>
 */
public final class MainWindow extends JFrame {
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

  public MainWindow() {
    this(new TradingFacade(ExchangeEngine.getInstance()));
  }

  public MainWindow(TradingFacade facade) {
    super("Trình mô phỏng giao dịch");
    this.facade = facade;

    // 1. Dữ liệu thị trường
    Map<String, BigDecimal> initialPrices = Map.of(
        "AAPL", new BigDecimal("150.00"),
        "BTC", new BigDecimal("45000.00"),
        "ETH", new BigDecimal("3000.00"));
    this.symbols = initialPrices.keySet();
    this.adapter = new MarketAdapter(new RandomWalkMarketDataProvider(initialPrices, 42L));
    this.facade.setMarketAdapter(this.adapter);

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
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    JLabel titleLabel = new JLabel("TRÌNH MÔ PHỎNG GIAO DỊCH CHỨNG KHOÁN");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
    headerPanel.add(titleLabel, BorderLayout.WEST);

    this.statusBar = new JLabel(" Trạng thái: Sẵn sàng");
    statusBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(4, 8, 4, 8)
    ));

    // Bố cục tổng thể
    add(headerPanel, BorderLayout.NORTH);
    add(sidebarNavigation, BorderLayout.WEST);
    add(screenManager.view(), BorderLayout.CENTER);
    add(statusBar, BorderLayout.SOUTH);

    // 6. Xử lý đặt lệnh từ form
    tradingPanel.getOrderForm().onSubmit(order -> {
      if (order.type() == OrderType.MARKET) {
        facade.submit(order);
      } else {
        facade.submitPending(order);
      }
      refreshAllScreens();
    });

    // 7. Lắng nghe session update từ controller
    sessionController.addListener(this::onSessionQuote);

    // 8. Khởi chạy timer phiên giao dịch qua controller
    sessionController.start(settingsPanel.refreshDelay());

    ThemeManager.load("theme_config.json").applyTo(getContentPane());
    setMinimumSize(new Dimension(980, 640));
    pack();
    setLocationByPlatform(true);
    refreshAllScreens();
  }

  private void onSessionQuote(Quote quote) {
    dashboardPanel.setQuote(quote.symbol(), quote.bid().toPlainString(), quote.ask().toPlainString());
    dashboardPanel.showCash(facade.cash().setScale(2, RoundingMode.HALF_UP).toPlainString());
    dashboardPanel.showPositions(facade.positions().toString());
    dashboardPanel.showPendingCount(facade.pendingOrders().size());
    dashboardPanel.showBotStatus(botController.status().label());

    botScreen.showQuote(quote, botController.prices(quote.symbol()), botController.status());
    pendingOrdersScreen.showOrders(facade.pendingOrders());
    historyScreen.showTrades(facade.trades());

    statusBar.setText(" Cập nhật gần nhất: " + quote.symbol() + " | Khớp: "
        + quote.last().toPlainString() + " | Lệnh chờ: " + facade.pendingOrders().size());
  }

  public void refreshAllScreens() {
    dashboardPanel.showCash(facade.cash().setScale(2, RoundingMode.HALF_UP).toPlainString());
    dashboardPanel.showPositions(facade.positions().toString());
    dashboardPanel.showPendingCount(facade.pendingOrders().size());
    dashboardPanel.showBotStatus(botController.status().label());
    pendingOrdersScreen.showOrders(facade.pendingOrders());
    historyScreen.showTrades(facade.trades());
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
}
