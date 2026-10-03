package com.tradingsimulator.tradingengine.botstrategies; /**
 * Lớp cơ sở cho các chiến lược: giữ chu kỳ (period) và kiểm tra hợp lệ (mẫu Strategy).
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public abstract class BaseStrategy implements IBotStrategy { protected final int period; protected BaseStrategy(int p){if(p<1)throw new IllegalArgumentException("Period must be positive");period=p;} }
