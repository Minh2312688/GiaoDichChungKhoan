package com.tradingsimulator.ui.screens;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PendingOrdersScreen extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã", "Chiều", "Loại lệnh", "Số lượng", "Giá đặt", "Trạng thái"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<Order> orders = List.of();
    private String activeFilter = "ALL";

    public PendingOrdersScreen() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel title = new JLabel("Lệnh chờ");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(title, BorderLayout.NORTH);
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        addFilter(filters, "Tất cả", "ALL");
        addFilter(filters, "Lệnh mua", "BUY");
        addFilter(filters, "Lệnh bán", "SELL");
        addFilter(filters, "Đang chờ", "PENDING");
        header.add(filters, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
        com.tradingsimulator.ui.components.TradingTableStyle.apply(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable source, Object value, boolean selected, boolean focus, int row, int column) {
                super.getTableCellRendererComponent(source, value, selected, focus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(6, 8, 6, 8),
                        BorderFactory.createLineBorder(new Color(0x5a, 0x49, 0x1d))));
                if (!selected) {
                    setForeground(new Color(0xf0, 0xb9, 0x0b));
                    setBackground(new Color(0x2d, 0x28, 0x1d));
                }
                return this;
            }
        });
    }

    public void showOrders(List<Order> orders) {
        this.orders = orders == null ? List.of() : List.copyOf(orders);
        renderOrders();
    }

    private void renderOrders() {
        model.setRowCount(0);
        for (Order order : orders) {
            if ("BUY".equals(activeFilter) && order.side() != OrderSide.BUY) continue;
            if ("SELL".equals(activeFilter) && order.side() != OrderSide.SELL) continue;
            String sideStr = order.side() == OrderSide.BUY ? "MUA" : "BÁN";
            String priceStr = order.price() != null ? order.price().toPlainString() : "-";
            model.addRow(new Object[]{
                    order.symbol(),
                    sideStr,
                    order.type().name(),
                    order.quantity(),
                    priceStr,
                    "CHỜ KHỚP"
            });
        }
    }

    private void addFilter(JPanel filters, String title, String filter) {
        JButton button = new JButton(title);
        button.addActionListener(event -> {
            activeFilter = filter;
            renderOrders();
        });
        filters.add(button);
    }

    public JTable getTable() {
        return table;
    }
}
