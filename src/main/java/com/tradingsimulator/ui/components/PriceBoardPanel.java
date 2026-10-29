package com.tradingsimulator.ui.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Bảng giá chứng khoán điện tử chuyên nghiệp theo chuẩn SSI iBoard.
 * Hiển thị đầy đủ các cột: Mã CK, Trần, Sàn, TC, Bên mua, Khớp lệnh, Bên bán, Tổng KL, Cao, Thấp.
 * Sử dụng đúng quy chuẩn màu sắc thị trường chứng khoán Việt Nam:
 * - Trần: Tím (#bf5af2)
 * - Sàn: Xanh lơ (#00f0ff)
 * - Tham chiếu: Vàng (#ffd600)
 * - Tăng: Xanh lá (#00e676)
 * - Giảm: Đỏ (#ff3b30)
 */
public final class PriceBoardPanel extends JPanel {
    private static final Color BG_DARK = new Color(0x11, 0x13, 0x17);
    private static final Color HEADER_BG = new Color(0x1a, 0x1e, 0x24);
    private static final Color STRIPE_BG = new Color(0x16, 0x19, 0x1f);
    private static final Color BORDER_COLOR = new Color(0x23, 0x27, 0x30);

    private static final Color PURPLE_CEIL = new Color(0xbf, 0x5a, 0xf2);
    private static final Color CYAN_FLOOR = new Color(0x00, 0xf0, 0xff);
    private static final Color YELLOW_REF = new Color(0xff, 0xd6, 0x00);
    private static final Color GREEN_UP = new Color(0x00, 0xe6, 0x76);
    private static final Color RED_DOWN = new Color(0xff, 0x3b, 0x30);
    private static final Color WHITE_VOL = new Color(0xea, 0xec, 0xef);
    private static final Color TEXT_LIGHT = new Color(0xea, 0xec, 0xef);

    // Thứ tự cột trong Model (4 cột đầu giữ nguyên tương thích TestUi và TestScreens)
    private static final String[] COLUMN_NAMES = {
        "Mã", "Giá mua", "Giá bán", "Thay đổi",
        "Trần", "Sàn", "TC", "Khớp lệnh", "KL Khớp", "Tổng KL", "Cao", "Thấp"
    };

    private final DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final Map<String, QuoteValues> previousQuotes = new HashMap<>();
    private final Map<String, Color> flashingCells = new HashMap<>();
    private final Map<String, javax.swing.Timer> flashTimers = new HashMap<>();
    private final TableRowSorter<DefaultTableModel> rowSorter;

    private final JTable table = new JTable(model) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            int modelRow = convertRowIndexToModel(row);
            int modelCol = convertColumnIndexToModel(column);

            if (!isCellSelected(row, column)) {
                c.setBackground(row % 2 == 0 ? BG_DARK : STRIPE_BG);

                String symbol = model.getValueAt(modelRow, 0).toString();
                Color flash = flashingCells.get(cellKey(symbol, modelCol));
                if (flash != null) {
                    c.setBackground(blend(c.getBackground(), flash));
                }

                // Tô màu theo quy chuẩn bảng giá chứng khoán
                c.setForeground(getCellTextColor(modelRow, modelCol));
            }
            return c;
        }
    };

    private Consumer<String> symbolDoubleClickListener;

    public PriceBoardPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(BG_DARK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(0x2a, 0x32, 0x40));
        table.setSelectionForeground(Color.WHITE);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 32));
        header.setBackground(HEADER_BG);
        header.setForeground(new Color(0x84, 0x8e, 0x9c));
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Căn lề số cho bảng giá
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        // Căn giữa cho Mã
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        scrollPane.getViewport().setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // Xử lý click/double-click
        table.setToolTipText("Nhấn đúp chuột vào mã chứng khoán để mở hộp thoại Mua/Bán");
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    int row = table.rowAtPoint(event.getPoint());
                    if (row >= 0 && symbolDoubleClickListener != null) {
                        int modelRow = table.convertRowIndexToModel(row);
                        symbolDoubleClickListener.accept(model.getValueAt(modelRow, 0).toString());
                    }
                }
            }
        });
    }

    public void seedDefaultStocks() {
        if (model.getRowCount() == 0) {
            initDefaultStockRows();
        }
    }

    private void initDefaultStockRows() {
        // Danh sách các mã cổ phiếu phổ biến thị trường (Ảnh 2 - SSI iBoard)
        Object[][] sampleData = {
            {"ACB", "21.10", "21.15", "-1.17%", "22.80", "19.90", "21.35", "21.10", "4,000", "2,424,800", "21.30", "21.05"},
            {"BID", "34.00", "34.10", "+1.34%", "36.00", "31.30", "33.65", "34.10", "100", "4,961,900", "34.80", "33.90"},
            {"BSR", "27.45", "27.50", "-4.35%", "30.75", "26.75", "28.75", "27.50", "300", "9,836,400", "28.85", "27.20"},
            {"CTG", "30.70", "30.75", "0.00%", "32.80", "28.60", "30.70", "30.70", "1,100", "5,485,000", "31.10", "30.60"},
            {"FPT", "52.50", "52.60", "-2.78%", "57.70", "50.30", "54.00", "52.50", "100", "7,932,800", "54.80", "52.50"},
            {"GAS", "74.10", "74.20", "-4.63%", "83.10", "72.30", "77.70", "74.10", "1,500", "1,245,300", "78.00", "73.80"},
            {"GVR", "32.10", "32.15", "+2.06%", "33.70", "29.30", "31.50", "32.15", "700", "3,076,400", "32.90", "32.00"},
            {"HDB", "22.45", "22.50", "-1.53%", "24.40", "21.30", "22.85", "22.50", "600", "5,705,000", "22.85", "22.40"},
            {"HPG", "21.20", "21.25", "+2.41%", "22.20", "19.30", "20.75", "21.25", "200", "26,338,500", "21.60", "20.75"},
            {"MBB", "24.15", "24.20", "+0.83%", "25.60", "22.30", "24.00", "24.20", "8,200", "12,450,100", "24.50", "23.95"},
            {"MWG", "65.30", "65.40", "+1.55%", "68.80", "60.00", "64.40", "65.40", "2,300", "6,120,400", "66.00", "64.20"},
            {"SSI", "31.80", "31.90", "+2.24%", "33.35", "29.05", "31.20", "31.90", "15,400", "18,920,300", "32.20", "31.10"},
            {"TCB", "23.40", "23.45", "-0.64%", "25.20", "22.00", "23.60", "23.45", "4,500", "8,400,200", "23.80", "23.30"},
            {"TPB", "17.10", "17.15", "+1.18%", "18.25", "15.95", "16.95", "17.15", "6,100", "7,120,400", "17.30", "16.90"},
            {"VCB", "88.20", "88.30", "+0.91%", "94.30", "82.10", "87.40", "88.30", "1,800", "1,980,500", "89.00", "87.50"},
            {"VHM", "42.10", "42.20", "+1.69%", "45.00", "39.20", "41.40", "42.20", "8,900", "14,350,200", "42.80", "41.30"},
            {"VIB", "18.60", "18.65", "-0.80%", "19.90", "17.30", "18.75", "18.65", "3,400", "5,210,000", "18.85", "18.55"},
            {"VIC", "41.50", "41.60", "+0.24%", "44.40", "38.60", "41.50", "41.60", "3,100", "4,210,000", "42.00", "41.20"},
            {"VJC", "102.50", "102.60", "-0.48%", "109.60", "95.40", "103.00", "102.60", "800", "950,400", "103.50", "102.00"},
            {"VNM", "66.80", "66.90", "-0.30%", "71.60", "62.40", "67.10", "66.90", "1,900", "3,500,400", "67.50", "66.50"},
            {"VPB", "19.85", "19.90", "+1.02%", "21.05", "18.35", "19.70", "19.90", "11,200", "15,800,900", "20.10", "19.65"},
            {"VRE", "18.20", "18.25", "+2.25%", "19.45", "16.95", "17.80", "18.25", "4,800", "8,940,000", "18.50", "17.80"},
            {"POW", "11.75", "11.80", "0.00%", "12.55", "10.95", "11.75", "11.75", "9,500", "11,200,500", "11.90", "11.70"},
            {"PLX", "38.10", "38.20", "-1.29%", "40.75", "35.45", "38.60", "38.20", "1,200", "2,130,000", "38.80", "38.00"},
            {"MSN", "72.40", "72.50", "+1.97%", "77.40", "67.40", "71.00", "72.50", "2,600", "4,510,800", "73.20", "71.10"},
            {"SAB", "56.00", "56.10", "-0.71%", "59.90", "52.10", "56.40", "56.10", "900", "1,450,200", "56.80", "55.80"},
            {"STB", "29.85", "29.90", "+1.70%", "31.90", "27.80", "29.35", "29.90", "12,800", "16,420,000", "30.15", "29.30"},
            {"SHB", "10.65", "10.70", "+0.94%", "11.45", "9.95", "10.60", "10.70", "18,400", "24,800,000", "10.80", "10.55"},
            {"SSB", "16.20", "16.25", "0.00%", "17.30", "15.10", "16.20", "16.20", "2,100", "3,100,500", "16.35", "16.15"},
            {"BVH", "41.80", "41.90", "-0.48%", "44.70", "38.90", "42.00", "41.90", "700", "1,120,400", "42.30", "41.70"},
            {"BCM", "66.50", "66.60", "+0.45%", "71.10", "61.90", "66.20", "66.60", "500", "154,200", "66.90", "65.80"},
            {"SHS", "14.20", "14.25", "+2.16%", "15.20", "13.20", "13.90", "14.25", "7,800", "9,850,000", "14.40", "13.80"},
            {"PVS", "37.50", "37.60", "+1.62%", "40.10", "34.90", "36.90", "37.60", "3,900", "6,150,000", "37.90", "36.80"},
            {"IDC", "53.20", "53.30", "-1.11%", "56.90", "49.50", "53.80", "53.30", "1,500", "2,840,000", "54.10", "53.00"},
            {"CEO", "15.60", "15.65", "+3.31%", "16.70", "14.50", "15.10", "15.65", "6,400", "10,240,000", "15.80", "15.00"},
            {"DGC", "112.50", "112.60", "+2.74%", "120.30", "104.70", "109.50", "112.60", "1,700", "3,890,000", "113.80", "109.00"},
            {"MWG", "65.30", "65.40", "+1.55%", "68.80", "60.00", "64.40", "65.40", "2,300", "6,120,400", "66.00", "64.20"},
            // Dữ liệu mô phỏng trong bài
            {"AAPL", "154.90", "155.10", "+3.33%", "160.50", "139.50", "150.00", "155.00", "5,200", "1,420,800", "156.20", "149.80"},
            {"BTC", "44990.00", "45010.00", "0.00%", "48150.00", "41850.00", "45000.00", "45000.00", "120", "45,200", "46200.00", "44100.00"},
            {"ETH", "2995.00", "3005.00", "0.00%", "3210.00", "2790.00", "3000.00", "3000.00", "850", "312,000", "3080.00", "2950.00"}
        };

        for (Object[] row : sampleData) {
            model.addRow(row);
        }
    }

    private Color getCellTextColor(int row, int col) {
        String symbol = model.getValueAt(row, 0).toString();
        String changeStr = model.getValueAt(row, 3).toString();

        // Cột Mã: tô theo biến động giá
        if (col == 0) {
            if (changeStr.startsWith("+")) return GREEN_UP;
            if (changeStr.startsWith("-")) return RED_DOWN;
            return YELLOW_REF;
        }

        // Cột Trần, Sàn, TC
        if (col == 4) return PURPLE_CEIL;
        if (col == 5) return CYAN_FLOOR;
        if (col == 6) return YELLOW_REF;

        // Cột Thay đổi (%)
        if (col == 3) {
            if (changeStr.startsWith("+")) return GREEN_UP;
            if (changeStr.startsWith("-")) return RED_DOWN;
            return YELLOW_REF;
        }

        // Cột Giá mua, Giá bán, Khớp lệnh
        if (col == 1 || col == 2 || col == 7 || col == 10 || col == 11) {
            try {
                BigDecimal val = new BigDecimal(model.getValueAt(row, col).toString().replace(",", ""));
                BigDecimal ref = new BigDecimal(model.getValueAt(row, 6).toString().replace(",", ""));
                int cmp = val.compareTo(ref);
                if (cmp > 0) return GREEN_UP;
                if (cmp < 0) return RED_DOWN;
                return YELLOW_REF;
            } catch (Exception e) {
                return TEXT_LIGHT;
            }
        }

        return WHITE_VOL;
    }

    public void setQuote(String symbol, String bid, String ask) {
        BigDecimal bidValue = new BigDecimal(bid);
        BigDecimal askValue = new BigDecimal(ask);
        BigDecimal midpoint = bidValue.add(askValue).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP);
        QuoteValues previous = previousQuotes.put(symbol, new QuoteValues(bidValue, askValue, midpoint));

        String change = "0.00%";
        if (previous != null && previous.midpoint().signum() != 0) {
            BigDecimal percent = midpoint.subtract(previous.midpoint())
                .multiply(BigDecimal.valueOf(100))
                .divide(previous.midpoint(), 2, RoundingMode.HALF_UP);
            change = (percent.signum() > 0 ? "+" : "") + percent.toPlainString() + "%";
        }

        BigDecimal tc = midpoint.setScale(2, RoundingMode.HALF_UP);
        BigDecimal ceil = tc.multiply(new BigDecimal("1.07")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal floor = tc.multiply(new BigDecimal("0.93")).setScale(2, RoundingMode.HALF_UP);
        String matchPrice = midpoint.setScale(2, RoundingMode.HALF_UP).toPlainString();

        for (int row = 0; row < model.getRowCount(); row++) {
            if (symbol.equals(model.getValueAt(row, 0))) {
                if (previous != null) {
                    if (bidValue.compareTo(previous.bid()) != 0) {
                        flash(symbol, 1, bidValue.compareTo(previous.bid()) > 0 ? GREEN_UP : RED_DOWN);
                    }
                    if (askValue.compareTo(previous.ask()) != 0) {
                        flash(symbol, 2, askValue.compareTo(previous.ask()) > 0 ? GREEN_UP : RED_DOWN);
                    }
                    flash(symbol, 3, midpoint.compareTo(previous.midpoint()) >= 0 ? GREEN_UP : RED_DOWN);
                    flash(symbol, 7, midpoint.compareTo(previous.midpoint()) >= 0 ? GREEN_UP : RED_DOWN);
                }
                model.setValueAt(bid, row, 1);
                model.setValueAt(ask, row, 2);
                model.setValueAt(change, row, 3);
                model.setValueAt(ceil.toPlainString(), row, 4);
                model.setValueAt(floor.toPlainString(), row, 5);
                model.setValueAt(tc.toPlainString(), row, 6);
                model.setValueAt(matchPrice, row, 7);
                return;
            }
        }

        model.addRow(new Object[]{
            symbol, bid, ask, change,
            ceil.toPlainString(), floor.toPlainString(), tc.toPlainString(),
            matchPrice, "1,000", "50,000", ceil.toPlainString(), floor.toPlainString()
        });
    }

    public JTable getTable() {
        return table;
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty() || query.equalsIgnoreCase("Tất cả")) {
            rowSorter.setRowFilter(null);
            return;
        }
        String q = query.trim().toUpperCase();
        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + q, 0));
    }

    public void filterCategory(String category) {
        if (category == null || category.equalsIgnoreCase("Tất cả") || category.equalsIgnoreCase("Danh mục của tôi")) {
            rowSorter.setRowFilter(null);
            return;
        }
        if (category.equalsIgnoreCase("VN30")) {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                private final java.util.Set<String> vn30 = java.util.Set.of(
                    "ACB", "BCM", "BID", "BVH", "CTG", "FPT", "GAS", "GVR", "HDB", "HPG",
                    "MBB", "MSN", "MWG", "PLX", "POW", "SAB", "SHB", "SSB", "SSI", "STB",
                    "TCB", "TPB", "VCB", "VHM", "VIB", "VIC", "VJC", "VNM", "VPB", "VRE"
                );
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String symbol = entry.getStringValue(0).toUpperCase();
                    return vn30.contains(symbol);
                }
            });
            return;
        }
        if (category.equalsIgnoreCase("HNX30") || category.equalsIgnoreCase("HNX")) {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                private final java.util.Set<String> hnx = java.util.Set.of("BSR", "SHS", "PVS", "IDC", "CEO");
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String symbol = entry.getStringValue(0).toUpperCase();
                    return hnx.contains(symbol);
                }
            });
            return;
        }
        if (category.equalsIgnoreCase("HOSE")) {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String symbol = entry.getStringValue(0).toUpperCase();
                    return !symbol.equals("BTC") && !symbol.equals("ETH");
                }
            });
            return;
        }
        if (category.equalsIgnoreCase("Phái sinh") || category.equalsIgnoreCase("Crypto")) {
            rowSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    String symbol = entry.getStringValue(0).toUpperCase();
                    return symbol.equals("BTC") || symbol.equals("ETH");
                }
            });
            return;
        }
        rowSorter.setRowFilter(null);
    }

    private void flash(String symbol, int column, Color color) {
        String key = cellKey(symbol, column);
        javax.swing.Timer prev = flashTimers.remove(key);
        if (prev != null) {
            prev.stop();
        }
        flashingCells.put(key, color);
        javax.swing.Timer timer = new javax.swing.Timer(400, event -> {
            flashingCells.remove(key);
            flashTimers.remove(key);
            table.repaint();
        });
        timer.setRepeats(false);
        flashTimers.put(key, timer);
        timer.start();
        table.repaint();
    }

    private static String cellKey(String symbol, int column) {
        return symbol + ":" + column;
    }

    private static Color blend(Color base, Color flash) {
        return new Color(
            (base.getRed() * 2 + flash.getRed()) / 3,
            (base.getGreen() * 2 + flash.getGreen()) / 3,
            (base.getBlue() * 2 + flash.getBlue()) / 3
        );
    }

    private record QuoteValues(BigDecimal bid, BigDecimal ask, BigDecimal midpoint) { }

    public void onSymbolDoubleClick(Consumer<String> listener) {
        symbolDoubleClickListener = Objects.requireNonNull(listener, "listener");
    }
}
