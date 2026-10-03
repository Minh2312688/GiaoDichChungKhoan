package com.tradingsimulator.model;
import java.math.BigDecimal; import java.time.Instant;
/** Bản ghi khớp lệnh bất biến (symbol, phía, khối lượng, giá, tổng tiền, thời điểm). Chủ sở hữu: dùng chung. */
public record Trade(String symbol, OrderSide side, int quantity, BigDecimal price, BigDecimal total, Instant timestamp) { public Trade { if (total == null) total = price.multiply(BigDecimal.valueOf(quantity)); if (timestamp == null) timestamp = Instant.now(); } }
