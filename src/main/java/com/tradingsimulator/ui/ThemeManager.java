package com.tradingsimulator.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.AbstractButton;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
    Color warning
) {
  public ThemeManager(Color background, Color foreground, Color accent) {
    this(
        background,
        foreground,
        accent,
        new Color(0x38, 0x3e, 0x47),
        new Color(0x28, 0xa7, 0x45),
        new Color(0xdc, 0x35, 0x45),
        new Color(0xff, 0xc1, 0x07)
    );
  }

  public static ThemeManager load(String resource) {
    try (InputStream in = ThemeManager.class.getClassLoader().getResourceAsStream(resource)) {
      if (in == null) return defaults();
      Map<?, ?> values = new ObjectMapper().readValue(in, Map.class);
      return new ThemeManager(
          color(values, "background", Color.DARK_GRAY),
          color(values, "foreground", Color.WHITE),
          color(values, "accent", new Color(45, 140, 220)),
          color(values, "panel", new Color(0x38, 0x3e, 0x47)),
          color(values, "positive", new Color(0x28, 0xa7, 0x45)),
          color(values, "negative", new Color(0xdc, 0x35, 0x45)),
          color(values, "warning", new Color(0xff, 0xc1, 0x07))
      );
    } catch (Exception ignored) {
      return defaults();
    }
  }

  public void applyTo(Component component) {
    component.setBackground(background);
    component.setForeground(foreground);
    if (component instanceof AbstractButton button) {
      button.setBackground(accent);
    }
    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        applyTo(child);
      }
    }
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
        Color.DARK_GRAY,
        Color.WHITE,
        new Color(45, 140, 220),
        new Color(0x38, 0x3e, 0x47),
        new Color(0x28, 0xa7, 0x45),
        new Color(0xdc, 0x35, 0x45),
        new Color(0xff, 0xc1, 0x07)
    );
  }
}
