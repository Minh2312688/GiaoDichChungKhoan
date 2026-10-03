package com.tradingsimulator.reporting;

import com.tradingsimulator.core.TradingFacade;
import com.tradingsimulator.model.Position;
import com.tradingsimulator.model.Quote;
import com.tradingsimulator.model.Trade;
import com.tradingsimulator.model.OrderSide;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Snapshot lãi/lỗ bất biến của danh mục hiện tại.
 *
 * @param cash        tiền mặt khả dụng
 * @param totalBuy    tổng giá trị các lệnh BUY đã khớp
 * @param totalSell   tổng giá trị các lệnh SELL đã khớp
 * @param netRealized dòng tiền từ giao dịch ({@code totalSell - totalBuy})
 * @param marketValue giá trị thị trường hiện tại của các vị thế đang mở
 * @param equity      {@code cash + marketValue}
 *
 * <p>Chủ sở hữu: Thành viên 2 (mẫu Memento).</p>
 */
public record PerformanceReport(
    BigDecimal cash,
    BigDecimal totalBuy,
    BigDecimal totalSell,
    BigDecimal netRealized,
    BigDecimal marketValue,
    BigDecimal equity) {

  public static PerformanceReport from(TradingFacade facade) {
    if (facade == null) {
      throw new IllegalArgumentException("Facade is required");
    }
    BigDecimal totalBuy = BigDecimal.ZERO;
    BigDecimal totalSell = BigDecimal.ZERO;
    for (Trade trade : facade.trades()) {
      if (trade.side() == OrderSide.BUY) {
        totalBuy = totalBuy.add(trade.total());
      } else {
        totalSell = totalSell.add(trade.total());
      }
    }
    Map<String, Quote> quotes = facade.quotes();
    BigDecimal marketValue = BigDecimal.ZERO;
    for (Position position : facade.positions().values()) {
      Quote quote = quotes.get(position.symbol());
      BigDecimal price = quote == null ? position.averagePrice() : quote.last();
      marketValue = marketValue.add(price.multiply(BigDecimal.valueOf(position.quantity())));
    }
    BigDecimal cash = facade.cash();
    return new PerformanceReport(
        cash.setScale(2, RoundingMode.HALF_UP),
        totalBuy.setScale(2, RoundingMode.HALF_UP),
        totalSell.setScale(2, RoundingMode.HALF_UP),
        totalSell.subtract(totalBuy).setScale(2, RoundingMode.HALF_UP),
        marketValue.setScale(2, RoundingMode.HALF_UP),
        cash.add(marketValue).setScale(2, RoundingMode.HALF_UP));
  }
}
