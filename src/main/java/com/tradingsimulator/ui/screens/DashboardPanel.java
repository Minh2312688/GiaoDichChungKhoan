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

import com.tradingsimulator.ui.components.MarketIndicesPanel;

public class DashboardPanel extends JPanel {
    private final PriceBoardPanel priceBoard;
    private final PortfolioPanel portfolioPanel;
    private final MarketIndicesPanel indicesPanel;
    private final JLabel cashLabel;
    private final JLabel positionsLabel;
    private final JLabel pendingLabel;
    private final JLabel botStatusLabel;
    private Consumer<String> tradeRequestListener;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(0x11, 0x13, 0x17));

        priceBoard = new PriceBoardPanel();
        portfolioPanel = new PortfolioPanel();

        // 1. Dải chỉ số thị trường ở đỉnh (Ảnh 2)
        indicesPanel = new MarketIndicesPanel();

        // 2. Thanh công cụ tìm kiếm và lọc tab chuẩn SSI iBoard
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(new Color(0x16, 0x19, 0x1f));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0x23, 0x27, 0x30)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Bên trái: Ô tìm kiếm CK
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("TIM:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 10));
        searchIcon.setForeground(new Color(0x84, 0x8e, 0x9c));
        JTextField searchField = new JTextField(12);
        searchField.setBackground(new Color(0x22, 0x26, 0x30));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x32, 0x38, 0x44)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                priceBoard.filter(searchField.getText());
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });
        searchBox.add(searchIcon);
        searchBox.add(searchField);

        // Ở giữa: Các tabs (VN30, HOSE, HNX, UPCOM, Phái sinh...)
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabsPanel.setOpaque(false);
        String[] tabs = {"Tất cả", "VN30", "HNX30", "HOSE", "HNX", "UPCOM", "Phái sinh"};
        java.util.List<JButton> tabButtons = new java.util.ArrayList<>();
        for (int i = 0; i < tabs.length; i++) {
            String tab = tabs[i];
            JButton tabBtn = new JButton(tab);
            tabBtn.setFont(new Font("Segoe UI", i == 0 ? Font.BOLD : Font.PLAIN, 12));
            tabBtn.setForeground(i == 0 ? new Color(0x0e, 0xcb, 0x81) : new Color(0x84, 0x8e, 0x9c));
            tabBtn.setBackground(i == 0 ? new Color(0x1e, 0x2e, 0x2a) : new Color(0x16, 0x19, 0x1f));
            tabBtn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(i == 0 ? new Color(0x0e, 0xcb, 0x81) : new Color(0x23, 0x27, 0x30)),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
            tabBtn.setFocusPainted(false);
            tabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabBtn.addActionListener(e -> {
                for (JButton b : tabButtons) {
                    boolean active = b == tabBtn;
                    b.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
                    b.setForeground(active ? new Color(0x0e, 0xcb, 0x81) : new Color(0x84, 0x8e, 0x9c));
                    b.setBackground(active ? new Color(0x1e, 0x2e, 0x2a) : new Color(0x16, 0x19, 0x1f));
                    b.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(active ? new Color(0x0e, 0xcb, 0x81) : new Color(0x23, 0x27, 0x30)),
                            BorderFactory.createEmptyBorder(4, 10, 4, 10)
                    ));
                }
                searchField.setText("");
                priceBoard.filterCategory(tab);
            });
            tabButtons.add(tabBtn);
            tabsPanel.add(tabBtn);
        }

        // Bên phải: Nút Đặt lệnh xanh lá nổi bật (Ảnh 2)
        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBox.setOpaque(false);
        JButton quickOrderBtn = new JButton("+ Đặt lệnh");
        quickOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quickOrderBtn.setBackground(new Color(0x0e, 0xcb, 0x81));
        quickOrderBtn.setForeground(Color.BLACK);
        quickOrderBtn.setFocusPainted(false);
        quickOrderBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quickOrderBtn.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        quickOrderBtn.addActionListener(e -> {
            if (tradeRequestListener != null) {
                int selectedRow = priceBoard.getTable().getSelectedRow();
                String targetSymbol = "AAPL";
                if (selectedRow >= 0) {
                    int modelRow = priceBoard.getTable().convertRowIndexToModel(selectedRow);
                    targetSymbol = priceBoard.getTable().getModel().getValueAt(modelRow, 0).toString();
                }
                tradeRequestListener.accept(targetSymbol);
            }
        });
        actionBox.add(quickOrderBtn);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(tabsPanel, BorderLayout.CENTER);
        toolbar.add(actionBox, BorderLayout.EAST);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(indicesPanel, BorderLayout.NORTH);
        northPanel.add(toolbar, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        JSplitPane marketAndPortfolio = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, priceBoard, portfolioPanel);
        marketAndPortfolio.setResizeWeight(0.68);
        marketAndPortfolio.setContinuousLayout(true);
        marketAndPortfolio.setBorder(null);
        marketAndPortfolio.setBackground(new Color(0x11, 0x13, 0x17));
        add(marketAndPortfolio, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
                        BorderFactory.createTitledBorder("TỔNG QUAN")),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        summaryPanel.setBackground(new Color(0x16, 0x19, 0x1f));
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

    public MarketIndicesPanel getIndicesPanel() {
        return indicesPanel;
    }

    public void onTradeRequest(Consumer<String> listener) {
        Consumer<String> tradeRequest = Objects.requireNonNull(listener, "listener");
        this.tradeRequestListener = tradeRequest;
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
