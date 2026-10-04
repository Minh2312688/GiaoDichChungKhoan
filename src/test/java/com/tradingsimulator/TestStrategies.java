package com.tradingsimulator;

import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.tradingengine.botstrategies.BollingerBandsStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MacdStrategy;
import com.tradingsimulator.tradingengine.botstrategies.RsiThresholdStrategy;
import com.tradingsimulator.ui.IndicatorDisplayFormatter;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Chủ sở hữu: Thành viên 4 (mẫu Strategy). */
class TestStrategies {

  @Test
  void macdSignalsBuyOnRisingMomentum() {
    MacdStrategy strategy = new MacdStrategy(2, 3, 1);
    assertEquals(OrderSide.BUY, strategy.signal(List.of(bd("10"), bd("11"), bd("12"), bd("14"))));
  }

  @Test
  void macdSignalsSellOnFallingMomentum() {
    MacdStrategy strategy = new MacdStrategy(2, 3, 1);
    assertEquals(OrderSide.SELL, strategy.signal(List.of(bd("14"), bd("12"), bd("11"), bd("10"))));
  }

  @Test
  void macdReturnsNullWhenDataIsInsufficient() {
    assertNull(new MacdStrategy(2, 3, 1).signal(List.of(bd("10"), bd("11"))));
  }

  @Test
  void bollingerSignalsBuyBelowLowerBand() {
    BollingerBandsStrategy strategy = new BollingerBandsStrategy(3, new BigDecimal("1"));
    assertEquals(OrderSide.BUY, strategy.signal(List.of(bd("10"), bd("10"), bd("1"))));
  }

  @Test
  void bollingerSignalsSellAboveUpperBand() {
    BollingerBandsStrategy strategy = new BollingerBandsStrategy(3, new BigDecimal("1"));
    assertEquals(OrderSide.SELL, strategy.signal(List.of(bd("1"), bd("1"), bd("10"))));
  }

  @Test
  void bollingerReturnsNullInsideBands() {
    BollingerBandsStrategy strategy = new BollingerBandsStrategy(3, new BigDecimal("2"));
    assertNull(strategy.signal(List.of(bd("10"), bd("10"), bd("10"))));
  }

  @Test
  void rsiUsesStrictThresholdComparisons() {
    RsiThresholdStrategy strategy = new RsiThresholdStrategy(30, 70);
    assertEquals(OrderSide.BUY, strategy.signal(29.999));
    assertNull(strategy.signal(30.0));
    assertNull(strategy.signal(70.0));
    assertEquals(OrderSide.SELL, strategy.signal(70.001));
  }

  @Test
  void indicatorValuesAreRoundedForDisplay() {
    assertEquals("35.42", IndicatorDisplayFormatter.format(35.4249));
  }

  @Test
  void rsiUsesWilderSmoothing() {
    RsiThresholdStrategy strategy = new RsiThresholdStrategy(14, 30, 70);
    double rsi = strategy.estimateRsi(List.of(
        bd("44.34"), bd("44.09"), bd("44.15"), bd("43.61"), bd("44.33"),
        bd("44.83"), bd("45.10"), bd("45.42"), bd("45.84"), bd("46.08"),
        bd("45.89"), bd("46.03"), bd("45.61"), bd("46.28"), bd("46.28")));

    assertEquals(70.464, rsi, 0.001);
  }

  @Test
  void rsiNeedsPeriodPlusOnePrices() {
    RsiThresholdStrategy strategy = new RsiThresholdStrategy(14, 30, 70);
    assertTrue(Double.isNaN(strategy.estimateRsi(List.of(bd("10"), bd("11")))));
  }

  @Test
  void rsiHandlesFlatAndRisingSeries() {
    RsiThresholdStrategy strategy = new RsiThresholdStrategy(3, 30, 70);
    assertEquals(50.0, strategy.estimateRsi(List.of(bd("10"), bd("10"), bd("10"), bd("10"))), 0.0001);
    assertEquals(100.0, strategy.estimateRsi(List.of(bd("10"), bd("11"), bd("12"), bd("13"))), 0.0001);
  }

  private static BigDecimal bd(String value) {
    return new BigDecimal(value);
  }
}
