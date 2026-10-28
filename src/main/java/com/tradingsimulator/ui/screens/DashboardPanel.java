package com.tradingsimulator.ui.screens;

import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.ui.components.PortfolioPanel;
import com.tradingsimulator.ui.components.PriceBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DashboardPanel extends JPanel {
    private final PriceBoardPanel priceBoard;
    private final PortfolioPanel portfolioPanel;
    private final JLabel cashLabel;
    private final JLabel positionsLabel;
    private final JLabel pendingLabel;
    private final JLabel botStatusLabel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        priceBoard = new PriceBoardPanel();
        portfolioPanel = new PortfolioPanel();
        JSplitPane marketAndPortfolio = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, priceBoard, portfolioPanel);
        marketAndPortfolio.setResizeWeight(0.58);
        marketAndPortfolio.setContinuousLayout(true);
        marketAndPortfolio.setBorder(null);
        add(marketAndPortfolio, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
                        BorderFactory.createTitledBorder("TỔNG QUAN")),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        summaryPanel.setBackground(new Color(0x1e, 0x23, 0x29));
        summaryPanel.setPreferredSize(new Dimension(220, 0));

        cashLabel = new JLabel("Tiền mặt: -");
        positionsLabel = new JLabel("Mã đang nắm giữ: 0");
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

    public void showPositions(Map<String, Position> positions) {
        portfolioPanel.showPositions(positions);
        long heldSymbols = positions.values().stream()
                .filter(position -> position.quantity() > 0)
                .count();
        positionsLabel.setText("Mã đang nắm giữ: " + heldSymbols);
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

    public PortfolioPanel getPortfolioPanel() {
        return portfolioPanel;
    }

    public void onTradeRequest(Consumer<String> listener) {
        Consumer<String> tradeRequest = Objects.requireNonNull(listener, "listener");
        priceBoard.onSymbolDoubleClick(tradeRequest);
        portfolioPanel.onSymbolDoubleClick(tradeRequest);
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
