package com.tradingsimulator.persistence;

import java.math.BigDecimal;

/** DTO bền vững cho một lệnh đang chờ trong snapshot danh mục. */
public record OrderState(
    String symbol,
    String side,
    String type,
    int quantity,
    BigDecimal price) { }
