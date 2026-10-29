package com.tradingsimulator.ui.navigation;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarNavigation extends JPanel {
    private final Consumer<ScreenId> onSelect;
    private final Map<ScreenId, JButton> buttons;
    private final Map<ScreenId, String> labels;
    private final JButton collapseButton;
    private boolean collapsed;
    private ScreenId activeScreen;

    public SidebarNavigation(Consumer<ScreenId> onSelect) {
        this.onSelect = onSelect != null ? onSelect : id -> {};
        this.buttons = new EnumMap<>(ScreenId.class);
        this.labels = new EnumMap<>(ScreenId.class);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0x1e, 0x23, 0x29));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x2b, 0x31, 0x3a)),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        brand.setOpaque(false);
        JLabel logoBadge = new JLabel(" SSI ");
        logoBadge.setOpaque(true);
        logoBadge.setBackground(new Color(0xd3, 0x2f, 0x2f)); // Màu đỏ SSI
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setFont(logoBadge.getFont().deriveFont(Font.BOLD, 12f));
        logoBadge.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JLabel brandLabel = new JLabel("iBoard");
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setFont(brandLabel.getFont().deriveFont(Font.BOLD, 15f));

        collapseButton = new JButton("≡");
        collapseButton.setToolTipText("Thu gọn thanh điều hướng");
        collapseButton.setFocusable(false);
        collapseButton.addActionListener(event -> setCollapsed(!collapsed));

        JPanel brandHeader = new JPanel(new BorderLayout(8, 0));
        brandHeader.setOpaque(false);
        brand.add(logoBadge);
        brand.add(brandLabel);
        brandHeader.add(brand, BorderLayout.CENTER);
        brandHeader.add(collapseButton, BorderLayout.EAST);
        brandHeader.setBorder(BorderFactory.createEmptyBorder(0, 4, 18, 4));
        add(brandHeader);

        for (ScreenId id : ScreenId.values()) {
            String icon = iconFor(id);
            JButton button = new JButton(icon + "  " + id.getTitle());
            labels.put(id, icon);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            button.setPreferredSize(new Dimension(190, 44));
            button.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.addActionListener(e -> select(id));
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (activeScreen != id) button.setBackground(new Color(0x2b, 0x31, 0x3a));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    updateButton(id, button);
                }
            });
            buttons.put(id, button);
            add(button);
            add(Box.createRigidArea(new Dimension(0, 8)));
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
        setPreferredSize(new Dimension(collapsed ? 68 : 210, 0));
        buttons.forEach((id, button) -> {
            button.setText(collapsed ? labels.get(id) : labels.get(id) + "  " + id.getTitle());
            button.setHorizontalAlignment(collapsed ? SwingConstants.CENTER : SwingConstants.LEFT);
            button.setToolTipText(collapsed ? id.getTitle() : null);
            updateButton(id, button);
        });
        collapseButton.setText(collapsed ? "»" : "☰");
        revalidate();
        repaint();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    private void updateButton(ScreenId id, JButton button) {
        boolean active = id == activeScreen;
        button.setBackground(active ? new Color(0x3a, 0x32, 0x1b) : new Color(0x1e, 0x23, 0x29));
        button.setForeground(active ? new Color(0xf0, 0xb9, 0x0b) : new Color(0xea, 0xec, 0xef));
        button.setEnabled(true);
    }

    private static String iconFor(ScreenId id) {
        return switch (id) {
            case DASHBOARD -> "■";
            case TRADING -> "▶";
            case BOT -> "●";
            case HISTORY -> "⏱";
            case PENDING_ORDERS -> "≡";
            case SETTINGS -> "⚙";
        };
    }

    public ScreenId getActiveScreen() {
        return activeScreen;
    }

    public JButton getButton(ScreenId id) {
        return buttons.get(id);
    }
}
