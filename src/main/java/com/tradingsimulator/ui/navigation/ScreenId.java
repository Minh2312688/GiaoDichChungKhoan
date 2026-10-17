package com.tradingsimulator.ui.navigation;

public enum ScreenId {
    DASHBOARD("Tổng quan"),
    TRADING("Đặt lệnh"),
    BOT("Bot giao dịch"),
    HISTORY("Lịch sử"),
    PENDING_ORDERS("Lệnh chờ"),
    SETTINGS("Cài đặt");

    private final String title;

    ScreenId(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
