package com.tradingsimulator.ui.components;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel tổng kết danh mục: tiền mặt và các vị thế hiện có (dữ liệu lấy từ memento/facade).
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Memento).</p>
 */
public final class PortfolioPanel extends JPanel {
  private final JLabel cashLabel = new JLabel("Tiền mặt: -");
  private final JLabel positionsLabel = new JLabel("Vị thế: -");

  public PortfolioPanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(new JLabel("Danh mục"));
    add(cashLabel);
    add(positionsLabel);
  }

  public void showCash(String cash) {
    cashLabel.setText("Tiền mặt: " + cash);
  }

  public void showPositions(String positions) {
    positionsLabel.setText("Vị thế: " + positions);
  }
}
