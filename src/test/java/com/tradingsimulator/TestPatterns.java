package com.tradingsimulator;

import com.tradingsimulator.marketdata.adapters.*;
import com.tradingsimulator.marketdata.observers.*;
import com.tradingsimulator.model.*;
import com.tradingsimulator.tradingengine.*;
import com.tradingsimulator.tradingengine.botstrategies.*;
import com.tradingsimulator.validationandportfolio.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử Adapter/Observer/Factory/Strategy/Chain (Thành viên 1–4). */
class TestPatterns {
  @Test void adapterNormalizesMockQuoteAndObserverReceivesIt() {
    MockMarketDataProvider provider = new MockMarketDataProvider(Map.of("ABC", new BigDecimal("12.50")));
    MarketAdapter adapter = new MarketAdapter(provider);
    List<Quote> received = new ArrayList<>();
    PriceObserver observer = new PriceObserver(received::add);
    adapter.addObserver(observer); adapter.refresh("ABC");
    assertEquals(new BigDecimal("12.50"), received.get(0).last());
  }
  @Test void factoryCreatesOrderTypes() {
    OrderFactory f = new OrderFactory();
    assertEquals(OrderType.MARKET, f.market("ABC", OrderSide.BUY, 1).type());
    assertEquals(OrderType.LIMIT, f.limit("ABC", OrderSide.BUY, 1, new BigDecimal("10")).type());
    assertEquals(OrderType.STOP_LOSS, f.stopLoss("ABC", OrderSide.SELL, 1, new BigDecimal("9")).type());
  }
  @Test void strategiesProduceSignals() {
    assertEquals(OrderSide.BUY, new MovingAverageCrossoverStrategy(2).signal(List.of(new BigDecimal("10"), new BigDecimal("12"), new BigDecimal("14"))));
    assertEquals(OrderSide.BUY, new RsiThresholdStrategy(30, 70).signal(20));
  }
  @Test void validationRejectsInsufficientCashAndPosition() {
    ValidationChain chain = ValidationChain.defaultChain();
    assertThrows(IllegalArgumentException.class, () -> chain.validate(new Order("ABC", OrderSide.BUY, OrderType.MARKET, 2, null), new BigDecimal("5"), 0, new BigDecimal("10")));
    assertThrows(IllegalArgumentException.class, () -> chain.validate(new Order("ABC", OrderSide.SELL, OrderType.MARKET, 2, null), new BigDecimal("100"), 1, new BigDecimal("10")));
  }
}
