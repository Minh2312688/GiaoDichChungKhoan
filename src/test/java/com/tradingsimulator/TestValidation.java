package com.tradingsimulator;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.validationandportfolio.QuantityLimitValidator;
import com.tradingsimulator.validationandportfolio.ValidationChain;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Chủ sở hữu: Thành viên 4 (mẫu Chain of Responsibility). */
class TestValidation {

  @Test
  void defaultChainRejectsInsufficientBalance() {
    ValidationChain chain = ValidationChain.defaultChain();
    assertThrows(IllegalArgumentException.class, () -> chain.validate(
        new Order("ABC", OrderSide.BUY, OrderType.MARKET, 2, null),
        new BigDecimal("5"), 0, new BigDecimal("10")));
  }

  @Test
  void customValidatorCanBePluggedIn() {
    AtomicInteger called = new AtomicInteger();
    ValidationChain chain = new ValidationChain()
        .add((order, cash, position, price) -> called.incrementAndGet());
    chain.validate(new Order("ABC", OrderSide.BUY, OrderType.MARKET, 1, null),
        new BigDecimal("100"), 0, new BigDecimal("10"));
    assertEquals(1, called.get());
  }

  @Test
  void chainShortCircuitsOnFirstFailure() {
    AtomicInteger second = new AtomicInteger();
    ValidationChain chain = new ValidationChain()
        .add((order, cash, position, price) -> {
          throw new IllegalArgumentException("first");
        })
        .add((order, cash, position, price) -> second.incrementAndGet());
    assertThrows(IllegalArgumentException.class, () -> chain.validate(
        new Order("ABC", OrderSide.BUY, OrderType.MARKET, 1, null),
        new BigDecimal("100"), 0, new BigDecimal("10")));
    assertEquals(0, second.get());
  }

  @Test
  void quantityLimitValidatorEnforcesLimit() {
    QuantityLimitValidator limit = new QuantityLimitValidator(10);
    assertThrows(IllegalArgumentException.class, () -> limit.validate(
        new Order("ABC", OrderSide.BUY, OrderType.MARKET, 11, null),
        new BigDecimal("1000"), 0, new BigDecimal("1")));
  }
}
