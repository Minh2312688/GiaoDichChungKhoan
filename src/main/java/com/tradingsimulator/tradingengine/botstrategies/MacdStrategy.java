package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.util.List;

/**
 * Chiến lược MACD (hội tụ – phân kỳ trung bình động) theo động lượng.
 *
 * <p>Đường MACD là {@code EMA(ngắn) - EMA(dài)}. Chiến lược trả BUY khi đường MACD đang tăng
 * và SELL khi đang giảm — một tín hiệu động lượng đơn giản, phù hợp với trình mô phỏng offline.</p>
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public final class MacdStrategy extends BaseStrategy {
  private final int shortPeriod;
  private final int longPeriod;
  private final int signalPeriod;

  public MacdStrategy() {
    this(3, 6, 3);
  }

  public MacdStrategy(int shortPeriod, int longPeriod, int signalPeriod) {
    super(longPeriod);
    if (shortPeriod < 1 || shortPeriod >= longPeriod) {
      throw new IllegalArgumentException("Chu kỳ ngắn phải dương và nhỏ hơn chu kỳ dài");
    }
    if (signalPeriod < 1) {
      throw new IllegalArgumentException("Tín hiệu (signal period) phải dương");
    }
    this.shortPeriod = shortPeriod;
    this.longPeriod = longPeriod;
    this.signalPeriod = signalPeriod;
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    int required = longPeriod + signalPeriod;
    if (prices == null || prices.size() < required) {
      return null;
    }
    double now = macdLine(prices);
    double previous = macdLine(prices.subList(0, prices.size() - 1));
    return now > previous ? OrderSide.BUY : now < previous ? OrderSide.SELL : null;
  }

  private double macdLine(List<BigDecimal> prices) {
    return ema(prices, shortPeriod) - ema(prices, longPeriod);
  }

  private static double ema(List<BigDecimal> values, int period) {
    double factor = 2.0 / (period + 1);
    double ema = values.get(0).doubleValue();
    for (int i = 1; i < values.size(); i++) {
      double value = values.get(i).doubleValue();
      ema = value - (value - ema) * (1.0 - factor);
    }
    return ema;
  }
}
