package com.tradingsimulator;

import com.tradingsimulator.ui.navigation.ScreenId;
import com.tradingsimulator.ui.navigation.ScreenManager;
import com.tradingsimulator.ui.navigation.SidebarNavigation;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestNavigation {

    @Test
    void screenManagerShowsRegisteredScreenWithoutCreatingWindows() {
        int initialWindowCount = Window.getWindows().length;

        ScreenManager screenManager = new ScreenManager();
        JPanel dashboardPanel = new JPanel();
        JPanel botPanel = new JPanel();

        screenManager.register(ScreenId.DASHBOARD, dashboardPanel);
        screenManager.register(ScreenId.BOT, botPanel);

        screenManager.show(ScreenId.BOT);

        assertEquals(ScreenId.BOT, screenManager.currentScreen());
        assertNotNull(screenManager.view());
        assertEquals(initialWindowCount, Window.getWindows().length);
    }

    @Test
    void sidebarNavigationTriggersCallbackWithCorrectScreenId() {
        AtomicReference<ScreenId> selected = new AtomicReference<>();
        SidebarNavigation sidebar = new SidebarNavigation(selected::set);

        sidebar.select(ScreenId.TRADING);
        assertEquals(ScreenId.TRADING, selected.get());

        sidebar.select(ScreenId.SETTINGS);
        assertEquals(ScreenId.SETTINGS, selected.get());
    }
}
