package com.tradingsimulator.marketdata.observers; import com.tradingsimulator.model.Quote; /**
 * Người quan sát nhận thông báo giá (mẫu Observer – giao diện).
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public interface IPriceObserver { void onPrice(Quote quote); }
