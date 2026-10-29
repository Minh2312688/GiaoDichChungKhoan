package com.tradingsimulator;

import com.tradingsimulator.auth.User;
import com.tradingsimulator.ui.MainWindow;
import com.tradingsimulator.ui.auth.AuthDialog;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Điểm khởi động ứng dụng: hiển thị hộp thoại đăng nhập / đăng ký và mở giao diện chính sau khi xác thực.
 *
 * <p>Chủ sở hữu: dùng chung (trưởng nhóm).</p>
 */
public final class Main {
  private Main() { }

  public static void main(String[] args) {
    System.out.println("Trình mô phỏng giao dịch - phiên bản MVP offline");
    if (!GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(() -> {
        AuthDialog authDialog = new AuthDialog(null);
        authDialog.setVisible(true);
        if (authDialog.isAuthenticated()) {
          User user = authDialog.getAuthenticatedUser();
          MainWindow window = new MainWindow();
          window.applyUser(user);
          window.setVisible(true);
        } else {
          System.out.println("Đã hủy đăng nhập. Thoát ứng dụng.");
        }
      });
    }
  }
}
