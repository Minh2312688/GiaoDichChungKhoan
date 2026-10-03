package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
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
    if (low < 0 || high > 100 || low >= high) throw new IllegalArgumentException("Ngưỡng RSI không hợp lệ");
    this.low = low;
    this.high = high;
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    double rsi = estimateRsi(prices);
    return Double.isNaN(rsi) ? null : signal(rsi);
  }

  /** Estimates RSI as double for high-volume indicator calculations. */
  public double estimateRsi(List<BigDecimal> prices) {
    if (prices == null || prices.size() < 2) return Double.NaN;
    double previous = prices.get(prices.size() - 2).doubleValue();
    double current = prices.get(prices.size() - 1).doubleValue();
    if (previous > -Double.MIN_VALUE && previous < Double.MIN_VALUE) return Double.NaN;
    return 50.0 + ((current - previous) / previous * 100.0);
  }

  @Override
  public OrderSide signal(double rsi) {
    return rsi < low ? OrderSide.BUY : rsi > high ? OrderSide.SELL : null;
  }
}
