package com.tradingsimulator.ui.components;

import com.tradingsimulator.model.Trade;
import com.tradingsimulator.model.OrderSide;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
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
  private final List<Trade> trades = new ArrayList<>();
  private String activeFilter = "ALL";

  public OrderHistoryPanel() {
    setLayout(new BorderLayout(8, 8));
    JLabel title = new JLabel("Lịch sử giao dịch");
    title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 15f));
    JPanel header = new JPanel(new BorderLayout(8, 8));
    header.add(title, BorderLayout.NORTH);
    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    addFilter(filters, "Tất cả", "ALL");
    addFilter(filters, "Lệnh mua", "BUY");
    addFilter(filters, "Lệnh bán", "SELL");
    header.add(filters, BorderLayout.SOUTH);
    add(header, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
    TradingTableStyle.apply(table);
    table.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
      @Override
      public java.awt.Component getTableCellRendererComponent(
          JTable source, Object value, boolean selected, boolean focus, int row, int column) {
        super.getTableCellRendererComponent(source, value, selected, focus, row, column);
        setHorizontalAlignment(CENTER);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (!selected) {
          boolean buy = "MUA".equals(value);
          setForeground(buy ? new java.awt.Color(0x0e, 0xcb, 0x81)
              : new java.awt.Color(0xf6, 0x46, 0x5d));
        }
        return this;
      }
    });
  }

  public void addTrade(Trade trade) {
    trades.add(trade);
    renderTrades();
  }

  public void showTrades(List<Trade> trades) {
    this.trades.clear();
    if (trades != null) this.trades.addAll(trades);
    renderTrades();
  }

  public JTable getTable() {
    return table;
  }

  private void addFilter(JPanel filters, String title, String filter) {
    JButton button = new JButton(title);
    button.addActionListener(event -> {
      activeFilter = filter;
      renderTrades();
    });
    filters.add(button);
  }

  private void renderTrades() {
    model.setRowCount(0);
    for (Trade trade : trades) {
      if (!"ALL".equals(activeFilter) && !trade.side().name().equals(activeFilter)) continue;
      model.addRow(new Object[] {
          trade.timestamp().toString(),
          trade.symbol(),
          trade.side() == OrderSide.BUY ? "MUA" : "BÁN",
          trade.quantity(),
          trade.price().toPlainString(),
          trade.total().toPlainString()});
    }
  }
}
