package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Bảng giá: hiển thị bid/ask theo symbol, cập nhật qua thông báo của Observer.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public final class PriceBoardPanel extends JPanel {
  private static final Color POSITIVE = new Color(0x0e, 0xcb, 0x81);
  private static final Color NEGATIVE = new Color(0xf6, 0x46, 0x5d);
  private final DefaultTableModel model = new DefaultTableModel(
      new Object[]{"Mã", "Giá mua", "Giá bán", "Thay đổi"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return false; }
  };
  private final Map<String, QuoteValues> previousQuotes = new HashMap<>();
  private final Map<String, Color> flashingCells = new HashMap<>();
  private final Map<String, javax.swing.Timer> flashTimers = new HashMap<>();
  private final JTable table = new JTable(model) {
    @Override
    public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
      Component component = super.prepareRenderer(renderer, row, column);
      if (!isCellSelected(row, column)) {
        String symbol = model.getValueAt(convertRowIndexToModel(row), 0).toString();
        int modelColumn = convertColumnIndexToModel(column);
        Color flash = flashingCells.get(cellKey(symbol, modelColumn));
        if (flash != null) {
          component.setBackground(blend(getBackground(), flash));
        }
        if (modelColumn == 3) {
          String change = model.getValueAt(convertRowIndexToModel(row), modelColumn).toString();
          component.setForeground(change.startsWith("-") ? NEGATIVE
              : change.startsWith("+") ? POSITIVE : table.getForeground());
        }
      }
      return component;
    }
  };
  private Consumer<String> symbolDoubleClickListener;

  public PriceBoardPanel() {
    setLayout(new BorderLayout(8, 8));
    JLabel title = new JLabel("Bảng giá thị trường");
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
    add(title, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
    TradingTableStyle.apply(table);
    table.setToolTipText("Nhấn đúp vào mã chứng khoán để mua hoặc bán");
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          int row = table.rowAtPoint(event.getPoint());
          if (row >= 0 && symbolDoubleClickListener != null) {
            symbolDoubleClickListener.accept(model.getValueAt(row, 0).toString());
          }
        }
      }
    });
  }

  public void setQuote(String symbol, String bid, String ask) {
    BigDecimal bidValue = new BigDecimal(bid);
    BigDecimal askValue = new BigDecimal(ask);
    BigDecimal midpoint = bidValue.add(askValue).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
    QuoteValues previous = previousQuotes.put(symbol, new QuoteValues(bidValue, askValue, midpoint));
    String change = "0.00%";
    if (previous != null && previous.midpoint().signum() != 0) {
      BigDecimal percent = midpoint.subtract(previous.midpoint())
          .multiply(BigDecimal.valueOf(100))
          .divide(previous.midpoint(), 2, RoundingMode.HALF_UP);
      change = (percent.signum() > 0 ? "+" : "") + percent.toPlainString() + "%";
    }

    for (int row = 0; row < model.getRowCount(); row++) {
      if (symbol.equals(model.getValueAt(row, 0))) {
        if (previous != null) {
          if (bidValue.compareTo(previous.bid()) != 0) {
            flash(symbol, 1, bidValue.compareTo(previous.bid()) > 0 ? POSITIVE : NEGATIVE);
          }
          if (askValue.compareTo(previous.ask()) != 0) {
            flash(symbol, 2, askValue.compareTo(previous.ask()) > 0 ? POSITIVE : NEGATIVE);
          }
          flash(symbol, 3, midpoint.compareTo(previous.midpoint()) >= 0 ? POSITIVE : NEGATIVE);
        }
        model.setValueAt(bid, row, 1);
        model.setValueAt(ask, row, 2);
        model.setValueAt(change, row, 3);
        return;
      }
    }
    model.addRow(new Object[]{symbol, bid, ask, change});
  }

  public JTable getTable() { return table; }

  private void flash(String symbol, int column, Color color) {
    String key = cellKey(symbol, column);
    javax.swing.Timer previous = flashTimers.remove(key);
    if (previous != null) {
      previous.stop();
    }
    flashingCells.put(key, color);
    javax.swing.Timer timer = new javax.swing.Timer(500, event -> {
      flashingCells.remove(key);
      flashTimers.remove(key);
      table.repaint();
    });
    timer.setRepeats(false);
    flashTimers.put(key, timer);
    timer.start();
    table.repaint();
  }

  private static String cellKey(String symbol, int column) {
    return symbol + ":" + column;
  }

  private static Color blend(Color base, Color flash) {
    return new Color(
        (base.getRed() + flash.getRed()) / 2,
        (base.getGreen() + flash.getGreen()) / 2,
        (base.getBlue() + flash.getBlue()) / 2);
  }

  private record QuoteValues(BigDecimal bid, BigDecimal ask, BigDecimal midpoint) { }

  public void onSymbolDoubleClick(Consumer<String> listener) {
    symbolDoubleClickListener = Objects.requireNonNull(listener, "listener");
  }
}
