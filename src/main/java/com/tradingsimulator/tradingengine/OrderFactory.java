package com.tradingsimulator.tradingengine; 
import com.tradingsimulator.model.*; 
import java.math.BigDecimal; 
/**
 * Nhà máy tạo lệnh (mẫu Factory Method).
 *
 * <p>Cung cấp ba phương thức tạo lệnh rõ ràng: {@link #market}, {@link #limit} và
 * {@link #stopLoss}, giúp tách việc khởi tạo lệnh khỏi nơi sử dụng.</p>
 *
 * <p>Chủ sở hữu: Thành viên 1 (mẫu Factory Method).</p>
 */
public final class OrderFactory { 
    public Order market(String s,OrderSide side,int q){
        return new Order(s,side,OrderType.MARKET,q,null);} 
    public Order limit(String s,OrderSide side,int q,BigDecimal p){
        return new Order(s,side,OrderType.LIMIT,q,p);} public Order stopLoss(String s,OrderSide side,int q,BigDecimal p){return new Order(s,side,OrderType.STOP_LOSS,q,p);} }
