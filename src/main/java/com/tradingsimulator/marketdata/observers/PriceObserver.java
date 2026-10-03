package com.tradingsimulator.marketdata.observers; import com.tradingsimulator.model.Quote; import java.util.function.Consumer; /**
 * Người quan sát giá cụ thể, chuyển tiếp quote tới một {@link Consumer} (mẫu Observer).
 *
 * <p>Chủ sở hữu: Thành viên 3 (mẫu Observer).</p>
 */
public final class PriceObserver implements IPriceObserver { private final Consumer<Quote> consumer; public PriceObserver(Consumer<Quote> c){consumer=c;} public void onPrice(Quote q){consumer.accept(q);} }
