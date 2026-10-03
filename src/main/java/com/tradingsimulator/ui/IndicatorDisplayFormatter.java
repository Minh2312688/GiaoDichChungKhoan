package com.tradingsimulator.ui;

import java.util.Locale;

/** Định dạng chỉ báo kỹ thuật cho lớp giao diện, không thay đổi giá trị tính toán bên trong. */
public final class IndicatorDisplayFormatter {
  private IndicatorDisplayFormatter() { }

  public static String format(double value) {
    if (!Double.isFinite(value)) {
      throw new IllegalArgumentException("Giá trị chỉ báo phải hữu hạn");
    }
    return String.format(Locale.ROOT, "%.2f", value);
  }
}
