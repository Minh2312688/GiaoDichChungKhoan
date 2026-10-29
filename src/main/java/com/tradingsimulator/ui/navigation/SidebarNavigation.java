package com.tradingsimulator.ui.navigation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Thanh điều hướng dọc thu gọn bên trái (compact sidebar) với biểu tượng vector sắc nét.
 * Hỗ trợ chế độ mở rộng (expanded ~165px) và thu nhỏ (collapsed ~56px).
 */
public class SidebarNavigation extends JPanel {

    private final Consumer<ScreenId> onSelect;
    private final Map<ScreenId, JButton> buttons = new EnumMap<>(ScreenId.class);
    private ScreenId current = ScreenId.DASHBOARD;
    private boolean collapsed = false;
    private final JLabel brandLabel;
    private final JLabel liveLabel;
    private final JButton toggleBtn;

    public SidebarNavigation(Consumer<ScreenId> onSelect) {
        this.onSelect = onSelect;
        setLayout(new BorderLayout());
        setBackground(new Color(15, 20, 28));
        setPreferredSize(new Dimension(165, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(34, 43, 58)));

        // Header Brand & Collapse Toggle
        JPanel brand = new JPanel(new BorderLayout(4, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(12, 10, 10, 10));

        brandLabel = new JLabel(" SSI iBoard", new LogoIcon(18), SwingConstants.LEFT);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandLabel.setForeground(new Color(245, 158, 11)); // Amber Gold
        brand.add(brandLabel, BorderLayout.CENTER);

        toggleBtn = new JButton("«");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        toggleBtn.setForeground(new Color(156, 163, 175));
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setToolTipText("Thu gọn / Mở rộng");
        toggleBtn.addActionListener(e -> setCollapsed(!collapsed));
        brand.add(toggleBtn, BorderLayout.EAST);

        add(brand, BorderLayout.NORTH);

        // Menu items
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(4, 6, 8, 6));

        for (ScreenId id : ScreenId.values()) {
            JButton btn = makeNavButton(id);
            buttons.put(id, btn);
            menu.add(btn);
            menu.add(Box.createVerticalStrut(4));
        }

        add(menu, BorderLayout.CENTER);

        // Footer status
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 8, 10, 8));
        liveLabel = new JLabel("● LIVE HOSE", SwingConstants.CENTER);
        liveLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        liveLabel.setForeground(new Color(34, 197, 94)); // Green
        footer.add(liveLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        select(ScreenId.DASHBOARD);
    }

    private JButton makeNavButton(ScreenId id) {
        JButton btn = new JButton(id.getTitle(), new NavIcon(id, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(153, 38));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(156, 163, 175));
        btn.setBackground(new Color(15, 20, 28));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.setToolTipText(id.getTitle());

        btn.addActionListener(e -> select(id));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (current != id) {
                    btn.setBackground(new Color(24, 32, 45));
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (current != id) {
                    btn.setBackground(new Color(15, 20, 28));
                    btn.setForeground(new Color(156, 163, 175));
                }
            }
        });

        return btn;
    }

    public void select(ScreenId id) {
        this.current = id;
        for (Map.Entry<ScreenId, JButton> entry : buttons.entrySet()) {
            boolean active = entry.getKey() == id;
            JButton b = entry.getValue();
            if (active) {
                b.setBackground(new Color(30, 58, 138)); // Dark accent blue
                b.setForeground(new Color(96, 165, 250)); // Sky blue text
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(59, 130, 246)),
                        new EmptyBorder(6, collapsed ? 7 : 7, 6, 10)
                ));
            } else {
                b.setBackground(new Color(15, 20, 28));
                b.setForeground(new Color(156, 163, 175));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                b.setBorder(new EmptyBorder(6, 10, 6, 10));
            }
        }
        if (onSelect != null) {
            onSelect.accept(id);
        }
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        if (collapsed) {
            setPreferredSize(new Dimension(56, 0));
            brandLabel.setText("");
            liveLabel.setText("●");
            toggleBtn.setText("»");
            for (Map.Entry<ScreenId, JButton> entry : buttons.entrySet()) {
                JButton b = entry.getValue();
                b.setText("");
                b.setHorizontalAlignment(SwingConstants.CENTER);
                b.setPreferredSize(new Dimension(44, 38));
            }
        } else {
            setPreferredSize(new Dimension(165, 0));
            brandLabel.setText(" SSI iBoard");
            liveLabel.setText("● LIVE HOSE");
            toggleBtn.setText("«");
            for (Map.Entry<ScreenId, JButton> entry : buttons.entrySet()) {
                JButton b = entry.getValue();
                b.setText(entry.getKey().getTitle());
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.setPreferredSize(new Dimension(153, 38));
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Vector icon cho Logo SSI
     */
    private static class LogoIcon implements Icon {
        private final int size;

        LogoIcon(int size) {
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 158, 11));
            g2.fillRoundRect(x, y, size, size, 4, 4);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString("S", x + 5, y + size - 4);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /**
     * Biểu tượng vector cho từng mục menu
     */
    private static class NavIcon implements Icon {
        private final ScreenId screenId;
        private final int size;

        NavIcon(ScreenId id, int size) {
            this.screenId = id;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());

            int midX = x + size / 2;
            int midY = y + size / 2;

            switch (screenId) {
                case DASHBOARD: // 4 ô vuông dashboard
                    g2.fillRect(x + 1, y + 1, 5, 5);
                    g2.fillRect(x + 9, y + 1, 5, 5);
                    g2.fillRect(x + 1, y + 9, 5, 5);
                    g2.fillRect(x + 9, y + 9, 5, 5);
                    break;
                case TRADING: // Biểu đồ nến / cột sóng
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(x + 3, y + 12, x + 3, y + 6);
                    g2.drawLine(x + 7, y + 14, x + 7, y + 3);
                    g2.drawLine(x + 11, y + 11, x + 11, y + 7);
                    g2.drawLine(x + 14, y + 13, x + 14, y + 2);
                    break;
                case BOT: // Biểu tượng robot / chip
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(x + 2, y + 4, 12, 10, 3, 3);
                    g2.drawLine(midX, y + 1, midX, y + 4); // anten
                    g2.fillOval(x + 5, y + 7, 2, 2); // mắt trái
                    g2.fillOval(x + 9, y + 7, 2, 2); // mắt phải
                    g2.drawLine(x + 5, y + 11, x + 9, y + 11); // miệng
                    break;
                case HISTORY: // Đồng hồ lịch sử
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(x + 1, y + 1, 13, 13);
                    g2.drawLine(midX, midY, midX, y + 4);
                    g2.drawLine(midX, midY, x + 11, midY);
                    break;
                case PENDING: // Danh sách / sổ lệnh
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRect(x + 2, y + 1, 11, 13);
                    g2.drawLine(x + 5, y + 4, x + 10, y + 4);
                    g2.drawLine(x + 5, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 5, y + 10, x + 8, y + 10);
                    break;
                case SETTINGS: // Bánh răng cài đặt
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(x + 4, y + 4, 8, 8);
                    g2.drawLine(midX, y + 1, midX, y + 3);
                    g2.drawLine(midX, y + 12, midX, y + 14);
                    g2.drawLine(x + 1, midY, x + 3, midY);
                    g2.drawLine(x + 12, midY, x + 14, midY);
                    break;
                default:
                    g2.fillOval(x + 4, y + 4, 8, 8);
                    break;
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
