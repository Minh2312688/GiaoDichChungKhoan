package com.tradingsimulator;

import com.tradingsimulator.core.*;
import com.tradingsimulator.model.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/** Chủ sở hữu: Thành viên 1 (mẫu Singleton) phối hợp Thành viên 2 (mẫu Facade). */
class TestCore {
  @BeforeEach void reset() { ExchangeEngine.getInstance().reset(new BigDecimal("10000")); }
  @Test void singletonAndMarketBuyUpdatePortfolio() {
    assertSame(ExchangeEngine.getInstance(), ExchangeEngine.getInstance());
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("10"), new BigDecimal("11"), 1000));
    Trade trade = facade.submitMarketOrder("ABC", OrderSide.BUY, 5);
    assertEquals(new BigDecimal("55.00"), trade.total());
    assertEquals(new BigDecimal("9945.00"), facade.cash());
    assertEquals(5, facade.position("ABC").quantity());
  }
  @Test void rejectsInvalidOrder() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    assertThrows(IllegalArgumentException.class, () -> facade.submitMarketOrder("", OrderSide.BUY, 1));
  }
}
