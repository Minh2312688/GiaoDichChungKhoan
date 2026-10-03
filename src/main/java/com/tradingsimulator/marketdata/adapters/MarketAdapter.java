package com.tradingsimulator.marketdata.adapters; import com.tradingsimulator.marketdata.observers.*; import com.tradingsimulator.model.*; import java.util.*; /**
 * Bộ chuyển đổi dữ liệu thị trường (mẫu Adapter): bọc một {@link IMarketDataProvider}, chuẩn hóa
 * quote và phát tới các {@code IPriceObserver} đã đăng ký.
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Adapter).</p>
 */
public final class MarketAdapter { private final IMarketDataProvider provider; private final List<IPriceObserver> observers=new ArrayList<>(); public MarketAdapter(IMarketDataProvider p){provider=p;} public Quote getQuote(String s){return provider.fetch(s);} public void addObserver(IPriceObserver o){observers.add(o);} public Quote refresh(String s){Quote q=getQuote(s);observers.forEach(o->o.onPrice(q));return q;} }
