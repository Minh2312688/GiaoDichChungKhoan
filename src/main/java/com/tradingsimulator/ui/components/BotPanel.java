package com.tradingsimulator.ui.components;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.tradingengine.botstrategies.BollingerBandsStrategy;
import com.tradingsimulator.tradingengine.botstrategies.IBotStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MacdStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MovingAverageCrossoverStrategy;
import com.tradingsimulator.tradingengine.botstrategies.RsiThresholdStrategy;
import com.tradingsimulator.ui.IndicatorDisplayFormatter;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
  private final JComboBox<String> strategyBox;
  private final JCheckBox enableBot;

  public BotPanel(TradingFacade facade) {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(new JLabel("Chiến lược bot"));

    strategyBox = new JComboBox<>(new String[] {MA, RSI, MACD, BOLLINGER});
    add(strategyBox);

    enableBot = new JCheckBox("Bật bot");
    add(enableBot);
    add(botStateLabel);
    add(botStatusLabel);
    add(rsiLabel);

    Runnable applyStrategy = () ->
        facade.setStrategy(createStrategy((String) strategyBox.getSelectedItem()));

    enableBot.addActionListener(event -> {
      facade.setBotEnabled(enableBot.isSelected());
      botStateLabel.setText(enableBot.isSelected() ? "Bot: BẬT" : "Bot: TẮT");
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
  }

  public void clearRsi() {
    rsiLabel.setText("RSI: -");
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
