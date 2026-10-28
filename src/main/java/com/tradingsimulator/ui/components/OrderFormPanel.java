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
  private final JToggleButton buyButton = new JToggleButton("MUA");
  private final JToggleButton sellButton = new JToggleButton("BÁN");
  private final JComboBox<OrderType> typeBox = new JComboBox<>(OrderType.values());
  private final JTextField priceField = new JTextField("0.00", 8);
  private final JButton submitButton = new JButton("Gửi lệnh");
  private final JLabel statusLabel = new JLabel(" ");

  public OrderFormPanel() {
    this(List.of());
  }

  public OrderFormPanel(Collection<String> symbols) {
    setLayout(new BorderLayout(12, 14));
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
        BorderFactory.createEmptyBorder(18, 18, 18, 18)));
    setPreferredSize(new Dimension(340, 400));
    setSymbols(symbols);
    ButtonGroup sides = new ButtonGroup();
    sides.add(buyButton);
    sides.add(sellButton);
    buyButton.setSelected(true);
    buyButton.setFocusPainted(false);
    sellButton.setFocusPainted(false);
    buyButton.setBackground(new Color(0x0e, 0xcb, 0x81));
    buyButton.setForeground(new Color(0x12, 0x17, 0x21));
    sellButton.setBackground(new Color(0xf6, 0x46, 0x5d));
    sellButton.setForeground(Color.WHITE);
    buyButton.setOpaque(true);
    sellButton.setOpaque(true);
    buyButton.setPreferredSize(new Dimension(135, 38));
    sellButton.setPreferredSize(new Dimension(135, 38));
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
    quantityField.setColumns(10);
    priceField.setColumns(10);
    quantityField.setHorizontalAlignment(JTextField.RIGHT);
    priceField.setHorizontalAlignment(JTextField.RIGHT);

    JLabel title = new JLabel("Đặt lệnh");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
    add(title, BorderLayout.NORTH);

    JPanel fields = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(7, 0, 7, 0);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    addRow(fields, "Mã chứng khoán", symbolBox, c, 0);
    JPanel sideButtons = new JPanel(new GridLayout(1, 2, 8, 0));
    sideButtons.add(buyButton);
    sideButtons.add(sellButton);
    addRow(fields, "Chiều giao dịch", sideButtons, c, 1);
    addRow(fields, "Loại lệnh", typeBox, c, 2);

    JPanel quantityControls = new JPanel(new BorderLayout(5, 0));
    JButton minusOne = new JButton("−1");
    JButton plusOne = new JButton("+1");
    JButton plusTen = new JButton("+10");
    minusOne.addActionListener(event -> adjustQuantity(-1));
    plusOne.addActionListener(event -> adjustQuantity(1));
    plusTen.addActionListener(event -> adjustQuantity(10));
    JPanel quantityButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    quantityButtons.add(minusOne);
    quantityButtons.add(plusOne);
    quantityButtons.add(plusTen);
    quantityControls.add(quantityField, BorderLayout.CENTER);
    quantityControls.add(quantityButtons, BorderLayout.EAST);
    addRow(fields, "Số lượng", quantityControls, c, 3);
    addRow(fields, "Giá điều kiện", priceField, c, 4);
    add(fields, BorderLayout.CENTER);

    JPanel footer = new JPanel(new BorderLayout(0, 10));
    submitButton.setFont(submitButton.getFont().deriveFont(Font.BOLD));
    submitButton.setPreferredSize(new Dimension(0, 42));
    statusLabel.setOpaque(true);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
    setStatus("Sẵn sàng đặt lệnh");
    footer.add(submitButton, BorderLayout.NORTH);
    footer.add(statusLabel, BorderLayout.CENTER);
    add(footer, BorderLayout.SOUTH);
    updatePriceFieldState();
  }

  private void addRow(
      JPanel panel, String label, Component component, GridBagConstraints c, int row) {
    c.gridy = row;
    c.gridx = 0;
    c.weightx = 1;
    panel.add(new JLabel(label), c);
    c.gridx = 1;
    panel.add(component, c);
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

  public void addSymbolSelectionListener(Consumer<String> listener) {
    Consumer<String> selectionListener = java.util.Objects.requireNonNull(listener, "listener");
    symbolBox.addActionListener(event -> {
      Object selected = symbolBox.getSelectedItem();
      if (selected != null) selectionListener.accept(selected.toString());
    });
  }

  public int getQuantity() { return Integer.parseInt(quantityField.getText().trim()); }
  public void setQuantity(String quantity) { quantityField.setText(quantity); }
  public void setSide(OrderSide side) {
    if (side == OrderSide.BUY) buyButton.setSelected(true);
    if (side == OrderSide.SELL) sellButton.setSelected(true);
  }
  public OrderSide getSide() {
    if (buyButton.isSelected()) return OrderSide.BUY;
    if (sellButton.isSelected()) return OrderSide.SELL;
    return null;
  }

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

  private void adjustQuantity(int amount) {
    try {
      int quantity = Integer.parseInt(quantityField.getText().trim());
      quantityField.setText(Integer.toString(Math.max(1, quantity + amount)));
    } catch (NumberFormatException error) {
      setStatus("Số lượng phải là số nguyên dương");
    }
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
  public void setStatus(String status) {
    statusLabel.setText(status == null ? " " : status);
    boolean isError = status != null && !status.startsWith("Sẵn sàng")
        && !status.startsWith("Đã ");
    statusLabel.setForeground(isError
        ? new Color(0xf6, 0x46, 0x5d) : new Color(0x0e, 0xcb, 0x81));
    statusLabel.setBackground(new Color(0x2b, 0x21, 0x27));
  }
  public JButton getSubmitButton() { return submitButton; }
  public JLabel getStatusLabel() { return statusLabel; }
}
