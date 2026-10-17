package com.tradingsimulator.ui.screens;

import com.tradingsimulator.ui.components.OrderFormPanel;

import javax.swing.*;
import java.awt.*;

public class TradingPanel extends JPanel {
    private final OrderFormPanel orderForm;

    public TradingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        orderForm = new OrderFormPanel();

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(orderForm);

        add(wrapper, BorderLayout.CENTER);
    }

    public OrderFormPanel getOrderForm() {
        return orderForm;
    }
}
