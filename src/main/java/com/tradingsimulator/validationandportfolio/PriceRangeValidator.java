package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import java.math.BigDecimal;

/** Từ chối lệnh khi giá thị trường tham chiếu không dương. */
public final class PriceRangeValidator implements IValidator {
  @Override
  public void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice) {
    if (marketPrice.signum() <= 0) {
      throw new IllegalArgumentException("Giá tham chiếu phải dương");
    }
  }
}
