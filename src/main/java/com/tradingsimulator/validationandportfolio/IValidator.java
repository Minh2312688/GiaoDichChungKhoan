package com.tradingsimulator.validationandportfolio;

import com.tradingsimulator.model.Order;
import java.math.BigDecimal;

/**
 * Một quy tắc kiểm tra đơn lẻ trong chuỗi kiểm tra hợp lệ của lệnh.
 *
 * <p>Lớp cài đặt phải ném {@link IllegalArgumentException} với thông báo rõ ràng khi lệnh vi
 * phạm quy tắc. Thêm quy tắc mới không cần sửa {@link ValidationChain}; chỉ cần đăng ký
 * validator qua {@link ValidationChain#add(IValidator)}.</p>
 *
 * <p>Chủ sở hữu: Thành viên 4 (mẫu Chain of Responsibility).</p>
 */
public interface IValidator {
  void validate(Order order, BigDecimal cash, int position, BigDecimal marketPrice);
}
