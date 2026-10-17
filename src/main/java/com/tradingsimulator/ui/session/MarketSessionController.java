package com.tradingsimulator.ui.session;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.marketdata.adapters.MarketAdapter;
import com.tradingsimulator.marketdata.observers.PriceObserver;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.bot.BotController;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MarketSessionController {
    private final TradingFacade facade;
    private final MarketAdapter adapter;
    private final Set<String> symbols;
    private final BotController botController;
    private final List<MarketSessionListener> listeners;
    private Timer timer;

    public MarketSessionController(TradingFacade facade, MarketAdapter adapter, Set<String> symbols, BotController botController) {
        if (facade == null || adapter == null || symbols == null) {
            throw new IllegalArgumentException("Dependencies cannot be null");
        }
        this.facade = facade;
        this.adapter = adapter;
        this.symbols = new LinkedHashSet<>(symbols);
        this.botController = botController;
        this.listeners = new ArrayList<>();

        this.adapter.addObserver(new PriceObserver(this::handleQuote));
    }

    private void handleQuote(Quote quote) {
        Runnable task = () -> {
            try {
                facade.updateQuote(quote);
            } catch (Exception ignored) {
            }

            try {
                facade.matchPending(quote.symbol());
            } catch (Exception ignored) {
                // Keep pending orders if validation/matching fails; do not break timer/session
            }

            if (botController != null) {
                try {
                    botController.onQuote(quote);
                } catch (Exception ignored) {
                }
            }

            for (MarketSessionListener listener : new ArrayList<>(listeners)) {
                try {
                    listener.onQuote(quote);
                } catch (Exception ignored) {
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (Exception e) {
                task.run();
            }
        }
    }

    public void addListener(MarketSessionListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void refreshOnce() {
        for (String symbol : symbols) {
            try {
                adapter.refresh(symbol);
            } catch (Exception ignored) {
            }
        }
    }

    public void start(int delayMillis) {
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("Delay must be positive");
        }
        if (timer != null && timer.isRunning()) {
            return;
        }
        if (timer != null) {
            timer.setDelay(delayMillis);
            timer.start();
            return;
        }
        timer = new Timer(delayMillis, e -> refreshOnce());
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public boolean isRunning() {
        return timer != null && timer.isRunning();
    }

    public TradingFacade getFacade() {
        return facade;
    }

    public MarketAdapter getAdapter() {
        return adapter;
    }

    public Set<String> getSymbols() {
        return Collections.unmodifiableSet(symbols);
    }

    public BotController getBotController() {
        return botController;
    }
}
