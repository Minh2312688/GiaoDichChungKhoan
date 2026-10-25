package com.tradingsimulator.ui.components;

import com.tradingsimulator.model.Position;
import java.awt.BorderLayout;
import java.util.Comparator;
import java.util.Map;
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

  public PortfolioPanel() {
    setLayout(new BorderLayout(8, 8));
    add(new JLabel("Danh mục chứng khoán"), BorderLayout.NORTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
    add(emptyLabel, BorderLayout.SOUTH);
    emptyLabel.setVisible(true);
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
}
