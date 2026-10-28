package com.tradingsimulator.ui.components;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.tradingengine.botstrategies.BollingerBandsStrategy;
import com.tradingsimulator.tradingengine.botstrategies.IBotStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MacdStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MovingAverageCrossoverStrategy;
import com.tradingsimulator.tradingengine.botstrategies.RsiThresholdStrategy;
import com.tradingsimulator.model.OrderSide;
import com.tradingsimulator.ui.IndicatorDisplayFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Panel điều khiển bot: chọn chiến lược (Strategy) và bật/tắt bot.
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public final class BotPanel extends JPanel {
  private static final String MA = "Trung bình động cắt nhau";
  private static final String RSI = "Ngưỡng RSI";
  private static final String MACD = "MACD";
  private static final String BOLLINGER = "Bollinger Bands";
  private final JLabel rsiLabel = new JLabel("RSI: -");
  private final JLabel botStateLabel = new JLabel("Bot: TẮT");
  private final JLabel botStatusLabel = new JLabel("Chờ tín hiệu");
  private final JLabel maSignalLabel = new JLabel("MA: -");
  private final JLabel macdSignalLabel = new JLabel("MACD: -");
  private final JProgressBar rsiGauge = new JProgressBar(0, 100);
  private final JComboBox<String> strategyBox;
  private final JCheckBox enableBot;

  public BotPanel(TradingFacade facade) {
    setLayout(new BorderLayout(12, 14));
    setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createLineBorder(new Color(0x2b, 0x31, 0x3a)),
        javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
    setPreferredSize(new java.awt.Dimension(560, 250));

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
    controls.add(new JLabel("Chiến lược"));
    strategyBox = new JComboBox<>(new String[] {MA, RSI, MACD, BOLLINGER});
    strategyBox.setPreferredSize(new java.awt.Dimension(230, 34));
    controls.add(strategyBox);

    enableBot = new JCheckBox("Bật bot");
    enableBot.setFocusPainted(false);
    controls.add(enableBot);
    add(controls, BorderLayout.NORTH);

    JPanel statusCard = new JPanel(new BorderLayout(8, 8));
    statusCard.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 4, 6, 4));
    botStateLabel.setFont(botStateLabel.getFont().deriveFont(Font.BOLD, 18f));
    botStateLabel.setForeground(new Color(0x84, 0x8e, 0x9c));
    botStatusLabel.setForeground(new Color(0x84, 0x8e, 0x9c));
    statusCard.add(botStateLabel, BorderLayout.NORTH);
    statusCard.add(botStatusLabel, BorderLayout.CENTER);

    JPanel indicator = new JPanel(new BorderLayout(8, 8));
    rsiLabel.setFont(rsiLabel.getFont().deriveFont(Font.BOLD));
    rsiGauge.setStringPainted(false);
    rsiGauge.setBorderPainted(false);
    rsiGauge.setPreferredSize(new java.awt.Dimension(0, 12));
    indicator.add(rsiLabel, BorderLayout.NORTH);
    indicator.add(rsiGauge, BorderLayout.CENTER);
    JPanel trendReadouts = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
    maSignalLabel.setForeground(new Color(0x84, 0x8e, 0x9c));
    macdSignalLabel.setForeground(new Color(0x84, 0x8e, 0x9c));
    trendReadouts.add(maSignalLabel);
    trendReadouts.add(macdSignalLabel);
    JPanel lowerIndicators = new JPanel(new BorderLayout(0, 8));
    lowerIndicators.add(indicator, BorderLayout.NORTH);
    lowerIndicators.add(trendReadouts, BorderLayout.SOUTH);
    statusCard.add(lowerIndicators, BorderLayout.SOUTH);
    add(statusCard, BorderLayout.CENTER);

    Runnable applyStrategy = () ->
        facade.setStrategy(createStrategy((String) strategyBox.getSelectedItem()));

    enableBot.addActionListener(event -> {
      facade.setBotEnabled(enableBot.isSelected());
      botStateLabel.setText(enableBot.isSelected() ? "Bot: BẬT" : "Bot: TẮT");
      botStateLabel.setForeground(enableBot.isSelected()
          ? new Color(0x0e, 0xcb, 0x81) : new Color(0x84, 0x8e, 0x9c));
      botStatusLabel.setText(enableBot.isSelected() ? "Chờ tín hiệu" : "Đã tắt");
      if (enableBot.isSelected()) {
        applyStrategy.run();
      }
    });

    strategyBox.addActionListener(event -> {
      if (enableBot.isSelected()) {
        applyStrategy.run();
      }
    });
  }

  public void showRsi(double rsi) {
    rsiLabel.setText("RSI: " + IndicatorDisplayFormatter.format(rsi));
    rsiGauge.setValue((int) Math.round(Math.max(0, Math.min(100, rsi))));
    rsiGauge.setForeground(rsi < 30 ? new Color(0x0e, 0xcb, 0x81)
        : rsi > 70 ? new Color(0xf6, 0x46, 0x5d) : new Color(0xf0, 0xb9, 0x0b));
  }

  public void clearRsi() {
    rsiLabel.setText("RSI: -");
    rsiGauge.setValue(0);
  }

  public void showStrategyIndicators(OrderSide ma, OrderSide macd) {
    maSignalLabel.setText("MA trend: " + signalName(ma));
    macdSignalLabel.setText("MACD: " + signalName(macd));
    maSignalLabel.setForeground(signalColor(ma));
    macdSignalLabel.setForeground(signalColor(macd));
  }

  private static String signalName(OrderSide side) {
    return side == null ? "Chờ dữ liệu" : side == OrderSide.BUY ? "TĂNG" : "GIẢM";
  }

  private static Color signalColor(OrderSide side) {
    return side == OrderSide.BUY ? new Color(0x0e, 0xcb, 0x81)
        : side == OrderSide.SELL ? new Color(0xf6, 0x46, 0x5d)
        : new Color(0x84, 0x8e, 0x9c);
  }

  public JLabel getRsiLabel() {
    return rsiLabel;
  }

  public boolean isBotEnabled() {
    return enableBot.isSelected();
  }

  public void setBotEnabled(boolean enabled) {
    if (enableBot.isSelected() != enabled) {
      enableBot.doClick();
    }
  }

  public void showBotStatus(String status) {
    botStatusLabel.setText(status);
    if (status != null && status.toLowerCase(java.util.Locale.ROOT).contains("lỗi")) {
      botStatusLabel.setForeground(new Color(0xf6, 0x46, 0x5d));
    } else if (status != null && (status.contains("Đã đặt") || status.contains("ORDER_SUBMITTED"))) {
      botStatusLabel.setForeground(new Color(0x0e, 0xcb, 0x81));
    } else {
      botStatusLabel.setForeground(new Color(0x84, 0x8e, 0x9c));
    }
  }

  public JLabel getBotStateLabel() {
    return botStateLabel;
  }

  public JLabel getBotStatusLabel() {
    return botStatusLabel;
  }

  private static IBotStrategy createStrategy(String selected) {
    if (RSI.equals(selected)) {
      return new RsiThresholdStrategy();
    }
    if (MACD.equals(selected)) {
      return new MacdStrategy();
    }
    if (BOLLINGER.equals(selected)) {
      return new BollingerBandsStrategy();
    }
    return new MovingAverageCrossoverStrategy();
  }
}
