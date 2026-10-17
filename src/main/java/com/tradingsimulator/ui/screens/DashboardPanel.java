package com.tradingsimulator.ui.screens;

import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.components.PriceBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final PriceBoardPanel priceBoard;
    private final JLabel cashLabel;
    private final JLabel positionsLabel;
    private final JLabel pendingLabel;
    private final JLabel botStatusLabel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        priceBoard = new PriceBoardPanel();
        add(priceBoard, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Tổng quan tài khoản & Bot"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        cashLabel = new JLabel("Tiền mặt: -");
        positionsLabel = new JLabel("Vị thế: -");
        pendingLabel = new JLabel("Lệnh chờ: 0");
        botStatusLabel = new JLabel("Trạng thái bot: TẮT");

        summaryPanel.add(cashLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        summaryPanel.add(positionsLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        summaryPanel.add(pendingLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        summaryPanel.add(botStatusLabel);

        add(summaryPanel, BorderLayout.EAST);
    }

    public void showQuotes(List<Quote> quotes) {
        if (quotes != null) {
            for (Quote q : quotes) {
                priceBoard.setQuote(q.symbol(), q.bid().toPlainString(), q.ask().toPlainString());
            }
        }
    }

    public void setQuote(String symbol, String bid, String ask) {
        priceBoard.setQuote(symbol, bid, ask);
    }

    public void showCash(String cash) {
        cashLabel.setText("Tiền mặt: " + cash);
    }

    public void showPositions(String positions) {
        positionsLabel.setText("Vị thế: " + positions);
    }

    public void showPendingCount(int count) {
        pendingLabel.setText("Lệnh chờ: " + count);
    }

    public void showBotStatus(String status) {
        botStatusLabel.setText("Trạng thái bot: " + status);
    }

    public PriceBoardPanel getPriceBoard() {
        return priceBoard;
    }

    public JLabel getCashLabel() {
        return cashLabel;
    }

    public JLabel getPositionsLabel() {
        return positionsLabel;
    }

    public JLabel getPendingLabel() {
        return pendingLabel;
    }

    public JLabel getBotStatusLabel() {
        return botStatusLabel;
    }
}
