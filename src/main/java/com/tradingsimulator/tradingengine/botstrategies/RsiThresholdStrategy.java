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
    this(14, 30, 70);
  }

  public RsiThresholdStrategy(double low, double high) {
    this(14, low, high);
  }

  public RsiThresholdStrategy(int period, double low, double high) {
    super(period);
    if (!Double.isFinite(low) || !Double.isFinite(high)
        || low < 0 || high > 100 || low >= high) {
      throw new IllegalArgumentException("Ngưỡng RSI không hợp lệ");
    }
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
    if (prices == null || prices.size() < period + 1) return Double.NaN;

    double averageGain = 0.0;
    double averageLoss = 0.0;
    for (int i = 1; i <= period; i++) {
      double change = prices.get(i).doubleValue() - prices.get(i - 1).doubleValue();
      if (change > 0.0) {
        averageGain += change;
      } else if (change < 0.0) {
        averageLoss -= change;
      }
    }
    averageGain /= period;
    averageLoss /= period;

    for (int i = period + 1; i < prices.size(); i++) {
      double change = prices.get(i).doubleValue() - prices.get(i - 1).doubleValue();
      double gain = change > 0.0 ? change : 0.0;
      double loss = change < 0.0 ? -change : 0.0;
      averageGain = ((averageGain * (period - 1)) + gain) / period;
      averageLoss = ((averageLoss * (period - 1)) + loss) / period;
    }

    if (averageLoss > 0.0) {
      double relativeStrength = averageGain / averageLoss;
      return 100.0 - (100.0 / (1.0 + relativeStrength));
    }
    return averageGain > 0.0 ? 100.0 : 50.0;
  }

  @Override
  public OrderSide signal(double rsi) {
    return rsi < low ? OrderSide.BUY : rsi > high ? OrderSide.SELL : null;
  }
}
