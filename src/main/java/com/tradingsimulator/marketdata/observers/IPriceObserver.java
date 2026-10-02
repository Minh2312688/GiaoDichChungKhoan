package com.tradingsimulator.marketdata.observers; import com.tradingsimulator.model.Quote; public interface IPriceObserver { void onPrice(Quote quote); }
