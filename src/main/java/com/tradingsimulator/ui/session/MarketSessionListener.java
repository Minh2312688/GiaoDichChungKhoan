package com.tradingsimulator.ui.session;

import com.tradingsimulator.model.Quote;

@FunctionalInterface
public interface MarketSessionListener {
    void onQuote(Quote quote);
}
