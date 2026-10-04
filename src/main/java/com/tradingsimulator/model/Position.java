package com.tradingsimulator.model;
import java.math.BigDecimal;
/** Vị thế bất biến (symbol, khối lượng, giá trung bình). Chủ sở hữu: dùng chung. */
public record Position(String symbol, int quantity, BigDecimal averagePrice) { public Position { if (symbol == null || symbol.isBlank() || quantity < 0 || averagePrice == null) throw new IllegalArgumentException("Vị thế không hợp lệ"); } }
