package com.tradingsimulator.model;
import java.math.BigDecimal;
/** Lệnh giao dịch bất biến (symbol, phía, loại, khối lượng, giá). Chủ sở hữu: dùng chung. */
public record Order(String symbol, OrderSide side, OrderType type, int quantity, BigDecimal price) {
  public Order { if (symbol == null || symbol.isBlank() || side == null || type == null || quantity <= 0) throw new IllegalArgumentException("Order symbol/side/type/quantity are invalid"); if (type != OrderType.MARKET && (price == null || price.signum() <= 0)) throw new IllegalArgumentException("Limit or stop price is required"); }
}
