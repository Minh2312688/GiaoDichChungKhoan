package com.tradingsimulator;

import com.tradingsimulator.core.ExchangeEngine;
import com.tradingsimulator.ui.MainWindow;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Điểm khởi động ứng dụng: in thông báo, nạp tiền ban đầu và mở giao diện khi có tham số --ui.
 *
 * <p>Chủ sở hữu: dùng chung (trưởng nhóm).</p>
 */
public final class Main {
  private Main() { }

  public static void main(String[] args) {
    System.out.println("Trading Simulator offline MVP");
    ExchangeEngine.getInstance().reset(new BigDecimal("10000"));
    if (Arrays.asList(args).contains("--ui") && !GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
  }
}
