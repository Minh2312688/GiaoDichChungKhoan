package com.tradingsimulator.ui.navigation;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScreenManager {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final Map<ScreenId, Component> screenMap;
    private ScreenId currentScreen;

    public ScreenManager() {
        this.cardLayout = new CardLayout();
        this.container = new JPanel(cardLayout);
        this.screenMap = new LinkedHashMap<>();
    }

    public void register(ScreenId id, Component component) {
        if (id == null || component == null) {
            throw new IllegalArgumentException("ScreenId and component must not be null");
        }
        screenMap.put(id, component);
        container.add(component, id.name());
        if (currentScreen == null) {
            currentScreen = id;
        }
    }

    public void show(ScreenId id) {
        if (!screenMap.containsKey(id)) {
            throw new IllegalArgumentException("Screen not registered: " + id);
        }
        cardLayout.show(container, id.name());
        currentScreen = id;
        container.revalidate();
        container.repaint();
    }

    public ScreenId currentScreen() {
        return currentScreen;
    }

    public JComponent view() {
        return container;
    }

    public Component getComponent(ScreenId id) {
        return screenMap.get(id);
    }
}
