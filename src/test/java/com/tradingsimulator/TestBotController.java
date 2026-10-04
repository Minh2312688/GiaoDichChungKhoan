package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.tradingengine.botstrategies.MovingAverageCrossoverStrategy;
import com.tradingsimulator.ui.bot.BotController;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm thử bộ điều khiển bot và luồng Strategy → Facade → giao dịch. */
class TestBotController {

  @BeforeEach
  void reset() {
    ExchangeEngine.getInstance().reset(new BigDecimal("10000"));
  }

  @Test
  void disabledBotStoresPricesButDoesNotTrade() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.setStrategy(new MovingAverageCrossoverStrategy(2));
    facade.setBotEnabled(false);
    BotController controller = new BotController(facade, 3, 1);

    facade.updateQuote(new Quote("ABC", new BigDecimal("10"), new BigDecimal("10"), 100));
    controller.onQuote(new Quote("ABC", new BigDecimal("11"), new BigDecimal("11"), 100));
    controller.onQuote(new Quote("ABC", new BigDecimal("12"), new BigDecimal("12"), 100));

    assertEquals(2, controller.prices("ABC").size());
    assertTrue(facade.trades().isEmpty());
    assertEquals(BotController.Status.DISABLED, controller.status());
  }

  @Test
  void enabledBotSubmitsStrategySignalThroughFacade() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.setStrategy(new MovingAverageCrossoverStrategy(2));
    facade.setBotEnabled(true);
    BotController controller = new BotController(facade, 10, 1);

    controller.onQuote(new Quote("ABC", new BigDecimal("10"), new BigDecimal("10"), 100));
    controller.onQuote(new Quote("ABC", new BigDecimal("11"), new BigDecimal("11"), 100));
    facade.updateQuote(new Quote("ABC", new BigDecimal("12"), new BigDecimal("12"), 100));
    Optional<Trade> result = controller.onQuote(new Quote("ABC", new BigDecimal("12"), new BigDecimal("12"), 100));

    assertTrue(result.isPresent());
    assertEquals(OrderSide.BUY, result.orElseThrow().side());
    assertEquals(1, result.orElseThrow().quantity());
    assertEquals(1, facade.position("ABC").quantity());
    assertEquals(BotController.Status.ORDER_SUBMITTED, controller.status());
  }

  @Test
  void priceHistoryIsBounded() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    BotController controller = new BotController(facade, 2, 1);

    controller.onQuote(new Quote("ABC", new BigDecimal("10"), new BigDecimal("10"), 100));
    controller.onQuote(new Quote("ABC", new BigDecimal("11"), new BigDecimal("11"), 100));
    controller.onQuote(new Quote("ABC", new BigDecimal("12"), new BigDecimal("12"), 100));

    assertEquals(2, controller.prices("ABC").size());
    assertEquals(new BigDecimal("11"), controller.prices("ABC").get(0));
  }

  @Test
  void enabledBotReportsWaitingWhenStrategyHasNoSignal() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.setStrategy(prices -> null);
    facade.setBotEnabled(true);
    BotController controller = new BotController(facade, 10, 1);

    controller.onQuote(new Quote("ABC", new BigDecimal("10"), new BigDecimal("10"), 100));

    assertEquals(BotController.Status.WAITING_SIGNAL, controller.status());
  }
}
