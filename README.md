# Trading Simulator MVP

Java 17/Maven offline simulator with a `TradingFacade`, deterministic market adapter, validation chain, order factory, bot strategies, portfolio memento, and Swing UI.

Run with `mvn clean test` and `mvn exec:java`. All prices and money use `BigDecimal`; the mock market requires no network.
