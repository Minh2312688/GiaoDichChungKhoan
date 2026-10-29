package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Dải hiển thị chỉ số thị trường (VNINDEX, VN30, HNX30, ...) theo chuẩn giao diện SSI iBoard.
 * Biểu đồ mini sparkline và các chỉ số biến động trực tiếp (live) theo nhịp giao dịch thị trường.
 */
public class MarketIndicesPanel extends JPanel {
    private static final Color BG_DARK = new Color(0x11, 0x13, 0x17);
    private static final Color CARD_BG = new Color(0x16, 0x19, 0x1f);
    private static final Color BORDER_COLOR = new Color(0x23, 0x27, 0x30);
    private static final Color GREEN = new Color(0x0e, 0xcb, 0x81);
    private static final Color RED = new Color(0xf6, 0x46, 0x5d);
    private static final Color YELLOW = new Color(0xf0, 0xb9, 0x0b);
    private static final Color TEXT_LIGHT = new Color(0xea, 0xec, 0xef);
    private static final Color TEXT_MUTED = new Color(0x84, 0x8e, 0x9c);

    private final IndexState vnIndexState;
    private final IndexState vn30State;
    private final IndexState hnx30State;

    private final JLabel vnIndexChangeLabel;
    private final JLabel vn30ChangeLabel;
    private final JLabel hnx30ChangeLabel;

    private final JLabel vnIndexBreadthLabel;
    private final JLabel vn30BreadthLabel;
    private final JLabel hnx30BreadthLabel;

    private final JLabel rowVnIndexPoints;
    private final JLabel rowVnIndexChange;
    private final JLabel rowVn30Points;
    private final JLabel rowVn30Change;
    private final JLabel rowHnx30Points;
    private final JLabel rowHnx30Change;

    private final MiniSparkline vnIndexChart;
    private final MiniSparkline vn30Chart;
    private final MiniSparkline hnx30Chart;

