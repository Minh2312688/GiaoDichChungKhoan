package com.tradingsimulator;

import com.tradingsimulator.marketdata.adapters.CsvMarketDataProvider;
import com.tradingsimulator.marketdata.adapters.RandomWalkMarketDataProvider;
import com.tradingsimulator.model.Quote;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Chủ sở hữu: Thành viên 3 (mẫu Adapter, Observer). */
class TestMarketData {

  @Test
  void randomWalkIsDeterministicAndKeepsPositivePrices() {
    RandomWalkMarketDataProvider first =
        new RandomWalkMarketDataProvider(Map.of("ABC", new BigDecimal("100.00")), 7L);
    RandomWalkMarketDataProvider second =
        new RandomWalkMarketDataProvider(Map.of("ABC", new BigDecimal("100.00")), 7L);
    for (int i = 0; i < 20; i++) {
      Quote a = first.fetch("ABC");
      Quote b = second.fetch("ABC");
      assertEquals(a.last(), b.last());
      assertTrue(a.bid().signum() >= 0);
      assertTrue(a.ask().compareTo(a.bid()) > 0);
    }
  }

  @Test
  void randomWalkRejectsUnknownSymbol() {
    RandomWalkMarketDataProvider provider =
        new RandomWalkMarketDataProvider(Map.of("ABC", new BigDecimal("10")), 1L);
    assertThrows(IllegalArgumentException.class, () -> provider.fetch("NOPE"));
  }

  @Test
  void csvProviderLoadsClasspathResource() {
    InputStream in = getClass().getClassLoader().getResourceAsStream("market_data.csv");
    assertNotNull(in);
    CsvMarketDataProvider provider = new CsvMarketDataProvider(in, new BigDecimal("0.10"));
    Quote quote = provider.fetch("AAPL");
    assertEquals(new BigDecimal("150.00"), quote.last());
    assertEquals(new BigDecimal("149.90"), quote.bid());
    assertEquals(new BigDecimal("150.10"), quote.ask());
  }
}
