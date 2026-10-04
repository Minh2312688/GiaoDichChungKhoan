package com.tradingsimulator.ui.bot;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Điều phối dữ liệu giá, Strategy và việc gửi lệnh tự động qua Facade. */
public final class BotController {
  public enum Status {
    DISABLED("Bot: TẮT"),
    WAITING_SIGNAL("Chờ tín hiệu"),
    ORDER_SUBMITTED("Đã đặt lệnh"),
    ERROR("Lỗi xử lý");

    private final String label;

    Status(String label) {
      this.label = label;
    }

    public String label() {
      return label;
    }
  }

  private final TradingFacade facade;
  private final int maxHistorySize;
  private final int orderQuantity;
  private final Map<String, Deque<BigDecimal>> priceHistory = new HashMap<>();
  private Status status = Status.DISABLED;

  public BotController(TradingFacade facade) {
    this(facade, 100, 1);
  }

  public BotController(TradingFacade facade, int maxHistorySize, int orderQuantity) {
    if (facade == null) {
      throw new IllegalArgumentException("Trading facade không được để trống");
    }
    if (maxHistorySize < 1 || orderQuantity < 1) {
      throw new IllegalArgumentException("Giới hạn lịch sử và khối lượng lệnh phải dương");
    }
    this.facade = facade;
    this.maxHistorySize = maxHistorySize;
    this.orderQuantity = orderQuantity;
  }

  /** Nhận quote mới, cập nhật lịch sử và gửi lệnh nếu Strategy phát tín hiệu. */
  public synchronized Optional<Trade> onQuote(Quote quote) {
    if (quote == null) {
      throw new IllegalArgumentException("Báo giá không được để trống");
    }
    Deque<BigDecimal> prices = priceHistory.computeIfAbsent(quote.symbol(), symbol -> new ArrayDeque<>());
    prices.addLast(quote.last());
    while (prices.size() > maxHistorySize) {
      prices.removeFirst();
    }

    if (!facade.isBotEnabled()) {
      status = Status.DISABLED;
      return Optional.empty();
    }
    try {
      OrderSide signal = facade.strategySignal(List.copyOf(prices));
      if (signal == null) {
        status = Status.WAITING_SIGNAL;
        return Optional.empty();
      }
      Trade trade = facade.submitMarketOrder(quote.symbol(), signal, orderQuantity);
      status = Status.ORDER_SUBMITTED;
      return Optional.of(trade);
    } catch (IllegalArgumentException error) {
      status = Status.ERROR;
      throw error;
    }
  }

  public synchronized Status status() {
    return status;
  }

  public synchronized List<BigDecimal> prices(String symbol) {
    Deque<BigDecimal> prices = priceHistory.get(symbol);
    return prices == null ? List.of() : List.copyOf(new ArrayList<>(prices));
  }
}
