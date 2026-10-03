package com.tradingsimulator;
import com.tradingsimulator.ui.ThemeManager;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

/** Chủ sở hữu: Thành viên 3 (giao diện, Observer). */
class TestTheme {
  @Test void missingThemeFallsBackToDefaults(){
    ThemeManager t=ThemeManager.load("missing-theme.json");
    assertNotNull(t.background());
    assertNotNull(t.foreground());
    assertEquals(Color.DARK_GRAY,t.background());
  }

  @Test void configuredThemeLoadsAndAppliesRecursively(){
    ThemeManager t=ThemeManager.load("theme_config.json");
    JPanel root=new JPanel();
    JPanel child=new JPanel();
    root.add(child);
    t.applyTo(root);
    assertEquals(t.background(),root.getBackground());
    assertEquals(t.background(),child.getBackground());
    assertEquals(t.foreground(),child.getForeground());
  }
}
