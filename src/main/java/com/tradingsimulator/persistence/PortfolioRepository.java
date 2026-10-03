package com.tradingsimulator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.validationandportfolio.PortfolioMemento;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lưu và đọc snapshot danh mục dưới dạng JSON bằng Jackson.
 *
 * <p>Lớp này cố ý lưu một DTO nhỏ ({@link PortfolioState}) thay vì trực tiếp các record miền,
 * để định dạng trên đĩa ổn định và độc lập với nội bộ engine. Đây chỉ là lưu trữ phiên làm
 * việc: không dùng cơ sở dữ liệu hay mạng.</p>
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Memento).</p>
 */
public final class PortfolioRepository {
  private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  public PortfolioState capture(PortfolioMemento memento) {
    if (memento == null) {
      throw new IllegalArgumentException("Memento is required");
    }
    List<PositionState> positions = memento.positions().values().stream()
        .map(p -> new PositionState(p.symbol(), p.quantity(), p.averagePrice()))
        .collect(Collectors.toList());
    List<TradeState> trades = memento.trades().stream()
        .map(t -> new TradeState(t.symbol(), t.side().name(), t.quantity(), t.price(), t.total()))
        .collect(Collectors.toList());
    List<OrderState> pendingOrders = memento.pendingOrders().stream()
        .map(o -> new OrderState(o.symbol(), o.side().name(), o.type().name(), o.quantity(), o.price()))
        .collect(Collectors.toList());
    return new PortfolioState(memento.cash(), positions, trades, pendingOrders);
  }

  public PortfolioMemento toMemento(PortfolioState state, Map<String, Quote> quotes) {
    if (state == null) {
      throw new IllegalArgumentException("State is required");
    }
    Map<String, Position> positions = new HashMap<>();
    for (PositionState row : state.positions()) {
      positions.put(row.symbol(), new Position(row.symbol(), row.quantity(), row.averagePrice()));
    }
    List<Trade> trades = state.trades().stream()
        .map(row -> new Trade(row.symbol(), OrderSide.valueOf(row.side()), row.quantity(),
            row.price(), row.total(), null))
        .collect(Collectors.toList());
    List<Order> pendingOrders = state.pendingOrders().stream()
        .map(row -> new Order(row.symbol(), OrderSide.valueOf(row.side()), OrderType.valueOf(row.type()),
            row.quantity(), row.price()))
        .collect(Collectors.toList());
    Map<String, Quote> safeQuotes = quotes == null ? Map.of() : quotes;
    return new PortfolioMemento(state.cash(), positions, trades, safeQuotes, pendingOrders);
  }

  public void save(PortfolioState state, Path file) {
    if (state == null || file == null) {
      throw new IllegalArgumentException("State and file are required");
    }
    try {
      if (file.getParent() != null) {
        Files.createDirectories(file.getParent());
      }
      mapper.writeValue(file.toFile(), state);
    } catch (IOException error) {
      throw new IllegalStateException("Không lưu được danh mục vào " + file, error);
    }
  }

  public PortfolioState load(Path file) {
    if (file == null) {
      throw new IllegalArgumentException("File is required");
    }
    try {
      return mapper.readValue(file.toFile(), PortfolioState.class);
    } catch (IOException error) {
      throw new IllegalStateException("Không đọc được danh mục từ " + file, error);
    }
  }

  public BigDecimal cashOf(PortfolioState state) {
    return state.cash();
  }
}
