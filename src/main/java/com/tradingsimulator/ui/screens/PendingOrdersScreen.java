package com.tradingsimulator.ui.screens;

import com.tradingsimulator.model.Order;
import com.tradingsimulator.model.OrderSide;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PendingOrdersScreen extends JPanel {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã", "Chiều", "Loại lệnh", "Khối lượng", "Giá đặt", "Trạng thái"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public PendingOrdersScreen() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JLabel("Danh sách lệnh chờ (LIMIT / STOP-LOSS)"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void showOrders(List<Order> orders) {
        model.setRowCount(0);
        if (orders != null) {
            for (Order order : orders) {
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
    }

    public JTable getTable() {
        return table;
    }
}
