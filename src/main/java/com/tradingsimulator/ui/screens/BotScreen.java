package com.tradingsimulator.ui.screens;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.tradingengine.botstrategies.RsiThresholdStrategy;
import com.tradingsimulator.ui.bot.BotController;
import com.tradingsimulator.ui.components.BotPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class BotScreen extends JPanel {
    private final BotPanel botPanel;
    private final RsiThresholdStrategy rsiIndicator = new RsiThresholdStrategy();

    public BotScreen(TradingFacade facade) {
        this(new BotPanel(facade));
    }

    public BotScreen(BotPanel botPanel) {
        if (botPanel == null) {
            throw new IllegalArgumentException("botPanel must not be null");
        }
        this.botPanel = botPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(botPanel);
        add(wrapper, BorderLayout.CENTER);
    }

    public void showQuote(Quote quote, List<BigDecimal> prices, BotController.Status status) {
        if (status != null) {
            botPanel.showBotStatus(status.label());
        }
        if (prices != null && !prices.isEmpty()) {
            double rsi = rsiIndicator.estimateRsi(prices);
            if (Double.isFinite(rsi)) {
                botPanel.showRsi(rsi);
            } else {
                botPanel.clearRsi();
            }
        } else {
            botPanel.clearRsi();
        }
    }

    public BotPanel getBotPanel() {
        return botPanel;
    }

    public boolean isBotEnabled() {
        return botPanel.isBotEnabled();
    }

    public void setBotEnabled(boolean enabled) {
        botPanel.setBotEnabled(enabled);
    }

    public JLabel getRsiLabel() {
        return botPanel.getRsiLabel();
    }

    public JLabel getBotStatusLabel() {
        return botPanel.getBotStatusLabel();
    }

    public JLabel getBotStateLabel() {
        return botPanel.getBotStateLabel();
    }
}
