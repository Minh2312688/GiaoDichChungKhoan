package com.tradingsimulator.core;
import com.tradingsimulator.model.*; import java.math.*; import java.util.*;
/**
 * Engine giao dịch duy nhất trong tiến trình (mẫu Singleton).
 *
 * <p>Giữ tiền mặt, vị thế, lịch sử khớp lệnh và bảng giá; chỉ truy cập qua {@link #getInstance()}.</p>
 *
 * <p>Chủ sở hữu: Thành viên 1 (mẫu Singleton).</p>
 */
public final class ExchangeEngine {
  private static final ExchangeEngine INSTANCE = new ExchangeEngine(); private BigDecimal cash = BigDecimal.ZERO; private final Map<String,Position> positions = new HashMap<>(); private final List<Trade> trades = new ArrayList<>(); private final Map<String,Quote> quotes = new HashMap<>();
  private ExchangeEngine() {}
  public static ExchangeEngine getInstance() { return INSTANCE; }
  public synchronized void reset(BigDecimal initialCash) { if (initialCash == null || initialCash.signum() < 0) throw new IllegalArgumentException("Tiền ban đầu không được âm"); cash=initialCash; positions.clear(); trades.clear(); quotes.clear(); }
  public synchronized void updateQuote(Quote q) { quotes.put(q.symbol(), q); }
  public synchronized Trade execute(Order o) { Quote q=quotes.get(o.symbol()); if(q==null) throw new IllegalArgumentException("Không có báo giá cho mã: "+o.symbol()); BigDecimal price=o.side()==OrderSide.BUY?q.ask():q.bid(); if(o.type()==OrderType.LIMIT && ((o.side()==OrderSide.BUY && price.compareTo(o.price())>0)||(o.side()==OrderSide.SELL && price.compareTo(o.price())<0))) throw new IllegalArgumentException("Chưa đạt giá giới hạn"); if(o.type()==OrderType.STOP_LOSS && o.side()==OrderSide.SELL && price.compareTo(o.price())>0) throw new IllegalArgumentException("Chưa chạm ngưỡng cắt lỗ"); BigDecimal total=price.multiply(BigDecimal.valueOf(o.quantity())).setScale(2,RoundingMode.HALF_UP); Position old=positions.getOrDefault(o.symbol(),new Position(o.symbol(),0,BigDecimal.ZERO)); int qty=old.quantity()+(o.side()==OrderSide.BUY?o.quantity():-o.quantity()); if(qty<0) throw new IllegalArgumentException("Không đủ vị thế"); if(o.side()==OrderSide.BUY) { cash=cash.subtract(total); BigDecimal cost=old.averagePrice().multiply(BigDecimal.valueOf(old.quantity())).add(total); positions.put(o.symbol(),new Position(o.symbol(),qty,cost.divide(BigDecimal.valueOf(qty),2,RoundingMode.HALF_UP))); } else { cash=cash.add(total); positions.put(o.symbol(),new Position(o.symbol(),qty,qty==0?BigDecimal.ZERO:old.averagePrice())); } Trade t=new Trade(o.symbol(),o.side(),o.quantity(),price,total,null); trades.add(t); return t; }
  public synchronized BigDecimal cash(){return cash;} public synchronized Position position(String s){return positions.getOrDefault(s,new Position(s,0,BigDecimal.ZERO));} public synchronized Map<String,Position> positions(){return Map.copyOf(positions);} public synchronized List<Trade> trades(){return List.copyOf(trades);} public synchronized Map<String,Quote> quotes(){return Map.copyOf(quotes);}
  public synchronized void restore(BigDecimal c, Map<String,Position> p, List<Trade> ts, Map<String,Quote> qs){cash=c;positions.clear();positions.putAll(p);trades.clear();trades.addAll(ts);quotes.clear();quotes.putAll(qs);}
}
