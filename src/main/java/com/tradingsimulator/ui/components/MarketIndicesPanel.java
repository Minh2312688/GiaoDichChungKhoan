package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Dải hiển thị chỉ số thị trường (VNINDEX, VN30, HNX30, ...) theo chuẩn giao diện SSI iBoard.
 * Bao gồm biểu đồ mini sparkline và bảng thống kê độ rộng thị trường (Mã tăng / Đứng giá / Giảm).
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

    public MarketIndicesPanel() {
        setLayout(new GridLayout(1, 4, 6, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(4, 6, 6, 6));
        setPreferredSize(new Dimension(0, 115));

        // 1. VNINDEX Card
        add(createIndexCard("VNINDEX", "1,754.53", "-12.12 (-0.69%)",
                "355.985 CP  ·  8,131.591 Tỷ",
                new double[]{1766, 1764, 1761, 1759, 1757, 1752, 1755, 1751, 1754},
                "▲ 81 (3)   ■ 52   ▼ 218 (7)", RED));

        // 2. VN30 Card
        add(createIndexCard("VN30", "1,875.48", "-14.74 (-0.78%)",
                "141.061 CP  ·  4,456.094 Tỷ",
                new double[]{1890, 1888, 1883, 1878, 1872, 1870, 1874, 1871, 1875},
                "▲ 4 (0)   ■ 2   ▼ 24 (0)", RED));

        // 3. HNX30 Card
        add(createIndexCard("HNX30", "435.58", "-4.08 (-0.93%)",
                "13.245 CP  ·  251.971 Tỷ",
                new double[]{440, 439, 437, 436, 434, 432, 435, 433, 435},
                "▲ 5 (0)   ■ 4   ▼ 18 (0)", RED));

        // 4. Bảng tổng hợp các chỉ số
        add(createSummaryCard());
    }

    private JPanel createIndexCard(String name, String points, String change, String volume,
                                   double[] trend, String breadth, Color changeColor) {
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

        JLabel changeLbl = new JLabel(points + "  " + change);
        changeLbl.setFont(changeLbl.getFont().deriveFont(Font.BOLD, 11f));
        changeLbl.setForeground(changeColor);

        top.add(titleLbl, BorderLayout.WEST);
        top.add(changeLbl, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Chart
        MiniSparkline chart = new MiniSparkline(trend, changeColor);
        card.add(chart, BorderLayout.CENTER);

        // Bottom Volume & Breadth
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel volLbl = new JLabel(volume);
        volLbl.setFont(volLbl.getFont().deriveFont(Font.PLAIN, 9f));
        volLbl.setForeground(TEXT_MUTED);

        JLabel breadthLbl = new JLabel(breadth);
        breadthLbl.setFont(breadthLbl.getFont().deriveFont(Font.BOLD, 9f));
        breadthLbl.setForeground(TEXT_LIGHT);

        bottom.add(volLbl, BorderLayout.NORTH);
        bottom.add(breadthLbl, BorderLayout.SOUTH);
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
        header.add(headerLabel("Chỉ số"));
        header.add(headerLabel("Điểm"));
        header.add(headerLabel("+/-"));
        header.add(headerLabel("KLGD (Tr)"));
        header.add(headerLabel("Tăng/Giảm"));
        card.add(header, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(5, 1, 0, 1));
        rows.setOpaque(false);

        rows.add(indexRow("VNINDEX", "1,754.53", "-12.12", "355.98", "▲81 ▼218", RED));
        rows.add(indexRow("VN30", "1,875.48", "-14.74", "141.06", "▲4 ▼24", RED));
        rows.add(indexRow("HNX30", "435.58", "-4.08", "13.25", "▲5 ▼18", RED));
        rows.add(indexRow("VNXALL", "2,767.50", "-21.65", "356.97", "▲91 ▼258", RED));
        rows.add(indexRow("HNXINDEX", "257.33", "-2.53", "20.58", "▲47 ▼78", RED));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 9f));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JPanel indexRow(String name, String points, String change, String vol, String breadth, Color col) {
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

    private static class MiniSparkline extends JPanel {
        private final double[] data;
        private final Color lineColor;

        public MiniSparkline(double[] data, Color lineColor) {
            this.data = data;
            this.lineColor = lineColor;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.length < 2) return;

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

            for (int i = 0; i < data.length; i++) {
                double x = 3 + (double) i / (data.length - 1) * w;
                double y = 3 + (1.0 - (data[i] - min) / (max - min)) * h;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(path);

            // Shading underneath
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
