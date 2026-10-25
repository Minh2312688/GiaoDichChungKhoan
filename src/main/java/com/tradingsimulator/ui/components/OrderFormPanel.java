package com.tradingsimulator.ui.components;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.model.OrderType;
import com.tradingsimulator.tradingengine.OrderFactory;

/**
 * Form đặt lệnh trên giao diện (dùng {@code OrderFactory} để tạo lệnh).
 *
 * <p>Chủ sở hữu: Thành viên 1 (mẫu Factory Method).</p>
 */
public final class OrderFormPanel extends JPanel {
  private final OrderFactory orderFactory = new OrderFactory();
  private final JComboBox<String> symbolBox = new JComboBox<>();
  private final JTextField quantityField = new JTextField("1", 8);
  private final JComboBox<OrderSide> sideBox = new JComboBox<>(OrderSide.values());
  private final JComboBox<OrderType> typeBox = new JComboBox<>(OrderType.values());
  private final JTextField priceField = new JTextField("0.00", 8);
  private final JButton submitButton = new JButton("Gửi lệnh");
  private final JLabel statusLabel = new JLabel(" ");

  public OrderFormPanel() {
    this(List.of());
  }

  public OrderFormPanel(Collection<String> symbols) {
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4); c.anchor = GridBagConstraints.WEST;
    setSymbols(symbols);
    sideBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        Object display = value == OrderSide.BUY ? "MUA" : value == OrderSide.SELL ? "BÁN" : value;
        return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
      }
    });
    typeBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        String display = switch ((OrderType) value) {
          case MARKET -> "Thị trường";
          case LIMIT -> "Giới hạn";
          case STOP_LOSS -> "Dừng lỗ";
        };
        return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
      }
    });
    typeBox.addActionListener(event -> updatePriceFieldState());
    symbolBox.setToolTipText("Chọn mã chứng khoán trong danh sách thị trường");
    priceField.setToolTipText("Giá giới hạn hoặc giá kích hoạt dừng lỗ");
    addRow("Đặt lệnh", new JLabel("Đặt lệnh"), c, 0);
    addRow("Mã chứng khoán", symbolBox, c, 1);
    addRow("Chiều", sideBox, c, 2);
    addRow("Loại lệnh", typeBox, c, 3);
    addRow("Số lượng", quantityField, c, 4);
    addRow("Giá điều kiện", priceField, c, 5);
    addRow("", submitButton, c, 6);
    addRow("Trạng thái", statusLabel, c, 7);
    updatePriceFieldState();
  }

  private void addRow(String label, Component component, GridBagConstraints c, int row) {
    c.gridy = row; c.gridx = 0; add(new JLabel(label), c); c.gridx = 1; add(component, c);
  }

  public String getSymbol() {
    Object selected = symbolBox.getSelectedItem();
    if (selected == null || selected.toString().isBlank()) {
      throw new IllegalArgumentException("Vui lòng chọn mã chứng khoán");
    }
    return selected.toString();
  }

  public void setSymbol(String symbol) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("Mã chứng khoán không được để trống");
    }
    String normalized = symbol.trim();
    boolean isAvailable = false;
    for (int index = 0; index < symbolBox.getItemCount(); index++) {
      if (normalized.equals(symbolBox.getItemAt(index))) {
        isAvailable = true;
        break;
      }
    }
    if (!isAvailable) {
      symbolBox.addItem(normalized);
    }
    symbolBox.setSelectedItem(normalized);
  }

  public void setSymbols(Collection<String> symbols) {
    if (symbols == null) {
      throw new IllegalArgumentException("Danh sách mã chứng khoán không được để trống");
    }
    TreeSet<String> sortedSymbols = new TreeSet<>();
    for (String symbol : symbols) {
      if (symbol == null || symbol.isBlank()) {
        throw new IllegalArgumentException("Danh sách có mã chứng khoán không hợp lệ");
      }
      sortedSymbols.add(symbol.trim());
    }
    String selected = getSelectedSymbolOrNull();
    symbolBox.removeAllItems();
    sortedSymbols.forEach(symbolBox::addItem);
    if (selected != null && sortedSymbols.contains(selected)) {
      symbolBox.setSelectedItem(selected);
    }
  }

  public int getQuantity() { return Integer.parseInt(quantityField.getText().trim()); }
  public void setQuantity(String quantity) { quantityField.setText(quantity); }
  public void setSide(OrderSide side) { sideBox.setSelectedItem(side); }
  public OrderSide getSide() { return (OrderSide) sideBox.getSelectedItem(); }

  public void setOrderType(OrderType type) { typeBox.setSelectedItem(type); }
  public OrderType getOrderType() { return (OrderType) typeBox.getSelectedItem(); }
  public void setPrice(String price) { priceField.setText(price); }
  public JTextField getPriceField() { return priceField; }

  public Order createOrder() {
    String symbol = getSymbol();
    OrderSide side = getSide();
    int quantity = getQuantity();
    return switch (getOrderType()) {
      case MARKET -> orderFactory.market(symbol, side, quantity);
      case LIMIT -> orderFactory.limit(symbol, side, quantity, getPrice());
      case STOP_LOSS -> orderFactory.stopLoss(symbol, side, quantity, getPrice());
    };
  }

  private BigDecimal getPrice() {
    try {
      return new BigDecimal(priceField.getText().trim());
    } catch (NumberFormatException error) {
      throw new IllegalArgumentException("Giá điều kiện phải là một số hợp lệ", error);
    }
  }

  private String getSelectedSymbolOrNull() {
    Object selected = symbolBox.getSelectedItem();
    return selected == null ? null : selected.toString();
  }

  private void updatePriceFieldState() {
    OrderType selected = getOrderType();
    priceField.setEnabled(selected == OrderType.LIMIT || selected == OrderType.STOP_LOSS);
  }

  public void onSubmit(Consumer<Order> listener) {
    submitButton.addActionListener(event -> {
      try {
        Order order = createOrder();
        listener.accept(order);
        setStatus(order.type() == OrderType.MARKET ? "Đã gửi lệnh thị trường" : "Đã thêm lệnh chờ");
      }
      catch (IllegalArgumentException error) { setStatus(error.getMessage()); }
    });
  }
  public void setStatus(String status) { statusLabel.setText(status == null ? " " : status); }
  public JButton getSubmitButton() { return submitButton; }
  public JLabel getStatusLabel() { return statusLabel; }
}
