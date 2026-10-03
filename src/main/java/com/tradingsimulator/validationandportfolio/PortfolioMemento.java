package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Snapshot bất biến của toàn bộ phiên giao dịch (mẫu Memento).
 *
 * <p>Mọi collection đều được sao chép phòng vệ để bên ngoài không thể sửa trạng thái đã lưu.
 * {@code pendingOrders} chứa các lệnh LIMIT/STOP_LOSS đang chờ để phiên khôi phục vẫn giữ được
 * lệnh mở.</p>
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Memento).</p>
 */
public record PortfolioMemento(
    BigDecimal cash,
    Map<String, Position> positions,
    List<Trade> trades,
    Map<String, Quote> quotes,
    List<Order> pendingOrders) {

  public PortfolioMemento {
    positions = Map.copyOf(positions);
    trades = List.copyOf(trades);
    quotes = Map.copyOf(quotes);
    pendingOrders = List.copyOf(pendingOrders);
  }

  public PortfolioMemento(
      BigDecimal cash,
      Map<String, Position> positions,
      List<Trade> trades,
      Map<String, Quote> quotes) {
    this(cash, positions, trades, quotes, List.of());
  }
}
