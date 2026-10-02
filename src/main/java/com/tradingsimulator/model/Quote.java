package com.tradingsimulator.model;
import java.math.BigDecimal;
public record Quote(String symbol, BigDecimal bid, BigDecimal ask, long volume) {
  public Quote { if (symbol == null || symbol.isBlank() || bid == null || ask == null || bid.signum() < 0 || ask.signum() < 0) throw new IllegalArgumentException("Quote symbol/prices are invalid"); }
  public BigDecimal last() { return bid.add(ask).divide(BigDecimal.valueOf(2)); }
}
