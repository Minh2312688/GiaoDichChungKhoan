package com.tradingsimulator.ui.navigation;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarNavigation extends JPanel {
    private final Consumer<ScreenId> onSelect;
    private final Map<ScreenId, JButton> buttons;
    private ScreenId activeScreen;

    public SidebarNavigation(Consumer<ScreenId> onSelect) {
        this.onSelect = onSelect != null ? onSelect : id -> {};
        this.buttons = new EnumMap<>(ScreenId.class);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (ScreenId id : ScreenId.values()) {
            JButton button = new JButton(id.getTitle());
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            button.setFocusPainted(false);
            button.addActionListener(e -> select(id));
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
        for (Map.Entry<ScreenId, JButton> entry : buttons.entrySet()) {
            entry.getValue().setEnabled(entry.getKey() != id);
        }
        onSelect.accept(id);
    }

    public ScreenId getActiveScreen() {
        return activeScreen;
    }

    public JButton getButton(ScreenId id) {
        return buttons.get(id);
    }
}
