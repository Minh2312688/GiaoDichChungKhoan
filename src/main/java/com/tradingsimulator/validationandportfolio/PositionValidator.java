package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;

/** Từ chối lệnh SELL vượt quá số lượng vị thế đang nắm giữ. */
public final class PositionValidator implements IValidator {
  @Override
  public void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice) {
    if (order.side() == OrderSide.SELL && position < order.quantity()) {
      throw new IllegalArgumentException("Không đủ vị thế để bán");
    }
  }
}
