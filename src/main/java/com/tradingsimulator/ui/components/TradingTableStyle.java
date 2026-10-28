package com.tradingsimulator.ui.components;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public final class TradingTableStyle {
  private static final Color STRIPE = new Color(0x22, 0x27, 0x30);

  private TradingTableStyle() { }

  public static void apply(JTable table) {
    table.setRowHeight(36);
    table.setIntercellSpacing(new java.awt.Dimension(0, 1));
    table.setShowGrid(false);
    table.setFillsViewportHeight(true);
    table.setSelectionBackground(color("Table.selectionBackground", new Color(0x3a, 0x3f, 0x48)));
    table.setSelectionForeground(color("Table.selectionForeground", Color.WHITE));
    table.setBackground(color("Table.background", new Color(0x1e, 0x23, 0x29)));
    table.setForeground(color("Table.foreground", new Color(0xea, 0xec, 0xef)));
    table.setDefaultRenderer(Object.class, new StripedRenderer());

    JTableHeader header = table.getTableHeader();
    header.setPreferredSize(new java.awt.Dimension(0, 38));
    header.setReorderingAllowed(false);
    header.setBackground(color("TableHeader.background", new Color(0x1e, 0x23, 0x29)));
    header.setForeground(color("TableHeader.foreground", new Color(0x84, 0x8e, 0x9c)));
    header.setBorder(javax.swing.BorderFactory.createMatteBorder(
        0, 0, 1, 0, color("Separator.foreground", new Color(0x2b, 0x31, 0x3a))));
  }

  public static Color color(String key, Color fallback) {
    Color configured = UIManager.getColor(key);
    return configured == null ? fallback : configured;
  }

  private static final class StripedRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean selected, boolean focus, int row, int column) {
      super.getTableCellRendererComponent(table, value, selected, focus, row, column);
      if (!selected) {
        setBackground(row % 2 == 0 ? table.getBackground() : STRIPE);
        setForeground(table.getForeground());
      }
      setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
      return this;
    }
  }
}
