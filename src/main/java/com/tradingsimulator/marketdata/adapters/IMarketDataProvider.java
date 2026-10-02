package com.tradingsimulator.marketdata.adapters; import com.tradingsimulator.model.Quote; public interface IMarketDataProvider { Quote fetch(String symbol); }
