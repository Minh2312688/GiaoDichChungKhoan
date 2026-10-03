package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.persistence.PortfolioRepository;
import com.tradingsimulator.persistence.PortfolioState;
import com.tradingsimulator.reporting.PerformanceReport;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Chủ sở hữu: Thành viên 2 (mẫu Memento); kiểm thử lưu snapshot. */
class TestPersistence {

  @BeforeEach
  void reset() {
    ExchangeEngine.getInstance().reset(new BigDecimal("1000"));
  }

  @Test
  void savesAndLoadsPortfolioStateRoundTrip() throws Exception {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("25.00"), new BigDecimal("25.00"), 1000));
    facade.submitMarketOrder("ABC", OrderSide.BUY, 10);

    PortfolioRepository repository = new PortfolioRepository();
    PortfolioState state = repository.capture(facade.snapshot());
    Path file = Files.createTempFile("portfolio", ".json");
    try {
      repository.save(state, file);

      PortfolioState loaded = repository.load(file);
      assertEquals(0, new BigDecimal("750.00").compareTo(loaded.cash()));
      assertEquals(1, loaded.positions().size());
      assertEquals(10, loaded.positions().get(0).quantity());

      facade.restore(repository.toMemento(loaded, facade.quotes()));
      assertEquals(10, facade.position("ABC").quantity());
    } finally {
      Files.deleteIfExists(file);
    }
  }

  @Test
  void savesAndRestoresPendingOrders() throws Exception {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("25.00"), new BigDecimal("25.00"), 1000));
    facade.submitPending(new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 10, new BigDecimal("24.00")));

    PortfolioRepository repository = new PortfolioRepository();
    Path file = Files.createTempFile("portfolio-pending", ".json");
    try {
      repository.save(repository.capture(facade.snapshot()), file);

      PortfolioState loaded = repository.load(file);
      assertEquals(1, loaded.pendingOrders().size());
      assertEquals(OrderType.LIMIT.name(), loaded.pendingOrders().get(0).type());
      assertEquals(10, loaded.pendingOrders().get(0).quantity());

      facade.restore(repository.toMemento(loaded, facade.quotes()));
      assertEquals(1, facade.pendingOrders().size());
      assertEquals(new BigDecimal("24.00"), facade.pendingOrders().get(0).price());
    } finally {
      Files.deleteIfExists(file);
    }
  }

  @Test
  void performanceReportTracksEquity() {
    TradingFacade facade = new TradingFacade(ExchangeEngine.getInstance());
    facade.updateQuote(new Quote("ABC", new BigDecimal("25.00"), new BigDecimal("25.00"), 1000));
    facade.submitMarketOrder("ABC", OrderSide.BUY, 10);

    PerformanceReport report = PerformanceReport.from(facade);
    assertEquals(0, new BigDecimal("250.00").compareTo(report.totalBuy()));
    assertEquals(0, new BigDecimal("750.00").compareTo(report.cash()));
    assertEquals(0, new BigDecimal("250.00").compareTo(report.marketValue()));
    assertEquals(0, new BigDecimal("1000.00").compareTo(report.equity()));
    assertEquals(0, new BigDecimal("-250.00").compareTo(report.netRealized()));
  }
}
