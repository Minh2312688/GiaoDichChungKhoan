package com.tradingsimulator.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;

/**
 * Form đặt lệnh trên giao diện (dùng {@code OrderFactory} để tạo lệnh).
 *
 * <p>Chủ sở hữu: Thành viên 1 (mẫu Factory Method).</p>
 */
public final class OrderFormPanel extends JPanel {
  private final JTextField symbolField = new JTextField(8);
  private final JTextField quantityField = new JTextField("1", 8);
  private final JComboBox<OrderSide> sideBox = new JComboBox<>(OrderSide.values());
  private final JButton submitButton = new JButton("Gửi lệnh");
  private final JLabel statusLabel = new JLabel(" ");

  public OrderFormPanel() {
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4); c.anchor = GridBagConstraints.WEST;
    sideBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        Object display = value == OrderSide.BUY ? "MUA" : value == OrderSide.SELL ? "BÁN" : value;
        return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
      }
    });
    addRow("Đặt lệnh", new JLabel("Đặt lệnh"), c, 0);
    addRow("Mã", symbolField, c, 1);
    addRow("Chiều", sideBox, c, 2);
    addRow("Khối lượng", quantityField, c, 3);
    addRow("", submitButton, c, 4);
    addRow("Trạng thái", statusLabel, c, 5);
  }

  private void addRow(String label, Component component, GridBagConstraints c, int row) {
    c.gridy = row; c.gridx = 0; add(new JLabel(label), c); c.gridx = 1; add(component, c);
  }

  public String getSymbol() { return symbolField.getText().trim(); }
  public void setSymbol(String symbol) { symbolField.setText(symbol); }
  public int getQuantity() { return Integer.parseInt(quantityField.getText().trim()); }
  public void setQuantity(String quantity) { quantityField.setText(quantity); }
  public void setSide(OrderSide side) { sideBox.setSelectedItem(side); }
  public OrderSide getSide() { return (OrderSide) sideBox.getSelectedItem(); }
  public Order createOrder() { return new Order(getSymbol(), getSide(), OrderType.MARKET, getQuantity(), null); }
  public void onSubmit(Consumer<Order> listener) {
    submitButton.addActionListener(event -> {
      try { listener.accept(createOrder()); setStatus("Đã gửi lệnh"); }
      catch (IllegalArgumentException error) { setStatus(error.getMessage()); }
    });
  }
  public void setStatus(String status) { statusLabel.setText(status == null ? " " : status); }
  public JButton getSubmitButton() { return submitButton; }
}
