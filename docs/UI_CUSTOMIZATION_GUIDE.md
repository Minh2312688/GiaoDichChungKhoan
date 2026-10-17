# Tùy chỉnh giao diện

Chỉnh sửa `src/main/resources/theme_config.json` hoặc `config/theme_config.json` với các mã màu HEX:
- `background`: Màu nền chính của cửa sổ.
- `foreground`: Màu chữ chính.
- `accent`: Màu nhấn cho các nút bấm (button).
- `panel`: Màu nền cho các panel nội bộ.
- `positive`: Màu trạng thái tích cực (mua, tăng giá, lời).
- `negative`: Màu trạng thái tiêu cực (bán, giảm giá, lỗ).
- `warning`: Màu trạng thái cảnh báo hoặc lệnh đang chờ khớp.

`ThemeManager` sẽ dùng giá trị mặc định an toàn khi tệp thiếu hoặc sai định dạng.

Ví dụ cấu hình mẫu:

```json
{
  "background": "#2f343b",
  "foreground": "#ffffff",
  "accent": "#2d8cdc",
  "panel": "#383e47",
  "positive": "#28a745",
  "negative": "#dc3545",
  "warning": "#ffc107"
}
```
