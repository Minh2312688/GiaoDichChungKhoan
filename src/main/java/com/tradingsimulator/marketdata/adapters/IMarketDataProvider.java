package com.tradingsimulator.marketdata.adapters; import com.tradingsimulator.model.Quote; /**
 * Cổng dữ liệu thị trường (mẫu Adapter – giao diện đích). Mọi bộ cung cấp chỉ cần trả về một
 * {@link Quote} cho mỗi symbol.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Adapter).</p>
 */
public interface IMarketDataProvider { Quote fetch(String symbol); }
