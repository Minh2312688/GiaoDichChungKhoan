package com.tradingsimulator.persistence;

import java.math.BigDecimal;
import java.util.List;

/** Snapshot danh mục có thể tuần tự hóa, được {@link PortfolioRepository} lưu dưới dạng JSON. */
public record PortfolioState(BigDecimal cash, List<PositionState> positions, List<TradeState> trades) { }
