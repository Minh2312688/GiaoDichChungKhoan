package com.tradingsimulator.tradingengine.botstrategies;

import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
  private final BigDecimal multiplier;

  public BollingerBandsStrategy() {
    this(5, new BigDecimal("2"));
  }

  public BollingerBandsStrategy(int period, BigDecimal multiplier) {
    super(period);
    if (multiplier == null || multiplier.signum() <= 0) {
      throw new IllegalArgumentException("Multiplier must be positive");
    }
    this.multiplier = multiplier;
  }

  @Override
  public OrderSide signal(List<BigDecimal> prices) {
    if (prices == null || prices.size() < period) {
      return null;
    }
    List<BigDecimal> window = prices.subList(prices.size() - period, prices.size());
    BigDecimal mean = window.stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    BigDecimal variance = window.stream()
        .map(value -> value.subtract(mean).pow(2))
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
    BigDecimal stdDev = variance.sqrt(new MathContext(10));
    BigDecimal upper = mean.add(multiplier.multiply(stdDev));
    BigDecimal lower = mean.subtract(multiplier.multiply(stdDev));
    BigDecimal last = prices.get(prices.size() - 1);
    return last.compareTo(lower) < 0 ? OrderSide.BUY
        : last.compareTo(upper) > 0 ? OrderSide.SELL : null;
  }
}
