package com.tradingsimulator.ui.screens;

import com.tradingsimulator.ui.components.OrderFormPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class TradingPanel extends JPanel {
    private final OrderFormPanel orderForm;

    public TradingPanel() {
        this(List.of());
    }

    public TradingPanel(Collection<String> symbols) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        orderForm = new OrderFormPanel(symbols);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(orderForm);

        add(wrapper, BorderLayout.CENTER);
    }

    public OrderFormPanel getOrderForm() {
        return orderForm;
    }
}
