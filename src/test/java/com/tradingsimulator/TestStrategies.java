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

  private static BigDecimal bd(String value) {
    return new BigDecimal(value);
  }
}
