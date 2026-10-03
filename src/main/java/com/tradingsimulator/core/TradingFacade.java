package com.tradingsimulator.core;

import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.tradingengine.OrderBook;
import com.tradingsimulator.tradingengine.botstrategies.IBotStrategy;
import com.tradingsimulator.validationandportfolio.PortfolioMemento;
import com.tradingsimulator.validationandportfolio.ValidationChain;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade cung cấp API đơn giản cho giao diện và bot.
 *
 * <p>Facade là ranh giới duy nhất trước exchange engine: nó kiểm tra hợp lệ lệnh, điều phối
 * {@link OrderBook} cho các lệnh chờ và giữ kết nối với adapter dữ liệu thị trường. Các thành
 * viên nên mở rộng facade bằng các phương thức có tên rõ ràng thay vì truy cập trực tiếp
 * {@code ExchangeEngine}.</p>
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Facade).</p>
 */
public final class TradingFacade {
  private final ExchangeEngine engine;
  private final ValidationChain validation = ValidationChain.defaultChain();
  private final OrderBook orderBook = new OrderBook();
  private MarketAdapter adapter;
  private IBotStrategy strategy;
  private boolean botEnabled;

  public TradingFacade(ExchangeEngine engine) {
    this.engine = engine;
  }

  public void setMarketAdapter(MarketAdapter adapter) {
    this.adapter = adapter;
  }

  public void updateQuote(Quote quote) {
    engine.updateQuote(quote);
  }

  public void refreshQuote(String symbol) {
    if (adapter == null) {
      throw new IllegalArgumentException("Thị trường chưa được cấu hình");
    }
    updateQuote(adapter.getQuote(symbol));
  }

  public Trade submitMarketOrder(String symbol, OrderSide side, int quantity) {
    Quote quote = engine.quotes().get(symbol);
    if (quote == null) {
      throw new IllegalArgumentException("Không có báo giá cho mã: " + symbol);
    }
    Order order = new Order(symbol, side, OrderType.MARKET, quantity, null);
    validation.validate(order, engine.cash(), engine.position(symbol).quantity(),
        side == OrderSide.BUY ? quote.ask() : quote.bid());
    return engine.execute(order);
  }

  public Trade submit(Order order) {
    Quote quote = engine.quotes().get(order.symbol());
    if (quote == null) {
      throw new IllegalArgumentException("Không có báo giá cho mã: " + order.symbol());
    }
    validation.validate(order, engine.cash(), engine.position(order.symbol()).quantity(),
        order.side() == OrderSide.BUY ? quote.ask() : quote.bid());
    return engine.execute(order);
  }

  /** Đưa một lệnh LIMIT hoặc STOP_LOSS vào hàng chờ để khớp sau. */
  public void submitPending(Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Lệnh không được để trống");
    }
    orderBook.add(order);
  }

  /**
   * Khớp mọi lệnh chờ của {@code symbol} theo báo giá mới nhất và thực thi các lệnh đã kích hoạt.
   *
   * @return danh sách lệnh đã khớp, theo thứ tự khớp
   */
  public List<Trade> matchPending(String symbol) {
    Quote quote = engine.quotes().get(symbol);
    if (quote == null) {
      throw new IllegalArgumentException("Không có báo giá cho mã: " + symbol);
    }
    List<Trade> executed = new ArrayList<>();
    for (Order order : orderBook.match(quote)) {
      BigDecimal price = order.side() == OrderSide.BUY ? quote.ask() : quote.bid();
      validation.validate(order, engine.cash(), engine.position(symbol).quantity(), price);
      executed.add(engine.execute(order));
    }
    return List.copyOf(executed);
  }

  public List<Order> pendingOrders() {
    return orderBook.openOrders();
  }

  public List<Trade> trades() {
    return engine.trades();
  }

  public Map<String, Position> positions() {
    return engine.positions();
  }

  public Map<String, Quote> quotes() {
    return engine.quotes();
  }

  public BigDecimal cash() {
    return engine.cash();
  }

  public Position position(String symbol) {
    return engine.position(symbol);
  }

  public PortfolioMemento snapshot() {
    return new PortfolioMemento(engine.cash(), engine.positions(), engine.trades(),
        engine.quotes(), orderBook.openOrders());
  }

  public void restore(PortfolioMemento memento) {
    engine.restore(memento.cash(), memento.positions(), memento.trades(), memento.quotes());
    orderBook.load(memento.pendingOrders());
  }

  public void setStrategy(IBotStrategy strategy) {
    this.strategy = strategy;
  }

  public OrderSide strategySignal(List<BigDecimal> prices) {
    if (strategy == null) {
      throw new IllegalArgumentException("Chiến lược chưa được cấu hình");
    }
    return strategy.signal(prices);
  }

  public void setBotEnabled(boolean enabled) {
    botEnabled = enabled;
  }

  public boolean isBotEnabled() {
    return botEnabled;
  }
}
