package com.tradingsimulator.ui.screens;

import com.tradingsimulator.ui.components.OrderFormPanel;
import com.tradingsimulator.model.Quote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

/**
 * Màn hình Đặt lệnh kết hợp Biểu đồ kỹ thuật nến Nhật (Candlestick Chart) chuẩn TradingView / KIS.
 * Hỗ trợ chọn/đổi mã chứng khoán trực tiếp bằng cách nhấn vào tiêu đề mã trên góc biểu đồ
 * hoặc chọn từ form đặt lệnh bên phải.
 */
public class TradingPanel extends JPanel {
    private final OrderFormPanel orderForm;
    private final CandlestickPriceChart priceChart;

    public TradingPanel() {
        this(List.of());
    }

    public TradingPanel(Collection<String> symbols) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(new Color(0x11, 0x13, 0x17));

        Set<String> allSymbols = new LinkedHashSet<>(symbols);
        allSymbols.addAll(List.of("AAPL", "BTC", "ETH", "ACB", "BID", "BSR", "CTG", "FPT", "GAS", "GVR", "HDB", "HPG", "MBB", "MWG", "SSI", "TCB", "VIC", "VNM", "VPB"));

        orderForm = new OrderFormPanel(allSymbols);
        priceChart = new CandlestickPriceChart(allSymbols, orderForm::setSymbol);
        orderForm.addSymbolSelectionListener(priceChart::selectSymbol);
        if (!allSymbols.isEmpty()) {
            priceChart.selectSymbol(orderForm.getSymbol());
        }

