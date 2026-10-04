package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.util.List;

/**
 * Chiến lược Bollinger Bands (dao động hồi quy về trung bình).
 *
 * <p>BUY khi giá mới nhất đóng dưới dải dưới và SELL khi đóng trên dải trên. Dải giữa là trung
 * bình động đơn giản, hai dải lệch đi {@code multiplier} lần độ lệch chuẩn.</p>
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public final class BollingerBandsStrategy extends BaseStrategy {
  private final double multiplier;

  public BollingerBandsStrategy() {
    this(5, 2.0);
  }

  public BollingerBandsStrategy(int period, BigDecimal multiplier) {
    this(period, multiplier == null ? Double.NaN : multiplier.doubleValue());
  }

  public BollingerBandsStrategy(int period, double multiplier) {
    super(period);
    if (!Double.isFinite(multiplier) || multiplier <= 0.0) {
      throw new IllegalArgumentException("Hệ số Bollinger phải dương");
    }
    this.multiplier = multiplier;
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    if (prices == null || prices.size() < period) {
      return null;
    }
    List<BigDecimal> window = prices.subList(prices.size() - period, prices.size());
    double mean = window.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
    double variance = window.stream()
        .mapToDouble(value -> Math.pow(value.doubleValue() - mean, 2.0))
        .average().orElse(0.0);
    double stdDev = Math.sqrt(variance);
    double upper = mean + multiplier * stdDev;
    double lower = mean - multiplier * stdDev;
    double last = prices.get(prices.size() - 1).doubleValue();
    return last < lower ? OrderSide.BUY : last > upper ? OrderSide.SELL : null;
  }
}
