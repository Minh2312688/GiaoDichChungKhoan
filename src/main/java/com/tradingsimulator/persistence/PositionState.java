package com.tradingsimulator.persistence;

import java.math.BigDecimal;

/** Dòng vị thế có thể tuần tự hóa, dùng bởi {@link PortfolioRepository}. */
public record PositionState(String symbol, int quantity, BigDecimal averagePrice) { }
