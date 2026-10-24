package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.ui.MainWindow;
import com.tradingsimulator.ui.components.*;
import com.tradingsimulator.model.*;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Chủ sở hữu: giao diện dùng chung (Thành viên 1, 3). */
class TestUi {
  @Test void priceBoardDisplaysQuotes(){
    PriceBoardPanel panel=new PriceBoardPanel();
    panel.setQuote("ABC", "10.00", "10.10");
    assertEquals(1, panel.getTable().getRowCount());
    assertEquals("ABC", panel.getTable().getValueAt(0, 0));
    assertEquals("10.10", panel.getTable().getValueAt(0, 2));
    assertEquals("Mã", panel.getTable().getColumnName(0));
    assertEquals("Giá mua", panel.getTable().getColumnName(1));
    assertEquals("Giá bán", panel.getTable().getColumnName(2));
  }

  @Test void orderFormEmitsOrderInput(){
    OrderFormPanel panel=new OrderFormPanel(List.of("ABC", "XYZ"));
    panel.setQuantity("5");
    assertEquals("ABC", panel.getSymbol());
    assertEquals(5, panel.getQuantity());
    assertNotNull(panel.getSubmitButton());
    assertEquals("Gửi lệnh", panel.getSubmitButton().getText());
  }

  @Test void orderFormCreatesMarketOrderFromInput(){
    OrderFormPanel panel=new OrderFormPanel(List.of("ABC"));
    panel.setQuantity("5");
    panel.setSide(OrderSide.SELL);
    Order order=panel.createOrder();
    assertEquals("ABC", order.symbol());
    assertEquals(OrderSide.SELL, order.side());
    assertEquals(OrderType.MARKET, order.type());
    assertEquals(5, order.quantity());
  }

  @Test void orderFormCreatesLimitAndStopLossOrders(){
    OrderFormPanel panel=new OrderFormPanel(List.of("ABC"));
    panel.setQuantity("5");
    panel.setPrice("9.50");

    panel.setOrderType(OrderType.LIMIT);
    Order limitOrder=panel.createOrder();
    assertEquals(OrderType.LIMIT, limitOrder.type());
    assertEquals(new BigDecimal("9.50"), limitOrder.price());
    assertTrue(panel.getPriceField().isEnabled());

    panel.setOrderType(OrderType.STOP_LOSS);
    Order stopLossOrder=panel.createOrder();
    assertEquals(OrderType.STOP_LOSS, stopLossOrder.type());
    assertEquals(new BigDecimal("9.50"), stopLossOrder.price());

    panel.setOrderType(OrderType.MARKET);
    assertEquals(OrderType.MARKET, panel.createOrder().type());
    assertFalse(panel.getPriceField().isEnabled());
  }

  @Test void orderFormUsesAvailableSymbolsAndQueuesNonMarketOrders(){
    OrderFormPanel panel=new OrderFormPanel(List.of("XYZ", "ABC"));
    assertEquals("ABC", panel.getSymbol());
    panel.setSymbol("XYZ");
    assertEquals("XYZ", panel.getSymbol());

    panel.setOrderType(OrderType.LIMIT);
    panel.setPrice("10.00");
    panel.onSubmit(order -> assertEquals(OrderType.LIMIT, order.type()));
    panel.getSubmitButton().doClick();
    assertEquals("Đã thêm lệnh chờ", panel.getStatusLabel().getText());
  }

  @Test void botPanelDisplaysFormattedRsi(){
    BotPanel panel = new BotPanel(new TradingFacade(ExchangeEngine.getInstance()));
    panel.showRsi(35.4249);
    assertEquals("RSI: 35.42", panel.getRsiLabel().getText());
  }

  @Test void orderHistoryDisplaysVietnameseHeadersAndSide(){
    OrderHistoryPanel panel = new OrderHistoryPanel();
    panel.addTrade(new Trade("ABC", OrderSide.BUY, 1, new java.math.BigDecimal("10"),
        new java.math.BigDecimal("10"), null));

    assertEquals("Thời gian", panel.getTable().getColumnName(0));
    assertEquals("Mã", panel.getTable().getColumnName(1));
    assertEquals("Chiều", panel.getTable().getColumnName(2));
    assertEquals("MUA", panel.getTable().getValueAt(0, 2));
  }

  @Test void botPanelReflectsEnabledStateAndStatus(){
    BotPanel panel = new BotPanel(new TradingFacade(ExchangeEngine.getInstance()));
    assertFalse(panel.isBotEnabled());

    panel.setBotEnabled(true);
    panel.showBotStatus("Đã đặt lệnh");

    assertTrue(panel.isBotEnabled());
    assertEquals("Bot: BẬT", panel.getBotStateLabel().getText());
    assertEquals("Đã đặt lệnh", panel.getBotStatusLabel().getText());
  }

  @Test
  void mainWindowRegistersAllSixScreensWithoutChildFrames() {
    MainWindow window = new MainWindow();
    assertNotNull(window.getScreenManager());
    for (com.tradingsimulator.ui.navigation.ScreenId id : com.tradingsimulator.ui.navigation.ScreenId.values()) {
      assertNotNull(window.getScreenManager().getComponent(id), "Screen " + id + " should be registered");
    }
    int initialWindows = java.awt.Window.getWindows().length;
    for (com.tradingsimulator.ui.navigation.ScreenId id : com.tradingsimulator.ui.navigation.ScreenId.values()) {
      window.getScreenManager().show(id);
      assertEquals(id, window.getScreenManager().currentScreen());
    }
    assertEquals(initialWindows, java.awt.Window.getWindows().length);
    window.dispose();
  }
}
