package com.tradingsimulator.tradingengine;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sổ lệnh lưu các lệnh LIMIT và STOP_LOSS đang chờ và quyết định khi nào một quote kích hoạt
 * chúng.
 *
 * <p>Sổ lệnh chỉ quyết định việc <em>khớp</em>; việc thực thi vẫn do {@code ExchangeEngine}
 * đảm nhiệm và được điều phối bởi {@code TradingFacade}. Lệnh market bị từ chối vì được
 * thực thi ngay lập tức.</p>
 *
 * <p>Chủ sở hữu: Thành viên 1 (mẫu Factory Method).</p>
 */
public final class OrderBook {
  private final Map<String, List<Order>> pending = new ConcurrentHashMap<>();

  public synchronized void add(Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Lệnh không được để trống");
    }
    if (order.type() == OrderType.MARKET) {
      throw new IllegalArgumentException("Lệnh thị trường được thực thi ngay, không đưa vào hàng chờ");
    }
    pending.computeIfAbsent(order.symbol(), symbol -> new ArrayList<>()).add(order);
  }

  public synchronized List<Order> pending(String symbol) {
    return List.copyOf(pending.getOrDefault(symbol, List.of()));
  }

  public synchronized List<Order> openOrders() {
    List<Order> all = new ArrayList<>();
    pending.values().forEach(all::addAll);
    return List.copyOf(all);
  }

  /** Returns every queued order whose trigger condition is satisfied by {@code quote}. */
  public synchronized List<Order> match(Quote quote) {
    List<Order> triggered = triggered(quote);
    triggered.forEach(this::remove);
    return triggered;
  }

  /** Returns triggered orders without removing them from the book. */
  public synchronized List<Order> triggered(Quote quote) {
    if (quote == null) {
      throw new IllegalArgumentException("Báo giá không được để trống");
    }
    List<Order> queue = pending.get(quote.symbol());
    if (queue == null) {
      return List.of();
    }
    List<Order> triggered = new ArrayList<>();
    for (Order order : queue) {
      if (isTriggered(order, quote)) {
        triggered.add(order);
      }
    }
    return List.copyOf(triggered);
  }

  /** Removes an order after its execution has completed successfully. */
  public synchronized void remove(Order order) {
    if (order == null) {
      return;
    }
    List<Order> queue = pending.get(order.symbol());
    if (queue == null) {
      return;
    }
    queue.remove(order);
    if (queue.isEmpty()) {
      pending.remove(order.symbol());
    }
  }

  public synchronized void load(List<Order> orders) {
    pending.clear();
    if (orders != null) {
      orders.forEach(this::add);
    }
  }

  public synchronized void clear() {
    pending.clear();
  }

  public static boolean isTriggered(Order order, Quote quote) {
    BigDecimal price = order.side() == OrderSide.BUY ? quote.ask() : quote.bid();
    if (order.type() == OrderType.LIMIT) {
      return order.side() == OrderSide.BUY
          ? price.compareTo(order.price()) <= 0
          : price.compareTo(order.price()) >= 0;
    }
    if (order.type() == OrderType.STOP_LOSS) {
      return order.side() == OrderSide.SELL
          ? price.compareTo(order.price()) <= 0
          : price.compareTo(order.price()) >= 0;
    }
    return false;
  }
}
