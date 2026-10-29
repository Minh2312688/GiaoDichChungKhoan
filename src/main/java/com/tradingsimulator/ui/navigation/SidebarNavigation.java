package com.tradingsimulator.ui.navigation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Thanh điều hướng Sidebar Navigation chuyên nghiệp chuẩn SSI iBoard.
 * Thiết kế gọn gàng, hiện đại với icon vector sinh động, sắc nét trên mọi hệ điều hành.
 */
public class SidebarNavigation extends JPanel {
    private final Consumer<ScreenId> onSelect;
    private final Map<ScreenId, JButton> buttons;
    private final Map<ScreenId, Icon> icons;
    private final JButton collapseButton;
    private boolean collapsed;
    private ScreenId activeScreen;

    public SidebarNavigation(Consumer<ScreenId> onSelect) {
        this.onSelect = onSelect != null ? onSelect : id -> {};
        this.buttons = new EnumMap<>(ScreenId.class);
        this.icons = new EnumMap<>(ScreenId.class);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0x13, 0x17, 0x22));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x23, 0x27, 0x30)),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)));
        setPreferredSize(new Dimension(175, 0));

        // Header Brand SSI iBoard
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        brand.setOpaque(false);
        JLabel logoBadge = new JLabel(" SSI ");
        logoBadge.setOpaque(true);
        logoBadge.setBackground(new Color(0xd3, 0x2f, 0x2f));
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setFont(logoBadge.getFont().deriveFont(Font.BOLD, 12f));
        logoBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel brandLabel = new JLabel("iBoard");
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setFont(brandLabel.getFont().deriveFont(Font.BOLD, 15f));

        collapseButton = new JButton("«");
        collapseButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        collapseButton.setForeground(new Color(0x84, 0x8e, 0x9c));
        collapseButton.setBackground(new Color(0x1e, 0x23, 0x29));
        collapseButton.setFocusPainted(false);
        collapseButton.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        collapseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        collapseButton.setToolTipText("Thu gọn thanh điều hướng");
        collapseButton.addActionListener(event -> setCollapsed(!collapsed));

        JPanel brandHeader = new JPanel(new BorderLayout(6, 0));
        brandHeader.setOpaque(false);
        brand.add(logoBadge);
        brand.add(brandLabel);
        brandHeader.add(brand, BorderLayout.CENTER);
        brandHeader.add(collapseButton, BorderLayout.EAST);
        brandHeader.setBorder(BorderFactory.createEmptyBorder(0, 4, 16, 4));
        add(brandHeader);

        for (ScreenId id : ScreenId.values()) {
            Icon icon = createVectorIcon(id);
            icons.put(id, icon);

            JButton button = new JButton(id.getTitle(), icon);
            button.setIconTextGap(10);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            button.setPreferredSize(new Dimension(160, 38));
            button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> select(id));
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (activeScreen != id) button.setBackground(new Color(0x23, 0x27, 0x30));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    updateButton(id, button);
                }
            });
            buttons.put(id, button);
            add(button);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(Box.createVerticalGlue());
        if (ScreenId.values().length > 0) {
            select(ScreenId.DASHBOARD);
        }
    }

    public void select(ScreenId id) {
        if (id == null) {
            return;
        }
        this.activeScreen = id;
        buttons.forEach(this::updateButton);
        onSelect.accept(id);
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        setPreferredSize(new Dimension(collapsed ? 54 : 175, 0));
        buttons.forEach((id, button) -> {
            button.setText(collapsed ? "" : id.getTitle());
            button.setHorizontalAlignment(collapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
            button.setToolTipText(collapsed ? id.getTitle() : null);
            updateButton(id, button);
        });
        collapseButton.setText(collapsed ? "»" : "«");
        revalidate();
        repaint();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    private void updateButton(ScreenId id, JButton button) {
        boolean active = id == activeScreen;
        button.setBackground(active ? new Color(0x1e, 0x2d, 0x38) : new Color(0x13, 0x17, 0x22));
        button.setForeground(active ? new Color(0x0e, 0xcb, 0x81) : new Color(0x84, 0x8e, 0x9c));
        button.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
    }

    private Icon createVectorIcon(ScreenId id) {
        return new Icon() {
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean active = (id == activeScreen);
                    Color col = active ? new Color(0x0e, 0xcb, 0x81) : new Color(0x84, 0x8e, 0x9c);
                    g2.setColor(col);

                    switch (id) {
                        case DASHBOARD -> {
                            // Biểu tượng Dashboard (4 ô lưới tinh tế)
                            g2.fillRoundRect(x + 1, y + 1, 6, 6, 2, 2);
                            g2.fillRoundRect(x + 10, y + 1, 6, 6, 2, 2);
                            g2.fillRoundRect(x + 1, y + 10, 6, 6, 2, 2);
                            g2.fillRoundRect(x + 10, y + 10, 6, 6, 2, 2);
                        }
                        case TRADING -> {
                            // Biểu tượng Biểu đồ nến / Đặt lệnh (Cột nến và mũi tên)
                            g2.fillRect(x + 3, y + 5, 3, 9);
                            g2.drawLine(x + 4, y + 2, x + 4, y + 15);

                            g2.fillRect(x + 9, y + 2, 3, 10);
                            g2.drawLine(x + 10, y, x + 10, y + 14);

                            g2.fillRect(x + 15, y + 7, 2, 6);
                            g2.drawLine(x + 16, y + 4, x + 16, y + 15);
                        }
                        case BOT -> {
                            // Biểu tượng Robot thông minh
                            g2.drawRoundRect(x + 2, y + 4, 13, 10, 3, 3);
                            g2.fillOval(x + 5, y + 7, 2, 2);
                            g2.fillOval(x + 10, y + 7, 2, 2);
                            g2.drawLine(x + 8, y + 1, x + 8, y + 4);
                            g2.fillOval(x + 7, y, 3, 2);
                        }
                        case HISTORY -> {
                            // Biểu tượng Đồng hồ lịch sử
                            g2.setStroke(new BasicStroke(1.6f));
                            g2.drawOval(x + 2, y + 2, 13, 13);
                            g2.drawLine(x + 8, y + 5, x + 8, y + 9);
                            g2.drawLine(x + 8, y + 9, x + 12, y + 9);
                        }
                        case PENDING_ORDERS -> {
                            // Biểu tượng Danh sách lệnh chờ
                            g2.setStroke(new BasicStroke(1.6f));
                            g2.drawLine(x + 2, y + 4, x + 15, y + 4);
                            g2.drawLine(x + 2, y + 9, x + 15, y + 9);
                            g2.drawLine(x + 2, y + 14, x + 11, y + 14);
                        }
                        case SETTINGS -> {
                            // Biểu tượng Bánh răng cài đặt
                            g2.setStroke(new BasicStroke(1.6f));
                            g2.drawOval(x + 5, y + 5, 7, 7);
                            g2.drawLine(x + 8, y + 1, x + 8, y + 4);
                            g2.drawLine(x + 8, y + 13, x + 8, y + 16);
                            g2.drawLine(x + 1, y + 8, x + 4, y + 8);
                            g2.drawLine(x + 13, y + 8, x + 16, y + 8);
                        }
                    }
                } finally {
                    g2.dispose();
                }
            }
        };
    }

    public ScreenId getActiveScreen() {
        return activeScreen;
    }

    public JButton getButton(ScreenId id) {
        return buttons.get(id);
    }
}