        JPanel marketPanel = new JPanel(new BorderLayout(0, 10));
        marketPanel.setBackground(new Color(0x16, 0x19, 0x1f));
        marketPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        marketPanel.add(priceChart, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, marketPanel, orderForm);
        splitPane.setResizeWeight(0.68);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setBackground(new Color(0x11, 0x13, 0x17));
        add(splitPane, BorderLayout.CENTER);
    }

    public OrderFormPanel getOrderForm() {
        return orderForm;
    }

    public void showQuote(Quote quote) {
        priceChart.showQuote(quote);
    }

    private static final class Candle {
        double open;
        double high;
        double low;
        double close;
        long volume;

        Candle(double o, double h, double l, double c, long v) {
            this.open = o;
            this.high = h;
            this.low = l;
            this.close = c;
            this.volume = v;
        }

        void update(double price) {
            if (price > high) high = price;
            if (price < low) low = price;
            close = price;
            volume += (long) (100 + Math.random() * 250);
        }

        boolean isUp() {
            return close >= open;
        }
    }

    private static final class CandlestickPriceChart extends JPanel {
        private static final Color BG_CHART = new Color(0x13, 0x17, 0x22);
        private static final Color GRID_COLOR = new Color(0x24, 0x28, 0x35);
        private static final Color GREEN_CANDLE = new Color(0x08, 0x99, 0x81);
        private static final Color RED_CANDLE = new Color(0xf2, 0x36, 0x45);
        private static final Color MA_LINE_COLOR = new Color(0x29, 0x62, 0xff);
        private static final Color TEXT_MUTED = new Color(0x78, 0x7b, 0x86);
        private static final Color TEXT_LIGHT = new Color(0xd1, 0xd4, 0xdc);

        private final Map<String, List<Candle>> candlesBySymbol = new HashMap<>();
        private final Map<String, Quote> quotes = new HashMap<>();
        private final java.util.function.Consumer<String> onSymbolChange;
        private final JComboBox<String> symbolDropdown;
        private final JPanel topControlsPanel;
        private String selectedSymbol;
        private Quote selectedQuote;
        private int tickCounter = 0;

        private CandlestickPriceChart(Collection<String> availableSymbols, java.util.function.Consumer<String> onSymbolChange) {
            this.onSymbolChange = onSymbolChange;
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(BG_CHART);
            setPreferredSize(new Dimension(580, 420));

            // Thanh công cụ chọn mã chứng khoán trực tiếp ở góc trên sơ đồ
            topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            topControlsPanel.setOpaque(false);

            symbolDropdown = new JComboBox<>(new Vector<>(availableSymbols));
            symbolDropdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
            symbolDropdown.setBackground(new Color(0x2a, 0x2e, 0x39));
            symbolDropdown.setForeground(Color.WHITE);
            symbolDropdown.setFocusable(false);
            symbolDropdown.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            symbolDropdown.setToolTipText("Nhấn vào đây để chọn loại chứng khoán cần xem biểu đồ");
            symbolDropdown.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x36, 0x3c, 0x4e)),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));

            symbolDropdown.addActionListener(e -> {
                String chosen = (String) symbolDropdown.getSelectedItem();
                if (chosen != null && !chosen.equals(selectedSymbol)) {
                    selectSymbol(chosen);
                    if (this.onSymbolChange != null) {
                        this.onSymbolChange.accept(chosen);
                    }
                }
            });

            JLabel timeframeLabel = new JLabel("1D \u00B7 HOSE");
            timeframeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            timeframeLabel.setForeground(new Color(0x29, 0x62, 0xff));

            topControlsPanel.add(symbolDropdown);
            topControlsPanel.add(timeframeLabel);
            add(topControlsPanel, BorderLayout.NORTH);
        }

        private void selectSymbol(String symbol) {
            this.selectedSymbol = symbol;
            this.selectedQuote = quotes.get(symbol);
            if (symbolDropdown != null && !Objects.equals(symbolDropdown.getSelectedItem(), symbol)) {
                symbolDropdown.setSelectedItem(symbol);
            }
            if (!candlesBySymbol.containsKey(symbol)) {
                generateInitialCandles(symbol, selectedQuote != null ? selectedQuote.last().doubleValue() : 100.0);
            }
            repaint();
        }

        private void showQuote(Quote quote) {
            quotes.put(quote.symbol(), quote);
            double price = quote.last().doubleValue();

            List<Candle> list = candlesBySymbol.computeIfAbsent(quote.symbol(),
                    s -> generateInitialCandlesList(price));

            if (list.isEmpty()) {
                list.add(new Candle(price, price, price, price, 1500));
            } else {
                Candle current = list.get(list.size() - 1);
                current.update(price);

                tickCounter++;
                if (tickCounter % 5 == 0) {
                    double nextOpen = current.close;
                    list.add(new Candle(nextOpen, nextOpen, nextOpen, nextOpen, 500));
                    if (list.size() > 45) {
                        list.remove(0);
                    }
                }
            }

            if (selectedSymbol == null || selectedSymbol.equals(quote.symbol())) {
                selectedSymbol = quote.symbol();
                selectedQuote = quote;
            }
            repaint();
        }

        private List<Candle> generateInitialCandlesList(double currentPrice) {
            List<Candle> list = new ArrayList<>();
            double p = currentPrice * 0.88;
            for (int i = 0; i < 35; i++) {
                double open = p;
                double change = (Math.random() - 0.46) * (currentPrice * 0.025);
                double close = open + change;
                double high = Math.max(open, close) + Math.random() * (currentPrice * 0.012);
                double low = Math.min(open, close) - Math.random() * (currentPrice * 0.012);
                long vol = (long) (5000 + Math.random() * 25000);
                list.add(new Candle(open, high, low, close, vol));
                p = close;
            }
            list.add(new Candle(p, Math.max(p, currentPrice), Math.min(p, currentPrice), currentPrice, 12000));
            return list;
        }

        private void generateInitialCandles(String symbol, double currentPrice) {
            candlesBySymbol.put(symbol, generateInitialCandlesList(currentPrice));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 1. Header thông tin O H L C và chỉ báo MA
                int topBarHeight = 56;
                paintHeaderStats(g, w);

                // Khu vực vẽ biểu đồ nến và volume
                int left = 14;
                int rightMargin = 64; // Dành cho trục giá bên phải
                int chartW = w - left - rightMargin;
                int chartTop = topBarHeight + 6;
                int volHeight = (int) (h * 0.20);
                int priceChartH = h - chartTop - volHeight - 20;

                List<Candle> candles = candlesBySymbol.get(selectedSymbol);
                if (candles == null || candles.isEmpty()) {
                    g.setColor(TEXT_MUTED);
                    g.drawString("Đang tải dữ liệu biểu đồ nến...", left + 20, chartTop + 60);
                    return;
                }

                // Tính toán min/max price
                double minPrice = Double.MAX_VALUE;
                double maxPrice = Double.MIN_VALUE;
                long maxVolume = 1;

                for (Candle c : candles) {
                    if (c.low < minPrice) minPrice = c.low;
                    if (c.high > maxPrice) maxPrice = c.high;
                    if (c.volume > maxVolume) maxVolume = c.volume;
                }
                double pricePad = (maxPrice - minPrice) * 0.08;
                if (pricePad <= 0) pricePad = 1.0;
                minPrice -= pricePad;
                maxPrice += pricePad;
                double priceRange = maxPrice - minPrice;

                // 2. Vẽ lưới ngang dọc (Grid)
                paintGrid(g, left, chartTop, chartW, priceChartH, minPrice, maxPrice);

                // 3. Vẽ nến Nhật (Candlesticks) và Khối lượng (Volume bars)
                int n = candles.size();
                double slotWidth = (double) chartW / n;
                double candleWidth = Math.max(3.0, slotWidth * 0.68);

                List<Point> maPoints = new ArrayList<>();
                int maPeriod = 9;

                for (int i = 0; i < n; i++) {
                    Candle c = candles.get(i);
                    double cx = left + i * slotWidth + slotWidth / 2.0;

                    int yHigh = (int) (chartTop + (1.0 - (c.high - minPrice) / priceRange) * priceChartH);
                    int yLow = (int) (chartTop + (1.0 - (c.low - minPrice) / priceRange) * priceChartH);
                    int yOpen = (int) (chartTop + (1.0 - (c.open - minPrice) / priceRange) * priceChartH);
                    int yClose = (int) (chartTop + (1.0 - (c.close - minPrice) / priceRange) * priceChartH);

                    Color candleCol = c.isUp() ? GREEN_CANDLE : RED_CANDLE;
                    g.setColor(candleCol);

                    // Bấc nến
                    g.setStroke(new BasicStroke(1.2f));
                    g.drawLine((int) cx, yHigh, (int) cx, yLow);

                    // Thân nến
                    int bodyTop = Math.min(yOpen, yClose);
                    int bodyHeight = Math.max(2, Math.abs(yOpen - yClose));
                    int bodyLeft = (int) (cx - candleWidth / 2.0);

                    g.fillRect(bodyLeft, bodyTop, (int) candleWidth, bodyHeight);

                    // Cột Volume
                    int volY = chartTop + priceChartH + 10;
                    int vH = (int) ((double) c.volume / maxVolume * (volHeight - 14));
                    g.setColor(new Color(candleCol.getRed(), candleCol.getGreen(), candleCol.getBlue(), 120));
                    g.fillRect(bodyLeft, volY + (volHeight - vH), (int) candleWidth, vH);

                    // Tính điểm MA9
                    if (i >= maPeriod - 1) {
                        double sum = 0;
                        for (int k = i - maPeriod + 1; k <= i; k++) {
                            sum += candles.get(k).close;
                        }
                        double maVal = sum / maPeriod;
                        int maY = (int) (chartTop + (1.0 - (maVal - minPrice) / priceRange) * priceChartH);
                        maPoints.add(new Point((int) cx, maY));
                    }
                }

                // 4. Vẽ đường Moving Average (MA9)
                if (maPoints.size() > 1) {
                    g.setColor(MA_LINE_COLOR);
                    g.setStroke(new BasicStroke(1.8f));
                    Path2D maPath = new Path2D.Double();
                    maPath.moveTo(maPoints.get(0).x, maPoints.get(0).y);
                    for (int i = 1; i < maPoints.size(); i++) {
                        maPath.lineTo(maPoints.get(i).x, maPoints.get(i).y);
                    }
                    g.draw(maPath);
                }

                // 5. Thước đo trục giá bên phải
                paintRightAxis(g, left + chartW, chartTop, priceChartH, minPrice, maxPrice,
                        candles.get(n - 1).close, candles.get(n - 1).isUp());

            } finally {
                g.dispose();
            }
        }

        private void paintHeaderStats(Graphics2D g, int w) {
            int left = 175; // Bắt đầu sau dropdown chọn mã
            List<Candle> list = candlesBySymbol.get(selectedSymbol);
            if (list != null && !list.isEmpty()) {
                Candle last = list.get(list.size() - 1);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                Color statusColor = last.isUp() ? GREEN_CANDLE : RED_CANDLE;
                double diff = last.close - last.open;
                double diffPct = (diff / last.open) * 100.0;
                String sign = diff >= 0 ? "+" : "";
                String diffStr = String.format(Locale.US, "%s%.2f (%s%.2f%%)", sign, diff, sign, diffPct);

                int x = left;
                g.setColor(TEXT_MUTED);
                g.drawString("O ", x, 22); x += 12;
                g.setColor(statusColor);
                g.drawString(String.format(Locale.US, "%.2f", last.open), x, 22); x += 44;

                g.setColor(TEXT_MUTED);
                g.drawString("H ", x, 22); x += 12;
                g.setColor(statusColor);
                g.drawString(String.format(Locale.US, "%.2f", last.high), x, 22); x += 44;

                g.setColor(TEXT_MUTED);
                g.drawString("L ", x, 22); x += 12;
                g.setColor(statusColor);
                g.drawString(String.format(Locale.US, "%.2f", last.low), x, 22); x += 44;

                g.setColor(TEXT_MUTED);
                g.drawString("C ", x, 22); x += 12;
                g.setColor(statusColor);
                g.drawString(String.format(Locale.US, "%.2f", last.close) + "  " + diffStr, x, 22);

                // Dòng thứ hai: Khối lượng và MA9
                g.setColor(TEXT_MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString("Khối lượng ", 14, 46);
                g.setColor(statusColor);
                g.drawString(String.format(Locale.US, "%,d", last.volume), 75, 46);

                g.setColor(MA_LINE_COLOR);
                g.drawString("MA 9 close 0 ", 160, 46);
                g.drawString(String.format(Locale.US, "%.2f", last.close * 0.995), 235, 46);
            }
        }

        private void paintGrid(Graphics2D g, int x, int y, int w, int h, double min, double max) {
            g.setColor(GRID_COLOR);
            g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{2, 3}, 0));

            int rows = 5;
            for (int i = 0; i <= rows; i++) {
                int gy = y + i * h / rows;
                g.drawLine(x, gy, x + w, gy);
            }

            int cols = 6;
            for (int i = 0; i <= cols; i++) {
                int gx = x + i * w / cols;
                g.drawLine(gx, y, gx, y + h);
            }
        }

        private void paintRightAxis(Graphics2D g, int x, int y, int h, double min, double max,
                                   double currentPrice, boolean isUp) {
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(TEXT_MUTED);

            int steps = 5;
            for (int i = 0; i <= steps; i++) {
                double p = max - i * (max - min) / steps;
                int py = y + i * h / steps;
                g.drawString(String.format(Locale.US, "%.2f", p), x + 6, py + 4);
            }

            int tagY = (int) (y + (1.0 - (currentPrice - min) / (max - min)) * h);
            Color tagCol = isUp ? GREEN_CANDLE : RED_CANDLE;
            g.setColor(tagCol);
            g.fillRect(x + 2, tagY - 9, 58, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g.drawString(String.format(Locale.US, "%.2f", currentPrice), x + 6, tagY + 4);
        }
    }
}
