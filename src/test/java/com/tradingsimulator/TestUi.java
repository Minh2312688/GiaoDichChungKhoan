package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.ui.components.*;
import com.tradingsimulator.model.*;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import static org.junit.jupiter.api.Assertions.*;

/** Chủ sở hữu: giao diện dùng chung (Thành viên 1, 3). */
class TestUi {
  @Test void priceBoardDisplaysQuotes(){
    PriceBoardPanel panel=new PriceBoardPanel();
    panel.setQuote("ABC", "10.00", "10.10");
    assertEquals(1, panel.getTable().getRowCount());
    assertEquals("ABC", panel.getTable().getValueAt(0, 0));
    assertEquals("10.10", panel.getTable().getValueAt(0, 2));
  }

  @Test void orderFormEmitsOrderInput(){
    OrderFormPanel panel=new OrderFormPanel();
    panel.setSymbol("ABC");
    panel.setQuantity("5");
    assertEquals("ABC", panel.getSymbol());
    assertEquals(5, panel.getQuantity());
    assertNotNull(panel.getSubmitButton());
  }

  @Test void orderFormCreatesMarketOrderFromInput(){
    OrderFormPanel panel=new OrderFormPanel();
    panel.setSymbol("ABC");
    panel.setQuantity("5");
    panel.setSide(OrderSide.SELL);
    Order order=panel.createOrder();
    assertEquals("ABC", order.symbol());
    assertEquals(OrderSide.SELL, order.side());
    assertEquals(OrderType.MARKET, order.type());
    assertEquals(5, order.quantity());
  }

  @Test void botPanelDisplaysFormattedRsi(){
    BotPanel panel = new BotPanel(new TradingFacade(ExchangeEngine.getInstance()));
    panel.showRsi(35.4249);
    assertEquals("RSI: 35.42", panel.getRsiLabel().getText());
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
}
