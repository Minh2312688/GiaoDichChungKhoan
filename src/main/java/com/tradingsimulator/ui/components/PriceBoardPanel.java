package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Bảng giá: hiển thị bid/ask theo symbol, cập nhật qua thông báo của Observer.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public final class PriceBoardPanel extends JPanel {
  private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Mã", "Giá mua", "Giá bán"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return false; }
  };
  private final JTable table = new JTable(model);
  private Consumer<String> symbolDoubleClickListener;

  public PriceBoardPanel() {
    setLayout(new BorderLayout(8, 8));
    add(new JLabel("Bảng giá"), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
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
    for (int row = 0; row < model.getRowCount(); row++) {
      if (symbol.equals(model.getValueAt(row, 0))) {
        model.setValueAt(bid, row, 1); model.setValueAt(ask, row, 2); return;
      }
    }
    model.addRow(new Object[]{symbol, bid, ask});
  }

  public JTable getTable() { return table; }

  public void onSymbolDoubleClick(Consumer<String> listener) {
    symbolDoubleClickListener = Objects.requireNonNull(listener, "listener");
  }
}
