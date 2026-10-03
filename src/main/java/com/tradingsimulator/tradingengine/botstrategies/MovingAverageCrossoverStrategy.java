package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Chiến lược cắt trung bình động (Moving Average Crossover): BUY khi MA hiện tại cao hơn MA
 * trước đó, SELL khi thấp hơn.
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public final class MovingAverageCrossoverStrategy extends BaseStrategy {
  public MovingAverageCrossoverStrategy() {
    this(3);
  }

  public MovingAverageCrossoverStrategy(int period) {
    super(period);
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    if (prices == null || prices.size() < period + 1) return null;
    BigDecimal previous = average(prices.subList(prices.size() - period - 1, prices.size() - 1));
    BigDecimal current = average(prices.subList(prices.size() - period, prices.size()));
    return current.compareTo(previous) > 0 ? OrderSide.BUY
        : current.compareTo(previous) < 0 ? OrderSide.SELL : null;
  }

  private BigDecimal average(List<BigDecimal> values) {
    return values.stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
  }
}
