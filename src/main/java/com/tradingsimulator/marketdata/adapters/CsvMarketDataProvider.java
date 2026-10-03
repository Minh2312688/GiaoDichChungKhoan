package com.tradingsimulator.marketdata.adapters;

import com.tradingsimulator.model.Quote;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Bộ cung cấp dữ liệu thị trường đọc từ tệp CSV dạng {@code symbol,price} (từ classpath hoặc
 * bất kỳ luồng đầu vào nào). Áp một mức spread cố định quanh giá tham chiếu.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Adapter).</p>
 */
public final class CsvMarketDataProvider implements IMarketDataProvider {
  private final Map<String, BigDecimal> prices = new HashMap<>();
  private final BigDecimal spread;

  public CsvMarketDataProvider(InputStream csv) {
    this(csv, new BigDecimal("0.05"));
  }

  public CsvMarketDataProvider(InputStream csv, BigDecimal spread) {
    if (csv == null) {
      throw new IllegalArgumentException("Cần có luồng CSV");
    }
    if (spread == null || spread.signum() < 0) {
      throw new IllegalArgumentException("Spread không được âm");
    }
    this.spread = spread;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.toLowerCase().startsWith("symbol")) {
          continue;
        }
        String[] parts = trimmed.split(",");
        if (parts.length < 2) {
          throw new IllegalArgumentException("Dòng CSV sai định dạng: " + trimmed);
        }
        String symbol = parts[0].trim();
        BigDecimal price = new BigDecimal(parts[1].trim());
        if (symbol.isEmpty() || price.signum() <= 0) {
          throw new IllegalArgumentException("Mục CSV không hợp lệ: " + trimmed);
        }
        prices.put(symbol, price);
      }
    } catch (IOException error) {
      throw new IllegalArgumentException("Không đọc được tệp CSV dữ liệu thị trường", error);
    }
    if (prices.isEmpty()) {
      throw new IllegalArgumentException("Tệp CSV dữ liệu thị trường rỗng");
    }
  }

  @Override
  public Quote fetch(String symbol) {
    BigDecimal price = prices.get(symbol);
    if (price == null) {
      throw new IllegalArgumentException("Mã không tồn tại: " + symbol);
    }
    return new Quote(symbol, price.subtract(spread).max(BigDecimal.ZERO), price.add(spread), 1000);
  }
}
