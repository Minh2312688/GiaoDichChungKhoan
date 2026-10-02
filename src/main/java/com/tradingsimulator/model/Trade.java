package com.tradingsimulator.model;
import java.math.BigDecimal; import java.time.Instant;
public record Trade(String symbol, OrderSide side, int quantity, BigDecimal price, BigDecimal total, Instant timestamp) { public Trade { if (total == null) total = price.multiply(BigDecimal.valueOf(quantity)); if (timestamp == null) timestamp = Instant.now(); } }
