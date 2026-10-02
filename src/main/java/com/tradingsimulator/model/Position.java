package com.tradingsimulator.model;
import java.math.BigDecimal;
public record Position(String symbol, int quantity, BigDecimal averagePrice) { public Position { if (symbol == null || symbol.isBlank() || quantity < 0 || averagePrice == null) throw new IllegalArgumentException("Position is invalid"); } }
