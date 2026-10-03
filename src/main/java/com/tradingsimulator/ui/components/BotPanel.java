package com.tradingsimulator.ui.components;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.tradingengine.botstrategies.BollingerBandsStrategy;
import com.tradingsimulator.tradingengine.botstrategies.IBotStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MacdStrategy;
import com.tradingsimulator.tradingengine.botstrategies.MovingAverageCrossoverStrategy;
import com.tradingsimulator.tradingengine.botstrategies.RsiThresholdStrategy;
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

  public BotPanel(TradingFacade facade) {
    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(new JLabel("Chiến lược bot"));

    JComboBox<String> strategyBox = new JComboBox<>(new String[] {MA, RSI, MACD, BOLLINGER});
    add(strategyBox);

    JCheckBox enableBot = new JCheckBox("Bật bot");
    add(enableBot);

    Runnable applyStrategy = () ->
        facade.setStrategy(createStrategy((String) strategyBox.getSelectedItem()));

    enableBot.addActionListener(event -> {
      facade.setBotEnabled(enableBot.isSelected());
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
