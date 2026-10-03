package com.tradingsimulator.persistence;

import java.math.BigDecimal;
import java.util.List;

/** Snapshot danh mục có thể tuần tự hóa, được {@link PortfolioRepository} lưu dưới dạng JSON. */
public record PortfolioState(
    BigDecimal cash,
    List<PositionState> positions,
    List<TradeState> trades,
    List<OrderState> pendingOrders) {

  public PortfolioState {
    positions = positions == null ? List.of() : List.copyOf(positions);
    trades = trades == null ? List.of() : List.copyOf(trades);
    pendingOrders = pendingOrders == null ? List.of() : List.copyOf(pendingOrders);
  }

  /** Tương thích với snapshot cũ chưa có pending orders. */
  public PortfolioState(BigDecimal cash, List<PositionState> positions, List<TradeState> trades) {
    this(cash, positions, trades, List.of());
  }
}
