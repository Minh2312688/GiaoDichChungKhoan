package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Chuỗi trách nhiệm (Chain of Responsibility) cho việc kiểm tra hợp lệ lệnh.
 *
 * <p>Các quy tắc là những {@link IValidator} có thể cắm thêm, được chạy theo thứ tự đăng ký.
 * Lỗi đầu tiên sẽ dừng chuỗi bằng cách ném {@link IllegalArgumentException}.</p>
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Chain of Responsibility).</p>
 */
public final class ValidationChain {
  private final List<IValidator> validators = new ArrayList<>();

  public ValidationChain add(IValidator validator) {
    if (validator == null) {
      throw new IllegalArgumentException("Validator không được để trống");
    }
    validators.add(validator);
    return this;
  }

  public void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice) {
    for (IValidator validator : validators) {
      validator.validate(order, cash, position, marketPrice);
    }
  }

  public static ValidationChain defaultChain() {
    return new ValidationChain()
        .add(new BalanceValidator())
        .add(new PositionValidator())
        .add(new QuantityLimitValidator(1_000_000))
        .add(new PriceRangeValidator());
  }
}
