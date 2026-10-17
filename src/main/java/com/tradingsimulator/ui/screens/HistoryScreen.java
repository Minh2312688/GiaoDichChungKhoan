package com.tradingsimulator.ui.screens;

import com.tradingsimulator.model.Trade;
import com.tradingsimulator.ui.components.OrderHistoryPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HistoryScreen extends JPanel {
    private final OrderHistoryPanel historyPanel;

    public HistoryScreen() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        historyPanel = new OrderHistoryPanel();
        add(historyPanel, BorderLayout.CENTER);
    }

    public void showTrades(List<Trade> trades) {
        historyPanel.showTrades(trades);
    }

    public JTable getTable() {
        return historyPanel.getTable();
    }

    public OrderHistoryPanel getHistoryPanel() {
        return historyPanel;
    }
}
