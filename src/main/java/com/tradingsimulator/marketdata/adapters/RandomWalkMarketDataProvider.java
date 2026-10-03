package com.tradingsimulator.marketdata.adapters;

import com.tradingsimulator.model.Quote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Bộ cung cấp dữ liệu thị trường mô phỏng theo bước ngẫu nhiên (random walk).
 *
 * <p>Mỗi lần gọi {@link #fetch(String)} sẽ dịch chuyển giá cuối cùng tối đa bằng phần trăm
 * bước cấu hình. Cùng một seed luôn sinh cùng một chuỗi giá, giúp bot và kiểm thử tích hợp
 * lặp lại được mà không cần mạng.</p>
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Adapter).</p>
 */
public final class RandomWalkMarketDataProvider implements IMarketDataProvider {
  private final Map<String, BigDecimal> prices = new HashMap<>();
  private final Random random;
  private final BigDecimal step;

  public RandomWalkMarketDataProvider(Map<String, BigDecimal> initialPrices, long seed) {
    this(initialPrices, seed, new BigDecimal("0.01"));
  }

  public RandomWalkMarketDataProvider(Map<String, BigDecimal> initialPrices, long seed, BigDecimal step) {
    if (initialPrices == null || initialPrices.isEmpty()) {
      throw new IllegalArgumentException("Cần có giá khởi tạo");
    }
    if (step == null || step.signum() <= 0) {
      throw new IllegalArgumentException("Bước giá phải dương");
    }
    for (Map.Entry<String, BigDecimal> entry : initialPrices.entrySet()) {
      String symbol = entry.getKey();
      BigDecimal price = entry.getValue();
      if (symbol == null || symbol.isBlank() || price == null || price.signum() <= 0) {
        throw new IllegalArgumentException("Giá khởi tạo không hợp lệ cho mã: " + symbol);
      }
      prices.put(symbol, price);
    }
    this.random = new Random(seed);
    this.step = step;
  }

  @Override
  public synchronized Quote fetch(String symbol) {
    BigDecimal current = prices.get(symbol);
    if (current == null) {
      throw new IllegalArgumentException("Mã không tồn tại: " + symbol);
    }
    BigDecimal delta = BigDecimal.valueOf(random.nextDouble() * 2 - 1).setScale(4, RoundingMode.HALF_UP);
    BigDecimal next = current
        .multiply(BigDecimal.ONE.add(step.multiply(delta)))
        .setScale(2, RoundingMode.HALF_UP);
    if (next.signum() <= 0) {
      next = current;
    }
    prices.put(symbol, next);
    BigDecimal spread = new BigDecimal("0.05");
    return new Quote(symbol, next.subtract(spread).max(BigDecimal.ZERO), next.add(spread), 1000);
  }

  public synchronized BigDecimal currentPrice(String symbol) {
    BigDecimal price = prices.get(symbol);
    if (price == null) {
      throw new IllegalArgumentException("Mã không tồn tại: " + symbol);
    }
    return price;
  }
}
