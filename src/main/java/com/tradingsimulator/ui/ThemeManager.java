package com.tradingsimulator.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.AbstractButton;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.InputStream;
import java.util.Map;

/**
 * Quản lý giao diện (theme) nạp từ tệp JSON cấu hình; có giá trị mặc định an toàn khi thiếu tệp.
 *
 * <p>Chủ sở hữu: Thành viên 3 (phần giao diện).</p>
 */
public record ThemeManager(
    Color background,
    Color foreground,
    Color accent,
    Color panel,
    Color positive,
    Color negative,
    Color warning,
    Color secondary,
    Color border
) {
  public ThemeManager(Color background, Color foreground, Color accent) {
    this(
        background,
        foreground,
        accent,
        new Color(0x1e, 0x23, 0x29),
        new Color(0x0e, 0xcb, 0x81),
        new Color(0xf6, 0x46, 0x5d),
        new Color(0xf0, 0xb9, 0x0b),
        new Color(0x84, 0x8e, 0x9c),
        new Color(0x2b, 0x31, 0x3a)
    );
  }

  public ThemeManager(
      Color background, Color foreground, Color accent, Color panel,
      Color positive, Color negative, Color warning) {
    this(background, foreground, accent, panel, positive, negative, warning,
        new Color(0x84, 0x8e, 0x9c), new Color(0x2b, 0x31, 0x3a));
  }

  public static void setupLookAndFeel() {
    if (!(UIManager.getLookAndFeel() instanceof FlatDarkLaf)) {
      FlatDarkLaf.setup();
    }
    UIManager.put("Button.arc", 10);
    UIManager.put("Component.arc", 8);
    UIManager.put("TextComponent.arc", 8);
    UIManager.put("ScrollBar.thumbArc", 999);
    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
  }

  public static ThemeManager load(String resource) {
    try (InputStream in = ThemeManager.class.getClassLoader().getResourceAsStream(resource)) {
      if (in == null) return defaults();
      Map<?, ?> values = new ObjectMapper().readValue(in, Map.class);
      return new ThemeManager(
          color(values, "background", new Color(0x12, 0x17, 0x21)),
          color(values, "foreground", new Color(0xea, 0xec, 0xef)),
          color(values, "accent", new Color(0xf0, 0xb9, 0x0b)),
          color(values, "panel", new Color(0x1e, 0x23, 0x29)),
          color(values, "positive", new Color(0x0e, 0xcb, 0x81)),
          color(values, "negative", new Color(0xf6, 0x46, 0x5d)),
          color(values, "warning", new Color(0xf0, 0xb9, 0x0b)),
          color(values, "secondary", new Color(0x84, 0x8e, 0x9c)),
          color(values, "border", new Color(0x2b, 0x31, 0x3a))
      );
    } catch (Exception ignored) {
      return defaults();
    }
  }

  public void applyTo(Component component) {
    applyDefaults();
    applyToComponent(component);
  }

  private void applyToComponent(Component component) {
    component.setBackground(background);
    component.setForeground(foreground);
    if (component instanceof AbstractButton button) {
      button.setBackground(accent);
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        applyToComponent(child);
      }
    }
  }

  private void applyDefaults() {
    UIManager.put("Panel.background", panel);
    UIManager.put("Label.foreground", foreground);
    UIManager.put("Button.background", panel);
    UIManager.put("Button.foreground", foreground);
    UIManager.put("TextField.background", panel);
    UIManager.put("TextField.foreground", foreground);
    UIManager.put("ComboBox.background", panel);
    UIManager.put("ComboBox.foreground", foreground);
    UIManager.put("Table.background", panel);
    UIManager.put("Table.foreground", foreground);
    UIManager.put("TableHeader.background", panel);
    UIManager.put("TableHeader.foreground", secondary);
    UIManager.put("Separator.foreground", border);
    UIManager.put("Component.borderColor", border);
    UIManager.put("Component.focusColor", accent);
    UIManager.put("ProgressBar.foreground", positive);
    UIManager.put("ProgressBar.background", border);
  }

  private static Color color(Map<?, ?> values, String key, Color fallback) {
    Object value = values.get(key);
    if (value instanceof String text) {
      try {
        return Color.decode(text);
      } catch (NumberFormatException ignored) {
      }
    }
    return fallback;
  }

  private static ThemeManager defaults() {
    return new ThemeManager(
        new Color(0x12, 0x17, 0x21),
        new Color(0xea, 0xec, 0xef),
        new Color(0xf0, 0xb9, 0x0b),
        new Color(0x1e, 0x23, 0x29),
        new Color(0x0e, 0xcb, 0x81),
        new Color(0xf6, 0x46, 0x5d),
        new Color(0xf0, 0xb9, 0x0b),
        new Color(0x84, 0x8e, 0x9c),
        new Color(0x2b, 0x31, 0x3a)
    );
  }
}
