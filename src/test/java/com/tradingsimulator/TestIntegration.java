package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.*;
import com.tradingsimulator.model.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Luồng tích hợp quote → kiểm tra → thực thi → danh mục (Thành viên 2 – Facade/Memento). */
class TestIntegration {
  @Test void quoteValidationFactoryExecutionPortfolioWorkflow() {
    ExchangeEngine engine = ExchangeEngine.getInstance(); engine.reset(new BigDecimal("1000"));
    TradingFacade facade = new TradingFacade(engine);
    facade.setMarketAdapter(new MarketAdapter(new MockMarketDataProvider(Map.of("XYZ", new BigDecimal("25")))));
    facade.refreshQuote("XYZ"); facade.submitMarketOrder("XYZ", OrderSide.BUY, 10);
    assertEquals(10, facade.position("XYZ").quantity());
    var snapshot = facade.snapshot(); facade.submitMarketOrder("XYZ", OrderSide.SELL, 5); facade.restore(snapshot);
    assertEquals(10, facade.position("XYZ").quantity());
  }
}
