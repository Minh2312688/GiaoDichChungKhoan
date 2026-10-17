package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.IMarketDataProvider;
import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.bot.BotController;
import com.tradingsimulator.ui.session.MarketSessionController;
import com.tradingsimulator.ui.session.MarketSessionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestMarketSessionController {

    @BeforeEach
    void reset() {
        ExchangeEngine.getInstance().reset(new BigDecimal("10000.00"));
    }

    @Test
    void refreshOnceMatchesPendingOrderAndNotifiesListener() {
        TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
        Quote fixedQuote = new Quote("ABC", new BigDecimal("10.00"), new BigDecimal("10.00"), 1000);
        IMarketDataProvider deterministicProvider = symbol -> fixedQuote;
        MarketAdapter adapter = new MarketAdapter(deterministicProvider);
        facade.setMarketAdapter(adapter);

        // Put a pending order that triggers when ask <= 10.00
        facade.submitPending(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 5, new BigDecimal("10.00")));
        assertEquals(0, facade.position("ABC").quantity());
        assertEquals(1, facade.pendingOrders().size());

        BotController botController = new BotController(facade);
        MarketSessionController controller = new MarketSessionController(
                facade, adapter, Set.of("ABC"), botController
        );

        AtomicReference<Quote> receivedQuote = new AtomicReference<>();
        controller.addListener(receivedQuote::set);

        controller.refreshOnce();

        assertNotNull(receivedQuote.get());
        assertEquals("ABC", receivedQuote.get().symbol());
        assertEquals(5, facade.position("ABC").quantity());
        assertTrue(facade.pendingOrders().isEmpty());
    }

    @Test
    void startDoesNotCreateSecondTimerWhenCalledRepeatedly() {
        TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
        MarketAdapter adapter = new MarketAdapter(symbol -> new Quote("ABC", BigDecimal.TEN, BigDecimal.TEN, 100));
        BotController botController = new BotController(facade);
        MarketSessionController controller = new MarketSessionController(
                facade, adapter, Set.of("ABC"), botController
        );

        controller.start(500);
        assertTrue(controller.isRunning());

        controller.start(1000); // Repeated call
        assertTrue(controller.isRunning());

        controller.stop();
        assertFalse(controller.isRunning());
    }
}
