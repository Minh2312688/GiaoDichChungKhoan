package com.tradingsimulator.marketdata.adapters; import com.tradingsimulator.model.*; import java.math.BigDecimal; import java.util.*; /**
 * Bộ cung cấp dữ liệu mô phỏng đơn giản: giá cố định, spread cố định 0.05.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Adapter).</p>
 */
public final class MockMarketDataProvider implements IMarketDataProvider { private final Map<String,BigDecimal> prices; public MockMarketDataProvider(Map<String,BigDecimal> p){prices=new HashMap<>(p);} public Quote fetch(String s){BigDecimal p=prices.get(s);if(p==null)throw new IllegalArgumentException("Mã không tồn tại: "+s);return new Quote(s,p.subtract(new BigDecimal("0.05")),p.add(new BigDecimal("0.05")),1000);} }
