package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.tradingengine.OrderBook;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Chủ sở hữu: Thành viên 1 (mẫu Factory Method). */
class TestOrderBook {

  @BeforeEach
  void reset() {
    ExchangeEngine.getInstance().reset(new BigDecimal("10000"));
  }

  @Test
  void limitBuyTriggersOnlyWhenAskReachesLimit() {
    OrderBook book = new OrderBook();
    book.add(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 5, new BigDecimal("10")));
    assertTrue(book.match(new Quote("ABC", new BigDecimal("10.20"), new BigDecimal("10.50"), 100)).isEmpty());
    assertEquals(1, book.openOrders().size());
    assertEquals(1, book.match(new Quote("ABC", new BigDecimal("9.50"), new BigDecimal("9.90"), 100)).size());
    assertTrue(book.openOrders().isEmpty());
  }

  @Test
  void marketOrdersCannotBeQueued() {
    OrderBook book = new OrderBook();
    assertThrows(IllegalArgumentException.class,
        () -> book.add(new Order("ABC", OrderSide.BUY, OrderType.MARKET, 1, null)));
  }

  @Test
  void facadeExecutesTriggeredPendingOrder() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("10.00"), new BigDecimal("10.00"), 1000));
    facade.submitPending(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 5, new BigDecimal("10.00")));
    assertEquals(1, facade.pendingOrders().size());
    assertEquals(1, facade.matchPending("ABC").size());
    assertEquals(5, facade.position("ABC").quantity());
    assertTrue(facade.pendingOrders().isEmpty());
  }

  @Test
  void triggeredPendingOrderRemainsWhenExecutionValidationFails() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("10.00"), new BigDecimal("10.00"), 1000));
    facade.submitPending(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 2_000, new BigDecimal("10.00")));

    assertThrows(IllegalArgumentException.class, () -> facade.matchPending("ABC"));
    assertEquals(1, facade.pendingOrders().size());
  }

  @Test
  void snapshotKeepsPendingOrders() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("10.00"), new BigDecimal("10.00"), 1000));
    facade.submitPending(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 5, new BigDecimal("9.00")));
    var snapshot = facade.snapshot();
    facade.submitPending(new Order("ABC", OrderSide.SELL, OrderType.LIMIT, 5, new BigDecimal("20.00")));
    assertEquals(2, facade.pendingOrders().size());
    facade.restore(snapshot);
    assertEquals(1, facade.pendingOrders().size());
  }
}
