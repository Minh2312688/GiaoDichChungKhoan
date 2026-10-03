package com.tradingsimulator.tradingengine.botstrategies; import com.tradingsimulator.model.OrderSide; import java.math.BigDecimal; import java.util.List; /**
 * Chiến lược sinh tín hiệu giao dịch (mẫu Strategy – giao diện). Trả về {@link OrderSide} hoặc
 * {@code null} khi chưa đủ dữ liệu/không có tín hiệu.
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Strategy).</p>
 */
public interface IBotStrategy { OrderSide signal(List<BigDecimal> prices); default OrderSide signal(double rsi){return null;} }