    public MarketIndicesPanel() {
        setLayout(new GridLayout(1, 4, 6, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(4, 6, 6, 6));
        setPreferredSize(new Dimension(0, 115));

        // Khởi tạo trạng thái ban đầu
        vnIndexState = new IndexState(1754.53, -12.12, -0.69, 81, 52, 218,
                new double[]{1766, 1764, 1761, 1759, 1757, 1752, 1755, 1751, 1754.53});
        vn30State = new IndexState(1875.48, -14.74, -0.78, 4, 2, 24,
                new double[]{1890, 1888, 1883, 1878, 1872, 1870, 1874, 1871, 1875.48});
        hnx30State = new IndexState(435.58, -4.08, -0.93, 5, 4, 18,
                new double[]{440, 439, 437, 436, 434, 432, 435, 433, 435.58});

        // 1. VNINDEX Card
        vnIndexChangeLabel = new JLabel();
        vnIndexBreadthLabel = new JLabel();
        vnIndexChart = new MiniSparkline(vnIndexState.history, RED);
        add(createIndexCard("VNINDEX", vnIndexChangeLabel, "355.985 CP  \u00B7  8,131.591 T\u1EF7",
                vnIndexChart, vnIndexBreadthLabel));

        // 2. VN30 Card
        vn30ChangeLabel = new JLabel();
        vn30BreadthLabel = new JLabel();
        vn30Chart = new MiniSparkline(vn30State.history, RED);
        add(createIndexCard("VN30", vn30ChangeLabel, "141.061 CP  \u00B7  4,456.094 T\u1EF7",
                vn30Chart, vn30BreadthLabel));

        // 3. HNX30 Card
        hnx30ChangeLabel = new JLabel();
        hnx30BreadthLabel = new JLabel();
        hnx30Chart = new MiniSparkline(hnx30State.history, RED);
        add(createIndexCard("HNX30", hnx30ChangeLabel, "13.245 CP  \u00B7  251.971 T\u1EF7",
                hnx30Chart, hnx30BreadthLabel));

        // 4. Bảng tổng hợp các chỉ số
        rowVnIndexPoints = new JLabel();
        rowVnIndexChange = new JLabel();
        rowVn30Points = new JLabel();
        rowVn30Change = new JLabel();
        rowHnx30Points = new JLabel();
        rowHnx30Change = new JLabel();
        add(createSummaryCard());

        renderAll();
    }

    private JPanel createIndexCard(String name, JLabel changeLabel, String volume,
                                   MiniSparkline chart, JLabel breadthLabel) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 4, 8)
        ));

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLbl = new JLabel(name);
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 12f));
        titleLbl.setForeground(TEXT_LIGHT);

        changeLabel.setFont(changeLabel.getFont().deriveFont(Font.BOLD, 11f));

        top.add(titleLbl, BorderLayout.WEST);
        top.add(changeLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Chart
        card.add(chart, BorderLayout.CENTER);

        // Bottom Volume & Breadth
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel volLbl = new JLabel(volume);
        volLbl.setFont(volLbl.getFont().deriveFont(Font.PLAIN, 9f));
        volLbl.setForeground(TEXT_MUTED);

        breadthLabel.setFont(breadthLabel.getFont().deriveFont(Font.BOLD, 9f));

        bottom.add(volLbl, BorderLayout.NORTH);
        bottom.add(breadthLabel, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSummaryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JPanel header = new JPanel(new GridLayout(1, 5));
        header.setOpaque(false);
        header.add(headerLabel("Ch\u1EC9 s\u1ED1"));
        header.add(headerLabel("\u0110i\u1EC3m"));
        header.add(headerLabel("+/-"));
        header.add(headerLabel("KLGD (Tr)"));
        header.add(headerLabel("T\u0103ng/Gi\u1EA3m"));
        card.add(header, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(5, 1, 0, 1));
        rows.setOpaque(false);

        rows.add(indexRow("VNINDEX", rowVnIndexPoints, rowVnIndexChange, "355.98", "\u25B281 \u25BC218"));
        rows.add(indexRow("VN30", rowVn30Points, rowVn30Change, "141.06", "\u25B24 \u25BC24"));
        rows.add(indexRow("HNX30", rowHnx30Points, rowHnx30Change, "13.25", "\u25B25 \u25BC18"));
        rows.add(staticRow("VNXALL", "2,767.50", "-21.65", "356.97", "\u25B291 \u25BC258", RED));
        rows.add(staticRow("HNXINDEX", "257.33", "-2.53", "20.58", "\u25B247 \u25BC78", RED));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 9f));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JPanel indexRow(String name, JLabel pLbl, JLabel cLbl, String vol, String breadth) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setOpaque(false);

        JLabel l1 = new JLabel(name);
        l1.setFont(l1.getFont().deriveFont(Font.BOLD, 9f));
        l1.setForeground(TEXT_LIGHT);

        pLbl.setFont(pLbl.getFont().deriveFont(Font.PLAIN, 9f));
        cLbl.setFont(cLbl.getFont().deriveFont(Font.PLAIN, 9f));

        JLabel l4 = new JLabel(vol);
        l4.setFont(l4.getFont().deriveFont(Font.PLAIN, 9f));
        l4.setForeground(TEXT_MUTED);

        JLabel l5 = new JLabel(breadth);
        l5.setFont(l5.getFont().deriveFont(Font.PLAIN, 9f));
        l5.setForeground(TEXT_LIGHT);

        row.add(l1);
        row.add(pLbl);
        row.add(cLbl);
        row.add(l4);
        row.add(l5);
        return row;
    }

    private JPanel staticRow(String name, String points, String change, String vol, String breadth, Color col) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setOpaque(false);

        JLabel l1 = new JLabel(name);
        l1.setFont(l1.getFont().deriveFont(Font.BOLD, 9f));
        l1.setForeground(TEXT_LIGHT);

        JLabel l2 = new JLabel(points);
        l2.setFont(l2.getFont().deriveFont(Font.PLAIN, 9f));
        l2.setForeground(col);

        JLabel l3 = new JLabel(change);
        l3.setFont(l3.getFont().deriveFont(Font.PLAIN, 9f));
        l3.setForeground(col);

        JLabel l4 = new JLabel(vol);
        l4.setFont(l4.getFont().deriveFont(Font.PLAIN, 9f));
        l4.setForeground(TEXT_MUTED);

        JLabel l5 = new JLabel(breadth);
        l5.setFont(l5.getFont().deriveFont(Font.PLAIN, 9f));
        l5.setForeground(TEXT_LIGHT);

        row.add(l1);
        row.add(l2);
        row.add(l3);
        row.add(l4);
        row.add(l5);
        return row;
    }

    public void updateQuote(String symbol, double priceChangePercent) {
        // Cập nhật ngẫu nhiên hoặc theo biến động giá cổ phiếu
        double factor = Math.max(-1.5, Math.min(1.5, priceChangePercent));
        vnIndexState.step(factor * 0.4);
        vn30State.step(factor * 0.5);
        hnx30State.step(factor * 0.35);

        renderAll();
    }

    private void renderAll() {
        renderCard(vnIndexState, vnIndexChangeLabel, vnIndexBreadthLabel, vnIndexChart, rowVnIndexPoints, rowVnIndexChange);
        renderCard(vn30State, vn30ChangeLabel, vn30BreadthLabel, vn30Chart, rowVn30Points, rowVn30Change);
        renderCard(hnx30State, hnx30ChangeLabel, hnx30BreadthLabel, hnx30Chart, rowHnx30Points, rowHnx30Change);
    }

    private void renderCard(IndexState s, JLabel changeLbl, JLabel breadthLbl, MiniSparkline chart, JLabel rowP, JLabel rowC) {
        Color c = s.change >= 0 ? GREEN : RED;
        String sign = s.change >= 0 ? "+" : "";
        String changeStr = String.format(java.util.Locale.US, "%,.2f  %s%.2f (%s%.2f%%)", s.points, sign, s.change, sign, s.changePercent);
        changeLbl.setText(changeStr);
        changeLbl.setForeground(c);

        breadthLbl.setText(String.format("\u25B2 %d   \u25A0 %d   \u25BC %d", s.advances, s.unchanged, s.declines));
        chart.updateData(s.history, c);

        rowP.setText(String.format(java.util.Locale.US, "%,.2f", s.points));
        rowP.setForeground(c);
        rowC.setText(String.format(java.util.Locale.US, "%s%.2f", sign, s.change));
        rowC.setForeground(c);
    }

    private static class IndexState {
        double basePoints;
        double points;
        double change;
        double changePercent;
        int advances;
        int unchanged;
        int declines;
        final List<Double> history = new ArrayList<>();

        IndexState(double p, double ch, double chPct, int adv, int unc, int dec, double[] initial) {
            this.points = p;
            this.basePoints = p - ch;
            this.change = ch;
            this.changePercent = chPct;
            this.advances = adv;
            this.unchanged = unc;
            this.declines = dec;
            for (double d : initial) {
                history.add(d);
            }
        }

        void step(double delta) {
            double randomVariation = (Math.random() - 0.48) * 1.8;
            points = Math.round((points + delta + randomVariation) * 100.0) / 100.0;
            change = Math.round((points - basePoints) * 100.0) / 100.0;
            changePercent = Math.round((change / basePoints * 100.0) * 100.0) / 100.0;

            history.add(points);
            if (history.size() > 40) {
                history.remove(0);
            }

            if (delta > 0 && declines > 5) {
                advances++;
                declines--;
            } else if (delta < 0 && advances > 5) {
                advances--;
                declines++;
            }
        }
    }

    private static class MiniSparkline extends JPanel {
        private final List<Double> data = new ArrayList<>();
        private Color lineColor;

        public MiniSparkline(List<Double> initialData, Color lineColor) {
            if (initialData != null) {
                this.data.addAll(initialData);
            }
            this.lineColor = lineColor;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 36));
        }

        public void updateData(List<Double> newData, Color newColor) {
            this.data.clear();
            this.data.addAll(newData);
            this.lineColor = newColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.size() < 2) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (double v : data) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            if (max == min) {
                max += 1;
                min -= 1;
            }

            int w = getWidth() - 6;
            int h = getHeight() - 6;
            Path2D path = new Path2D.Double();

            for (int i = 0; i < data.size(); i++) {
                double x = 3 + (double) i / (data.size() - 1) * w;
                double y = 3 + (1.0 - (data.get(i) - min) / (max - min)) * h;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(path);

            // Shading gradient underneath
            path.lineTo(3 + w, 3 + h);
            path.lineTo(3, 3 + h);
            path.closePath();
            g2.setPaint(new GradientPaint(0, 0, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 60),
                    0, getHeight(), new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0)));
            g2.fill(path);

            g2.dispose();
        }
    }
}
