package com.tradingsimulator.ui.screens;

import com.tradingsimulator.ui.components.OrderFormPanel;
import com.tradingsimulator.model.Quote;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingPanel extends JPanel {
    private final OrderFormPanel orderForm;
    private final PriceChart priceChart;

    public TradingPanel() {
        this(List.of());
    }

    public TradingPanel(Collection<String> symbols) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        orderForm = new OrderFormPanel(symbols);
        priceChart = new PriceChart();
        orderForm.addSymbolSelectionListener(priceChart::selectSymbol);
        if (!symbols.isEmpty()) {
            priceChart.selectSymbol(orderForm.getSymbol());
        }

        JPanel marketPanel = new JPanel(new BorderLayout(0, 12));
        marketPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel marketTitle = new JLabel("Biểu đồ giá");
        marketTitle.setFont(marketTitle.getFont().deriveFont(Font.BOLD, 16f));
        marketPanel.add(marketTitle, BorderLayout.NORTH);
        marketPanel.add(priceChart, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, marketPanel, orderForm);
        splitPane.setResizeWeight(0.68);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        add(splitPane, BorderLayout.CENTER);
    }

    public OrderFormPanel getOrderForm() {
        return orderForm;
    }

    public void showQuote(Quote quote) {
        priceChart.showQuote(quote);
    }

    private static final class PriceChart extends JPanel {
        private final Map<String, List<BigDecimal>> history = new HashMap<>();
        private final Map<String, Quote> quotes = new HashMap<>();
        private String selectedSymbol;
        private Quote selectedQuote;

        private PriceChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(560, 390));
        }

        private void selectSymbol(String symbol) {
            selectedSymbol = symbol;
            selectedQuote = quotes.get(symbol);
            repaint();
        }

        private void showQuote(Quote quote) {
            quotes.put(quote.symbol(), quote);
            history.computeIfAbsent(quote.symbol(), ignored -> new java.util.ArrayList<>())
                    .add(quote.last());
            List<BigDecimal> values = history.get(quote.symbol());
            if (values.size() > 80) values.remove(0);
            if (selectedSymbol == null || selectedSymbol.equals(quote.symbol())) {
                selectedSymbol = quote.symbol();
                selectedQuote = quote;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 18;
                int top = 52;
                int width = Math.max(1, getWidth() - 36);
                int height = Math.max(1, getHeight() - 105);
                g.setColor(new Color(0x2b, 0x31, 0x3a));
                for (int i = 0; i <= 4; i++) {
                    int y = top + height * i / 4;
                    g.drawLine(left, y, left + width, y);
                }

                List<BigDecimal> values = history.getOrDefault(selectedSymbol, List.of());
                g.setColor(new Color(0xea, 0xec, 0xef));
                g.setFont(getFont().deriveFont(Font.BOLD, 22f));
                String symbol = selectedSymbol == null ? "Chờ dữ liệu" : selectedSymbol;
                g.drawString(symbol, left, 30);
                if (selectedQuote != null && selectedQuote.symbol().equals(selectedSymbol)) {
                    g.setFont(getFont().deriveFont(12f));
                    g.setColor(new Color(0x84, 0x8e, 0x9c));
                    g.drawString("BID " + selectedQuote.bid().toPlainString()
                            + "    ASK " + selectedQuote.ask().toPlainString(), left + 150, 28);
                }
                if (values.size() < 2) {
                    g.setColor(new Color(0x84, 0x8e, 0x9c));
                    g.drawString("Đang thu thập dữ liệu giá...", left, top + height / 2);
                    return;
                }

                BigDecimal minimum = values.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maximum = values.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
                BigDecimal range = maximum.subtract(minimum);
                if (range.signum() == 0) range = BigDecimal.ONE;
                Path2D line = new Path2D.Double();
                for (int i = 0; i < values.size(); i++) {
                    double x = left + (double) i * width / (values.size() - 1);
                    double ratio = values.get(i).subtract(minimum).doubleValue() / range.doubleValue();
                    double y = top + height - ratio * height;
                    if (i == 0) line.moveTo(x, y); else line.lineTo(x, y);
                }
                BigDecimal change = values.get(values.size() - 1).subtract(values.get(0));
                g.setColor(change.signum() >= 0
                        ? new Color(0x0e, 0xcb, 0x81) : new Color(0xf6, 0x46, 0x5d));
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(line);
                g.setFont(getFont().deriveFont(12f));
                g.setColor(new Color(0x84, 0x8e, 0x9c));
                g.drawString("Biến động giá gần nhất", left, top + height + 28);
            } finally {
                g.dispose();
            }
        }
    }
}
