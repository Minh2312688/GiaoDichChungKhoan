package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Chiến lược ngưỡng RSI: mua khi chỉ báo dưới ngưỡng thấp, bán khi trên ngưỡng cao.
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public final class RsiThresholdStrategy extends BaseStrategy {
  private final double low;
  private final double high;

  public RsiThresholdStrategy() {
    this(30, 70);
  }

  public RsiThresholdStrategy(double low, double high) {
    super(1);
    if (low < 0 || high > 100 || low >= high) throw new IllegalArgumentException("RSI thresholds invalid");
    this.low = low;
    this.high = high;
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    if (prices == null || prices.size() < 2) return null;
    BigDecimal previous = prices.get(prices.size() - 2);
    BigDecimal current = prices.get(prices.size() - 1);
    if (previous.signum() == 0) return null;
    BigDecimal change = current.subtract(previous)
        .divide(previous, 8, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
    double proxyRsi = BigDecimal.valueOf(50).add(change).doubleValue();
    return signal(proxyRsi);
  }

  @Override
  public OrderSide signal(double rsi) {
    return rsi < low ? OrderSide.BUY : rsi > high ? OrderSide.SELL : null;
  }
}
