package com.tradingsimulator.persistence;

import java.math.BigDecimal;

/** Dòng khớp lệnh có thể tuần tự hóa, dùng bởi {@link PortfolioRepository}. */
public record TradeState(String symbol, String side, int quantity, BigDecimal price, BigDecimal total) { }
