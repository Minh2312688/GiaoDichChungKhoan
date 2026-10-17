package com.tradingsimulator.ui.screens;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final JComboBox<String> themeBox;
    private final JTextField delayField;
    private int refreshDelay = 2000;
    private String themeMode = "dark";

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Cài đặt hệ thống"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        themeBox = new JComboBox<>(new String[]{"dark", "light"});
        themeBox.setSelectedItem(themeMode);
        themeBox.addActionListener(e -> themeMode = (String) themeBox.getSelectedItem());

        delayField = new JTextField(String.valueOf(refreshDelay), 10);
        JButton applyDelayButton = new JButton("Áp dụng chu kỳ");
        applyDelayButton.addActionListener(e -> {
            try {
                int val = Integer.parseInt(delayField.getText().trim());
                setRefreshDelay(val);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Chu kỳ phải là số nguyên dương", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        c.gridx = 0; c.gridy = 0;
        formPanel.add(new JLabel("Giao diện (Theme):"), c);
        c.gridx = 1;
        formPanel.add(themeBox, c);

        c.gridx = 0; c.gridy = 1;
        formPanel.add(new JLabel("Chu kỳ cập nhật giá (ms):"), c);
        c.gridx = 1;
        JPanel delayGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        delayGroup.add(delayField);
        delayGroup.add(applyDelayButton);
        formPanel.add(delayGroup, c);

        add(formPanel, BorderLayout.NORTH);
    }

    public String themeMode() {
        return themeMode;
    }

    public void setThemeMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("Theme mode must not be blank");
        }
        this.themeMode = mode;
        this.themeBox.setSelectedItem(mode);
    }

    public int refreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(int delayMillis) {
        if (delayMillis <= 0) {
            throw new IllegalArgumentException("Refresh delay must be positive");
        }
        this.refreshDelay = delayMillis;
        this.delayField.setText(String.valueOf(delayMillis));
    }
}
