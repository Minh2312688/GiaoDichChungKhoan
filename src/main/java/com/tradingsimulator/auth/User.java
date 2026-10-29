package com.tradingsimulator.auth;

import com.tradingsimulator.validationandportfolio.PortfolioMemento;

/**
 * Đại diện cho thông tin người dùng trong hệ thống mô phỏng giao dịch.
 * Lưu trữ thông tin tài khoản, gmail và snapshot danh mục đầu tư (Memento) tương ứng.
 */
public class User {
    private final String username;
    private String email;
    private String password;
    private String fullName;
    private PortfolioMemento portfolio;

    public User(String username, String email, String password, String fullName, PortfolioMemento portfolio) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        this.username = username;
        this.email = (email != null && !email.isBlank()) ? email.trim() : username + "@gmail.com";
        this.password = password != null ? password : "";
        this.fullName = (fullName != null && !fullName.isBlank()) ? fullName : username;
        this.portfolio = portfolio;
    }

    public User(String username, String password, String fullName, PortfolioMemento portfolio) {
        this(username, username + "@gmail.com", password, fullName, portfolio);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public PortfolioMemento getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(PortfolioMemento portfolio) {
        this.portfolio = portfolio;
    }
}
