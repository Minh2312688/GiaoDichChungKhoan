package com.tradingsimulator;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.ui.components.PortfolioPanel;
import com.tradingsimulator.ui.screens.DashboardPanel;
import com.tradingsimulator.ui.screens.HistoryScreen;
import com.tradingsimulator.ui.screens.PendingOrdersScreen;
import com.tradingsimulator.ui.screens.TradingPanel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestScreens {

    @Test
    void dashboardDisplaysPendingCountAndSummaryData() {
        DashboardPanel dashboard = new DashboardPanel();
        dashboard.showPendingCount(1);
        dashboard.showCash("10000.00");
        dashboard.showPositions(Map.of(
                "ABC", new Position("ABC", 10, new BigDecimal("9.50"))));
        dashboard.showBotStatus("ĐANG CHỜ");
        dashboard.showQuotes(List.of(new Quote("ABC", new BigDecimal("10.00"), new BigDecimal("10.50"), 100)));

        assertTrue(dashboard.getPendingLabel().getText().contains("Lệnh chờ: 1"));
        assertTrue(dashboard.getCashLabel().getText().contains("10000.00"));
        assertEquals("Mã đang nắm giữ: 1", dashboard.getPositionsLabel().getText());
        assertEquals(1, dashboard.getPortfolioPanel().getTable().getRowCount());
        assertEquals("ABC", dashboard.getPortfolioPanel().getTable().getValueAt(0, 0));
        assertEquals(10, dashboard.getPortfolioPanel().getTable().getValueAt(0, 1));
        assertEquals("9.50", dashboard.getPortfolioPanel().getTable().getValueAt(0, 2));
        assertTrue(dashboard.getBotStatusLabel().getText().contains("ĐANG CHỜ"));
    }

    @Test
    void portfolioPanelUpdatesHoldingsAndShowsEmptyState() {
        PortfolioPanel portfolio = new PortfolioPanel();
        portfolio.showPositions(Map.of(
                "XYZ", new Position("XYZ", 3, new BigDecimal("20.00")),
                "ABC", new Position("ABC", 8, new BigDecimal("10.00")),
                "ZERO", new Position("ZERO", 0, BigDecimal.ZERO)));

        assertEquals("Mã chứng khoán", portfolio.getTable().getColumnName(0));
        assertEquals("Số lượng đang nắm giữ", portfolio.getTable().getColumnName(1));
        assertEquals("ABC", portfolio.getTable().getValueAt(0, 0));
        assertEquals("XYZ", portfolio.getTable().getValueAt(1, 0));
        assertEquals(2, portfolio.getTable().getRowCount());
        assertFalse(portfolio.getTable().isCellEditable(0, 0));

        portfolio.showPositions(Map.of(
                "ZERO", new Position("ZERO", 0, BigDecimal.ZERO)));
        assertEquals(0, portfolio.getTable().getRowCount());
    }

    @Test
    void pendingOrdersScreenDisplaysLoadedOrders() {
        PendingOrdersScreen screen = new PendingOrdersScreen();
        Order order = new Order("ABC", OrderSide.BUY, OrderType.LIMIT, 5, new BigDecimal("10.00"));
        screen.showOrders(List.of(order));

        assertEquals(1, screen.getTable().getRowCount());
        assertEquals("ABC", screen.getTable().getValueAt(0, 0));
        assertEquals("MUA", screen.getTable().getValueAt(0, 1));
        assertEquals("LIMIT", screen.getTable().getValueAt(0, 2));
        assertEquals(5, screen.getTable().getValueAt(0, 3));
        assertEquals("10.00", screen.getTable().getValueAt(0, 4));
    }

    @Test
    void historyScreenDisplaysTrades() {
        HistoryScreen screen = new HistoryScreen();
        Trade trade = new Trade("ABC", OrderSide.BUY, 2, new BigDecimal("10.00"), new BigDecimal("20.00"), null);
        screen.showTrades(List.of(trade));

        assertEquals(1, screen.getTable().getRowCount());
        assertEquals("ABC", screen.getTable().getValueAt(0, 1));
    }

    @Test
    void tradingPanelProvidesOrderForm() {
        TradingPanel panel = new TradingPanel();
        assertNotNull(panel.getOrderForm());
    }
}
