package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import java.math.BigDecimal;

/** Từ chối lệnh có khối lượng vượt quá hạn mức cho phép trên mỗi lệnh. */
public final class QuantityLimitValidator implements IValidator {
  private final int maxQuantity;

  public QuantityLimitValidator(int maxQuantity) {
    if (maxQuantity < 1) {
      throw new IllegalArgumentException("Hạn mức khối lượng phải dương");
    }
    this.maxQuantity = maxQuantity;
  }

  @Override
  public void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice) {
    if (order.quantity() > maxQuantity) {
      throw new IllegalArgumentException("Khối lượng vượt hạn mức cho phép");
    }
  }
}
