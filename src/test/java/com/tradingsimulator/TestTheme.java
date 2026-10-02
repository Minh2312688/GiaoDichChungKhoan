package com.tradingsimulator;
import com.tradingsimulator.ui.ThemeManager; import org.junit.jupiter.api.Test; import java.awt.Color; import static org.junit.jupiter.api.Assertions.*;
class TestTheme { @Test void missingThemeFallsBackToDefaults(){ ThemeManager t=ThemeManager.load("missing-theme.json"); assertNotNull(t.background()); assertNotNull(t.foreground()); assertEquals(Color.DARK_GRAY,t.background()); } }
