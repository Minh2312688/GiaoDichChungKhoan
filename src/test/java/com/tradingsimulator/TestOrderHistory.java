package com.tradingsimulator;

import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.ui.components.OrderHistoryPanel;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Chủ sở hữu: Thành viên 3 (mẫu Observer). */
class TestOrderHistory {

  @Test
  void displaysExecutedTrades() {
    OrderHistoryPanel panel = new OrderHistoryPanel();
    panel.showTrades(List.of(
        new Trade("ABC", OrderSide.BUY, 5, new BigDecimal("10.00"), new BigDecimal("50.00"), null),
        new Trade("ABC", OrderSide.SELL, 2, new BigDecimal("11.00"), new BigDecimal("22.00"), null)));

    assertEquals(2, panel.getTable().getRowCount());
    assertEquals("ABC", panel.getTable().getValueAt(0, 1));
    assertEquals("MUA", panel.getTable().getValueAt(0, 2));
    assertEquals("22.00", panel.getTable().getValueAt(1, 5));
  }

  @Test
  void addTradeAppendsRow() {
    OrderHistoryPanel panel = new OrderHistoryPanel();
    panel.addTrade(new Trade("XYZ", OrderSide.SELL, 1, new BigDecimal("5.00"), new BigDecimal("5.00"), null));
    assertEquals(1, panel.getTable().getRowCount());
    assertEquals("XYZ", panel.getTable().getValueAt(0, 1));
  }
}
