package com.tradingsimulator.ui.auth;

import com.tradingsimulator.auth.AuthService;
import com.tradingsimulator.auth.User;
import com.tradingsimulator.ui.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Hộp thoại đăng nhập và đăng ký tài khoản cho ứng dụng mô phỏng giao dịch.
 * Thiết kế giao diện Dark Theme đồng bộ, có tính năng đăng nhập nhanh tài khoản demo
 * kèm dữ liệu mẫu phong phú để thuận tiện quay video báo cáo/demo.
 */
public class AuthDialog extends JDialog {
    private static final Color BG_COLOR = new Color(0x18, 0x1a, 0x20);
    private static final Color PANEL_COLOR = new Color(0x1e, 0x23, 0x29);
    private static final Color BORDER_COLOR = new Color(0x2b, 0x31, 0x3a);
    private static final Color GOLD = new Color(0xf0, 0xb9, 0x0b);
    private static final Color GREEN = new Color(0x0e, 0xcb, 0x81);
    private static final Color RED = new Color(0xf6, 0x46, 0x5d);
    private static final Color TEXT_MUTED = new Color(0x84, 0x8e, 0x9c);
    private static final Color TEXT_LIGHT = new Color(0xea, 0xec, 0xef);

    private final AuthService authService = AuthService.getInstance();
    private boolean authenticated = false;
    private User authenticatedUser;

    // Login Form Fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JLabel loginErrorLabel;

    // Register Form Fields
    private JTextField regUsernameField;
    private JTextField regEmailField;
    private JTextField regFullNameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JLabel regMessageLabel;

    private JTabbedPane tabbedPane;

