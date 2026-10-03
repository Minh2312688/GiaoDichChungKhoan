package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;

/** Từ chối lệnh BUY có giá trị vượt quá số tiền mặt hiện có. */
public final class BalanceValidator implements IValidator {
  @Override
  public void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice) {
    if (order.side() == OrderSide.BUY) {
      BigDecimal required = marketPrice.multiply(BigDecimal.valueOf(order.quantity()));
      if (cash.compareTo(required) < 0) {
        throw new IllegalArgumentException("Số dư không đủ");
      }
    }
  }
}
