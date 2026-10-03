package com.tradingsimulator.ui;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.marketdata.adapters.RandomWalkMarketDataProvider;
import com.tradingsimulator.marketdata.observers.PriceObserver;
import com.tradingsimulator.ui.components.BotPanel;
import com.tradingsimulator.ui.components.OrderFormPanel;
import com.tradingsimulator.ui.components.OrderHistoryPanel;
import com.tradingsimulator.ui.components.PortfolioPanel;
import com.tradingsimulator.ui.components.PriceBoardPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Cửa sổ chính của ứng dụng Swing: bố cục bảng giá, form lệnh, panel bot, danh mục và lịch sử
 * lệnh; đăng ký observer để cập nhật giá tự động.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer, phần giao diện).</p>
 */
public final class MainWindow extends JFrame {
  private final TradingFacade facade;
  private final PortfolioPanel portfolioPanel;
  private final PriceBoardPanel priceBoard;
  private final OrderHistoryPanel orderHistory;
  private final MarketAdapter adapter;
  private final Timer timer;
  private final Set<String> symbols;

  public MainWindow() {
    this(new TradingFacade(ExchangeEngine.getInstance()));
  }

  public MainWindow(TradingFacade facade) {
    super("Trading Simulator");
    this.facade = facade;

    // 1. Dữ liệu thị trường: random walk tất định để bảng giá luôn biến động khi offline.
    Map<String, BigDecimal> initialPrices = Map.of(
        "AAPL", new BigDecimal("150.00"),
        "BTC", new BigDecimal("45000.00"),
        "ETH", new BigDecimal("3000.00"));
    this.symbols = initialPrices.keySet();
    this.adapter = new MarketAdapter(new RandomWalkMarketDataProvider(initialPrices, 42L));
    this.facade.setMarketAdapter(this.adapter);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(8, 8));

    // 2. Các thành phần giao diện
    priceBoard = new PriceBoardPanel();
    OrderFormPanel orderForm = new OrderFormPanel();
    portfolioPanel = new PortfolioPanel();
    orderHistory = new OrderHistoryPanel();
    BotPanel botPanel = new BotPanel(facade);

    // 3. Bố cục
    JPanel centerPanel = new JPanel(new GridLayout(2, 2, 8, 8));
    centerPanel.add(priceBoard);
    centerPanel.add(orderForm);
    centerPanel.add(botPanel);
    centerPanel.add(portfolioPanel);
    add(centerPanel, BorderLayout.CENTER);
    add(orderHistory, BorderLayout.SOUTH);

    // 4. Observer và bộ lắng nghe sự kiện
    adapter.addObserver(new PriceObserver(quote -> SwingUtilities.invokeLater(() -> {
      priceBoard.setQuote(quote.symbol(), quote.bid().toPlainString(), quote.ask().toPlainString());
      facade.updateQuote(quote);
      try {
        facade.matchPending(quote.symbol());
      } catch (IllegalArgumentException error) {
        // Giữ lệnh pending nếu validation thất bại; timer vẫn tiếp tục cập nhật giá.
      }
      refreshPortfolio();
    })));

    orderForm.onSubmit(order -> {
      try {
        facade.submit(order);
        refreshPortfolio();
      } catch (IllegalArgumentException error) {
        orderForm.setStatus(error.getMessage());
      }
    });

    // 5. Bộ đếm mô phỏng (cập nhật giá mỗi 2 giây)
    timer = new Timer(2000, event -> symbols.forEach(adapter::refresh));
    timer.start();

    ThemeManager.load("theme_config.json").applyTo(getContentPane());
    setMinimumSize(new Dimension(900, 600));
    pack();
    setLocationByPlatform(true);
    refreshPortfolio();
  }

  private void refreshPortfolio() {
    portfolioPanel.showCash(facade.cash().setScale(2, RoundingMode.HALF_UP).toPlainString());
    portfolioPanel.showPositions(facade.positions().toString());
    orderHistory.showTrades(facade.trades());
  }
}
