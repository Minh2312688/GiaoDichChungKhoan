package com.tradingsimulator.ui.components;

import com.tradingsimulator.model.Position;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Bảng danh mục chứng khoán đang nắm giữ.
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Memento).</p>
 */
public final class PortfolioPanel extends JPanel {
  private final DefaultTableModel model = new DefaultTableModel(
      new Object[] {"Mã chứng khoán", "Số lượng đang nắm giữ", "Giá vốn TB"}, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  };
  private final JTable table = new JTable(model);
  private final JLabel emptyLabel = new JLabel("Chưa có chứng khoán đang nắm giữ", JLabel.CENTER);
  private Consumer<String> symbolDoubleClickListener;

  public PortfolioPanel() {
    setLayout(new BorderLayout(8, 8));
    JLabel title = new JLabel("Danh mục chứng khoán");
    title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 15f));
    title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
    add(title, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
    add(scrollPane, BorderLayout.CENTER);
    add(emptyLabel, BorderLayout.SOUTH);
    TradingTableStyle.apply(table);
    emptyLabel.setVisible(true);
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

  public void showPositions(Map<String, Position> positions) {
    if (positions == null) {
      throw new IllegalArgumentException("Danh mục vị thế không được để trống");
    }
    model.setRowCount(0);
    positions.values().stream()
        .filter(position -> position.quantity() > 0)
        .sorted(Comparator.comparing(Position::symbol))
        .forEach(position -> model.addRow(new Object[] {
            position.symbol(),
            position.quantity(),
            position.averagePrice().toPlainString()
        }));
    emptyLabel.setVisible(model.getRowCount() == 0);
  }

  public JTable getTable() {
    return table;
  }

  public void onSymbolDoubleClick(Consumer<String> listener) {
    symbolDoubleClickListener = Objects.requireNonNull(listener, "listener");
  }
}
