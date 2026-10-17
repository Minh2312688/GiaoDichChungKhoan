package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.bot.BotController;
import com.tradingsimulator.ui.screens.BotScreen;
import com.tradingsimulator.ui.screens.SettingsPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestBotScreen {

    @BeforeEach
    void reset() {
        ExchangeEngine.getInstance().reset(new BigDecimal("10000.00"));
    }

    @Test
    void botScreenRetainsStrategyAndReflectsRsiOnQuote() {
        TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
        BotScreen botScreen = new BotScreen(facade);

        botScreen.setBotEnabled(true);
        assertTrue(botScreen.isBotEnabled());

        List<BigDecimal> prices = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            prices.add(new BigDecimal(100 + (i % 5)));
        }

        Quote quote = new Quote("ABC", new BigDecimal("104.00"), new BigDecimal("104.50"), 100);
        botScreen.showQuote(quote, prices, BotController.Status.WAITING_SIGNAL);

        assertTrue(botScreen.isBotEnabled());
        assertNotNull(botScreen.getBotStatusLabel().getText());
        assertTrue(botScreen.getRsiLabel().getText().startsWith("RSI: "));
        assertFalse(botScreen.getRsiLabel().getText().equals("RSI: -"));
    }

    @Test
    void settingsPanelDefaultsAndValidation() {
        SettingsPanel settings = new SettingsPanel();

        assertEquals(2000, settings.refreshDelay());
        assertNotNull(settings.themeMode());
        assertTrue(settings.themeMode().equalsIgnoreCase("dark") || settings.themeMode().equalsIgnoreCase("tối"));

        settings.setRefreshDelay(3000);
        assertEquals(3000, settings.refreshDelay());

        assertThrows(IllegalArgumentException.class, () -> settings.setRefreshDelay(0));
        assertThrows(IllegalArgumentException.class, () -> settings.setRefreshDelay(-500));

        settings.setThemeMode("light");
        assertEquals("light", settings.themeMode().toLowerCase());
    }
}
