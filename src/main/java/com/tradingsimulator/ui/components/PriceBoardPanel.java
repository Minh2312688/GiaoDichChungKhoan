package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Bảng giá: hiển thị bid/ask theo symbol, cập nhật qua thông báo của Observer.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public final class PriceBoardPanel extends JPanel {
  private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Symbol", "Bid", "Ask"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return false; }
  };
  private final JTable table = new JTable(model);

  public PriceBoardPanel() {
    setLayout(new BorderLayout(8, 8));
    add(new JLabel("Price Board"), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
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
}
