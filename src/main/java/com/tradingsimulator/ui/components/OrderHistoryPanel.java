package com.tradingsimulator.ui.components;

import com.tradingsimulator.model.Trade;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Bảng liệt kê các lệnh đã khớp (lịch sử giao dịch).
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public final class OrderHistoryPanel extends JPanel {
  private final DefaultTableModel model = new DefaultTableModel(
      new Object[] {"Thời gian", "Mã", "Chiều", "Số lượng", "Giá", "Tổng tiền"}, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  };
  private final JTable table = new JTable(model);

  public OrderHistoryPanel() {
    setLayout(new BorderLayout(8, 8));
    add(new JLabel("Lịch sử giao dịch"), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  public void addTrade(Trade trade) {
    model.addRow(new Object[] {
        trade.timestamp().toString(),
        trade.symbol(),
        trade.side() == com.tradingsimulator.model.OrderSide.BUY ? "MUA" : "BÁN",
        trade.quantity(),
        trade.price().toPlainString(),
        trade.total().toPlainString()});
  }

  public void showTrades(List<Trade> trades) {
    model.setRowCount(0);
    if (trades != null) {
      trades.forEach(this::addTrade);
    }
  }

  public JTable getTable() {
    return table;
  }
}