    public AuthDialog(Window owner) {
        super(owner, "Đăng nhập / Đăng ký - Trading Simulator", ModalityType.APPLICATION_MODAL);
        ThemeManager.setupLookAndFeel();
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_COLOR);
        root.setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. Header Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel titleLabel = new JLabel("◆ TRADING SIMULATOR");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(GOLD);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("HỆ THỐNG MÔ PHỎNG GIAO DỊCH CHỨNG KHOÁN");
        subTitleLabel.setFont(subTitleLabel.getFont().deriveFont(Font.BOLD, 10f));
        subTitleLabel.setForeground(TEXT_MUTED);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subTitleLabel);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Tabs: Đăng nhập & Đăng ký
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(PANEL_COLOR);
        tabbedPane.setForeground(TEXT_LIGHT);
        tabbedPane.setFont(tabbedPane.getFont().deriveFont(Font.BOLD, 12f));

        tabbedPane.addTab("  Đăng nhập  ", buildLoginPanel());
        tabbedPane.addTab("  Đăng ký  ", buildRegisterPanel());

        root.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Tên đăng nhập hoặc Gmail
        JLabel userLabel = new JLabel("Tên đăng nhập hoặc Gmail:");
        userLabel.setForeground(TEXT_LIGHT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 12f));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginUsernameField = new JTextField("demo");
        loginUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginUsernameField.setPreferredSize(new Dimension(360, 36));
        loginUsernameField.setBackground(new Color(0x2b, 0x31, 0x3a));
        loginUsernameField.setForeground(Color.WHITE);
        loginUsernameField.setCaretColor(GOLD);
        loginUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mật khẩu
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setForeground(TEXT_LIGHT);
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD, 12f));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginPasswordField = new JPasswordField("123");
        loginPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginPasswordField.setPreferredSize(new Dimension(360, 36));
        loginPasswordField.setBackground(new Color(0x2b, 0x31, 0x3a));
        loginPasswordField.setForeground(Color.WHITE);
        loginPasswordField.setCaretColor(GOLD);
        loginPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setForeground(RED);
        loginErrorLabel.setFont(loginErrorLabel.getFont().deriveFont(Font.PLAIN, 11f));
        loginErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút Đăng nhập
        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD, 13f));
        loginButton.setBackground(GOLD);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setPreferredSize(new Dimension(360, 40));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        // Nút Đăng nhập với Gmail
        JButton gmailLoginButton = new JButton("G   Đăng nhập với Gmail");
        gmailLoginButton.setFont(gmailLoginButton.getFont().deriveFont(Font.BOLD, 12f));
        gmailLoginButton.setBackground(new Color(0xd9, 0x30, 0x25));
        gmailLoginButton.setForeground(Color.WHITE);
        gmailLoginButton.setFocusPainted(false);
        gmailLoginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        gmailLoginButton.setPreferredSize(new Dimension(360, 38));
        gmailLoginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gmailLoginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        gmailLoginButton.addActionListener(e -> handleGmailLogin());

        // Box thông tin demo
        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(BG_COLOR);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel infoTitle = new JLabel("📌 Tài khoản Demo có sẵn dữ liệu quay demo:");
        infoTitle.setFont(infoTitle.getFont().deriveFont(Font.BOLD, 11f));
        infoTitle.setForeground(GOLD);

        JLabel infoRow1 = new JLabel("• Tài khoản: demo (hoặc demo.trader@gmail.com) / Mật khẩu: 123");
        infoRow1.setFont(infoRow1.getFont().deriveFont(Font.PLAIN, 11f));
        infoRow1.setForeground(TEXT_LIGHT);

        JLabel infoRow2 = new JLabel("• Tiền mặt $25,000, đang giữ AAPL (50), BTC (1), ETH (5)");
        infoRow2.setFont(infoRow2.getFont().deriveFont(Font.PLAIN, 11f));
        infoRow2.setForeground(TEXT_LIGHT);

        JLabel infoRow3 = new JLabel("• Đầy đủ lịch sử 4 giao dịch và 3 lệnh chờ khớp (Limit / Stop-loss)");
        infoRow3.setFont(infoRow3.getFont().deriveFont(Font.PLAIN, 11f));
        infoRow3.setForeground(TEXT_MUTED);

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(infoRow1);
        infoCard.add(Box.createVerticalStrut(2));
        infoCard.add(infoRow2);
        infoCard.add(Box.createVerticalStrut(2));
        infoCard.add(infoRow3);

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        loginUsernameField.addKeyListener(enterKey);
        loginPasswordField.addKeyListener(enterKey);

        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginUsernameField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginPasswordField);
        panel.add(Box.createVerticalStrut(4));
        panel.add(loginErrorLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(gmailLoginButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(infoCard);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Tên đăng nhập
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setForeground(TEXT_LIGHT);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 12f));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        regUsernameField = new JTextField();
        regUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regUsernameField.setPreferredSize(new Dimension(360, 34));
        regUsernameField.setBackground(new Color(0x2b, 0x31, 0x3a));
        regUsernameField.setForeground(Color.WHITE);
        regUsernameField.setCaretColor(GOLD);
        regUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Gmail
        JLabel emailLabel = new JLabel("Gmail:");
        emailLabel.setForeground(TEXT_LIGHT);
        emailLabel.setFont(emailLabel.getFont().deriveFont(Font.BOLD, 12f));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        regEmailField = new JTextField();
        regEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regEmailField.setPreferredSize(new Dimension(360, 34));
        regEmailField.setBackground(new Color(0x2b, 0x31, 0x3a));
        regEmailField.setForeground(Color.WHITE);
        regEmailField.setCaretColor(GOLD);
        regEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Họ tên
        JLabel nameLabel = new JLabel("Họ và tên nhà đầu tư:");
        nameLabel.setForeground(TEXT_LIGHT);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        regFullNameField = new JTextField();
        regFullNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regFullNameField.setPreferredSize(new Dimension(360, 34));
        regFullNameField.setBackground(new Color(0x2b, 0x31, 0x3a));
        regFullNameField.setForeground(Color.WHITE);
        regFullNameField.setCaretColor(GOLD);
        regFullNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mật khẩu
        JLabel passLabel = new JLabel("Mật khẩu (ít nhất 3 ký tự):");
        passLabel.setForeground(TEXT_LIGHT);
        passLabel.setFont(passLabel.getFont().deriveFont(Font.BOLD, 12f));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        regPasswordField = new JPasswordField();
        regPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regPasswordField.setPreferredSize(new Dimension(360, 34));
        regPasswordField.setBackground(new Color(0x2b, 0x31, 0x3a));
        regPasswordField.setForeground(Color.WHITE);
        regPasswordField.setCaretColor(GOLD);
        regPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Xác nhận mật khẩu
        JLabel confirmPassLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPassLabel.setForeground(TEXT_LIGHT);
        confirmPassLabel.setFont(confirmPassLabel.getFont().deriveFont(Font.BOLD, 12f));
        confirmPassLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        regConfirmPasswordField = new JPasswordField();
        regConfirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regConfirmPasswordField.setPreferredSize(new Dimension(360, 34));
        regConfirmPasswordField.setBackground(new Color(0x2b, 0x31, 0x3a));
        regConfirmPasswordField.setForeground(Color.WHITE);
        regConfirmPasswordField.setCaretColor(GOLD);
        regConfirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        regMessageLabel = new JLabel(" ");
        regMessageLabel.setForeground(RED);
        regMessageLabel.setFont(regMessageLabel.getFont().deriveFont(Font.PLAIN, 11f));
        regMessageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút Đăng ký
        JButton regButton = new JButton("Đăng ký tài khoản");
        regButton.setFont(regButton.getFont().deriveFont(Font.BOLD, 13f));
        regButton.setBackground(GREEN);
        regButton.setForeground(Color.WHITE);
        regButton.setFocusPainted(false);
        regButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regButton.setPreferredSize(new Dimension(360, 38));
        regButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        regButton.addActionListener(e -> handleRegister());

        // Note
        JPanel noteCard = new JPanel();
        noteCard.setLayout(new BoxLayout(noteCard, BoxLayout.Y_AXIS));
        noteCard.setBackground(BG_COLOR);
        noteCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(6, 12, 6, 12)
        ));
        noteCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel noteText = new JLabel("ℹ️ Tài khoản đăng ký mới sẽ nhận ngay $10,000.00 tiền vốn ban đầu.");
        noteText.setFont(noteText.getFont().deriveFont(Font.PLAIN, 11f));
        noteText.setForeground(TEXT_MUTED);
        noteCard.add(noteText);

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleRegister();
                }
            }
        };
        regUsernameField.addKeyListener(enterKey);
        regEmailField.addKeyListener(enterKey);
        regFullNameField.addKeyListener(enterKey);
        regPasswordField.addKeyListener(enterKey);
        regConfirmPasswordField.addKeyListener(enterKey);

        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regUsernameField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(emailLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regEmailField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regFullNameField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(confirmPassLabel);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regConfirmPasswordField);
        panel.add(Box.createVerticalStrut(3));
        panel.add(regMessageLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(regButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(noteCard);

        return panel;
    }

    private void handleLogin() {
        loginErrorLabel.setText(" ");
        String identifier = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        try {
            User user = authService.login(identifier, password);
            this.authenticated = true;
            this.authenticatedUser = user;
            dispose();
        } catch (IllegalArgumentException ex) {
            loginErrorLabel.setText("⚠ " + ex.getMessage());
        }
    }

    private void handleGmailLogin() {
        loginErrorLabel.setText(" ");
        String identifier = loginUsernameField.getText().trim();
        if (!identifier.toLowerCase().endsWith("@gmail.com")) {
            String input = JOptionPane.showInputDialog(
                this,
                "Nhập địa chỉ Gmail thật của bạn để đăng nhập:",
                "Đăng nhập với Gmail",
                JOptionPane.QUESTION_MESSAGE
            );
            if (input == null || input.trim().isEmpty()) {
                return;
            }
            identifier = input.trim();
            loginUsernameField.setText(identifier);
        }

        try {
            User user = authService.loginWithGoogle(identifier);
            this.authenticated = true;
            this.authenticatedUser = user;
            dispose();
        } catch (IllegalArgumentException ex) {
            loginErrorLabel.setText("⚠ " + ex.getMessage());
        }
    }

    private void handleRegister() {
        regMessageLabel.setForeground(RED);
        regMessageLabel.setText(" ");

        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String fullName = regFullNameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmPasswordField.getPassword());

        if (username.isEmpty()) {
            regMessageLabel.setText("⚠ Tên đăng nhập không được để trống.");
            return;
        }
        if (password.length() < 3) {
            regMessageLabel.setText("⚠ Mật khẩu phải có ít nhất 3 ký tự.");
            return;
        }
        if (!password.equals(confirm)) {
            regMessageLabel.setText("⚠ Mật khẩu xác nhận không khớp.");
            return;
        }

        try {
            User newUser = authService.register(username, email, password, fullName);
            regMessageLabel.setForeground(GREEN);
            regMessageLabel.setText("✔ Đăng ký thành công! Đã tự động đăng nhập...");

            // Auto-login registered user and close dialog
            Timer timer = new Timer(600, e -> {
                this.authenticated = true;
                this.authenticatedUser = newUser;
                this.authService.setCurrentUser(newUser);
                dispose();
            });
            timer.setRepeats(false);
            timer.start();
        } catch (IllegalArgumentException ex) {
            regMessageLabel.setForeground(RED);
            regMessageLabel.setText("⚠ " + ex.getMessage());
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public JTextField getLoginUsernameField() {
        return loginUsernameField;
    }

    public JPasswordField getLoginPasswordField() {
        return loginPasswordField;
    }

    public JTextField getRegUsernameField() {
        return regUsernameField;
    }

    public JTextField getRegEmailField() {
        return regEmailField;
    }

    public JPasswordField getRegPasswordField() {
        return regPasswordField;
    }

    public JPasswordField getRegConfirmPasswordField() {
        return regConfirmPasswordField;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
