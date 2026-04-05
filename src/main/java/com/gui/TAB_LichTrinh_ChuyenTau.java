package com.gui;

import com.dao.DAO_ChuyenTau;
import com.dao.DAO_ChuyenTau.ChuyenTauRow;
import com.dao.DAO_LichTrinh;
import com.dao.DAO_LichTrinh.LichTrinhRow;
import com.dao.DAO_Tau;
import com.entities.Tau;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import javax.swing.table.TableRowSorter;

/**
 * Tab Quản lý Lịch trình & Chuyến tàu
 *
 * Quy tắc mã:
 *  - maChuyen : CHUYEN + XXXX  (10 ký tự, tự động sinh, chỉ tăng khi xác nhận)
 *  - maTuyen  : T + XXXX        ( 5 ký tự, tự động sinh, chỉ tăng khi xác nhận)
 *  - maLT     : LT + XXXX       ( 6 ký tự, tự động sinh, chỉ tăng khi xác nhận)
 *
 * Nghiệp vụ:
 *  - 1 maChuyen ↔ 1 maLT
 *  - 2 maChuyen khác nhau có thể dùng cùng maTau
 *  - Giờ đến = giờ đi + 1 hoặc 2 ngày (tuỳ tuyến)
 *  - Trạng thái tự động theo thời gian thực
 *  - Double-click dòng → mở dialog cập nhật
 *  - Mã chỉ tăng khi nhấn xác nhận lưu
 *  - Không có dữ liệu demo sẵn
 */
public class TAB_LichTrinh_ChuyenTau extends JPanel {

    // =========================================================================
    // MÀU SẮC
    // =========================================================================
    private static final Color BG_PAGE      = new Color(0xF4F7FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR   = new Color(0x2270CC);
    private static final Color ACCENT_FOC   = new Color(0x4D9DE0);
    private static final Color TEXT_DARK    = new Color(0x1E2B3C);
    private static final Color TEXT_MID     = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT   = new Color(0xA0AEC0);
    private static final Color BORDER       = new Color(0xE2EAF4);
    private static final Color ROW_ALT      = new Color(0xF7FAFF);
    private static final Color BTN2_BG      = new Color(0xF0F4FA);
    private static final Color BTN2_FG      = new Color(0x3A5A8C);
    private static final Color BTN_RED      = new Color(0xC0392B);
    private static final Color BTN_RED_HVR  = new Color(0xE74C3C);
    private static final Color TH_BG        = new Color(0xE8F0FB);
    private static final Color CLR_CHUA     = new Color(0xE67E22);
    private static final Color CLR_DANG     = new Color(0x27AE60);
    private static final Color CLR_HOAN     = new Color(0x2980B9);
    private static final Color CLR_HUY      = new Color(0xC0392B);

    // =========================================================================
    // FONT
    // =========================================================================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // =========================================================================
    // CỘT BẢNG HIỂN THỊ (6 cột)
    // Bảng model thực sự có 10 cột: 6 hiển thị + 4 ẩn (maLT, ngayKH, gioDi, gioDen)
    // =========================================================================
    private static final String[] COLS_CT = {
            "Mã Chuyến", "Tên Chuyến", "Mã Tàu", "Mã Tuyến", "Tuyến", "Trạng Thái",
            "_maLT", "_ngayKH", "_gioDi", "_gioDen"   // cột ẩn index 6..9
    };
    private static final String[] COLS_LT = {
            "Mã LT", "Ngày Khởi Hành", "Giờ Đi", "Giờ Đến"
    };

    // =========================================================================
    // TRẠNG THÁI
    // =========================================================================
    private static final String TT_CHUA = "Chưa Khởi Hành";
    private static final String TT_DANG = "Đang Khởi Hành";
    private static final String TT_HOAN = "Đã Hoàn Thành";
    private static final String TT_HUY  = "Đã Hủy";
    private static final String[] DS_TRANG_THAI = {TT_CHUA, TT_DANG, TT_HOAN, TT_HUY};

    // =========================================================================
    // GIỜ
    // =========================================================================
    private static final String[] GIO_24H = buildGio24H();
    private static String[] buildGio24H() {
        String[] a = new String[48];
        for (int h = 0; h < 24; h++) { a[h*2]=String.format("%02d:00",h); a[h*2+1]=String.format("%02d:30",h); }
        return a;
    }
    private static final String DATE_FMT = "dd/MM/yyyy";

    // =========================================================================
    // BỘ ĐẾM – chỉ tăng khi xác nhận lưu
    // Mã chuyến: CT + số 2 chữ số (CT01 → CT9999)
    // =========================================================================
    private int cntChuyen = 1;
    private int cntLT     = 1;

    private String peekMaChuyen() { return String.format("CT%02d", cntChuyen); }
    private String peekMaLT()     { return String.format("LT%04d", cntLT);     }

    private String nextMaChuyen() { return String.format("CT%02d", cntChuyen++); }
    private String nextMaLT()     { return String.format("LT%04d", cntLT++);     }

    // =========================================================================
    // THÀNH PHẦN CHÍNH
    // =========================================================================
    private final DefaultTableModel modelCT;
    private final DefaultTableModel modelLT;
    private final JTable            tableCT;
    private final JTable            tableLT;

    private final JLabel lblTauMa      = infoLbl("-");
    private final JLabel lblTauTen     = infoLbl("-");
    private final JLabel lblTauSoToa   = infoLbl("-");
    private final JLabel lblTuyenMa    = infoLbl("-");
    private final JLabel lblTuyenGaDi  = infoLbl("-");
    private final JLabel lblTuyenGaDen = infoLbl("-");

    // =========================================================================
    // STAT BAR – labels thống kê
    // =========================================================================
    private final JLabel lblStatTotal = new JLabel("0");
    private final JLabel lblStatChua  = new JLabel("0");
    private final JLabel lblStatDang  = new JLabel("0");
    private final JLabel lblStatHoan  = new JLabel("0");

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // BỘ LỌC – fields
    // =========================================================================
    private JComboBox<String> cbFilterTuyen;
    private DatePickerField    dcFilterNgay;
    private JComboBox<String> cbFilterTT;

    // =========================================================================
    // DAO – KẾT NỐI DATABASE
    // =========================================================================
    private final DAO_ChuyenTau daoChuyenTau = new DAO_ChuyenTau();
    private final DAO_LichTrinh daoLichTrinh  = new DAO_LichTrinh();
    private final DAO_Tau       daoTau        = new DAO_Tau();

    // =========================================================================
    // KHỞI TẠO
    // =========================================================================
    public TAB_LichTrinh_ChuyenTau() {
        // Fix lỗi Swing: ArrayIndexOutOfBoundsException khi JComboBox popup bị đóng đột ngột
        UIManager.put("PopupMenu.consumeEventOnClose", Boolean.TRUE);

        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Model 10 cột (4 cột ẩn)
        modelCT = new DefaultTableModel(COLS_CT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCT = buildTableCT();

        modelLT = new DefaultTableModel(COLS_LT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLT = buildTableLT();

        // Chọn dòng → cập nhật chi tiết
        tableCT.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshDetail();
        });

        // Double-click → cập nhật
        tableCT.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openUpdateDialog();
            }
        });

        // Panel cố định 60/40 — dùng GridBagLayout, KHÔNG JSplitPane để tránh kéo thả
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        JPanel leftPanel  = buildLeftPanel();
        JPanel rightPanel = buildRightPanel();

        // Cố định kích thước tối thiểu để không bị squeeze
        // Panel trái cố định 780px, panel phải fill phần còn lại
        leftPanel.setPreferredSize(new Dimension(780, 0));
        leftPanel.setMinimumSize(new Dimension(780, 0));
        leftPanel.setMaximumSize(new Dimension(780, Integer.MAX_VALUE));

        GridBagConstraints gcLeft = new GridBagConstraints();
        gcLeft.gridx=0; gcLeft.gridy=0; gcLeft.weightx=0.0; gcLeft.weighty=1.0;
        gcLeft.fill=GridBagConstraints.BOTH; gcLeft.insets=new Insets(0,0,0,6);
        body.add(leftPanel, gcLeft);

        GridBagConstraints gcRight = new GridBagConstraints();
        gcRight.gridx=1; gcRight.gridy=0; gcRight.weightx=1.0; gcRight.weighty=1.0;
        gcRight.fill=GridBagConstraints.BOTH; gcRight.insets=new Insets(0,6,0,0);
        body.add(rightPanel, gcRight);

        add(buildStatBar(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        // Load dữ liệu từ DB khi khởi động
        loadFromDB();
    }

    // =========================================================================
    // PANEL TRÁI
    // =========================================================================
    // =========================================================================
    // STAT BAR – 4 thẻ thống kê (giống TAB_Tau)
    // =========================================================================
    private JPanel buildStatBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 16, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        bar.add(buildStatCard("TỔNG SỐ CHUYẾN",    lblStatTotal, new Color(37,  99, 235)));
        bar.add(buildStatCard("CHƯA KHỞI HÀNH",    lblStatChua,  new Color(245, 158, 11)));
        bar.add(buildStatCard("ĐANG KHỞI HÀNH",    lblStatDang,  new Color(34,  197, 94)));
        bar.add(buildStatCard("ĐÃ HOÀN THÀNH",     lblStatHoan,  new Color(100, 116, 139)));
        return bar;
    }

    private JPanel buildStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(TEXT_MID);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(accent);

        // Icon vẽ tay theo loại thẻ
        JLabel ico = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth()/2, cy = getHeight()/2;
                String t = title;
                if (t.contains("TỔNG")) {
                    // Icon: tàu đơn giản
                    g2.drawRoundRect(cx-10, cy-4, 20, 10, 4, 4);
                    g2.drawLine(cx-6, cy-4, cx-6, cy-8);
                    g2.drawLine(cx+6, cy-4, cx+6, cy-8);
                    g2.drawLine(cx-6, cy-8, cx+6, cy-8);
                    g2.fillOval(cx-8, cy+4, 4, 4);
                    g2.fillOval(cx+4, cy+4, 4, 4);
                } else if (t.contains("CHƯA")) {
                    // Icon: đồng hồ
                    g2.drawOval(cx-9, cy-9, 18, 18);
                    g2.drawLine(cx, cy-6, cx, cy);
                    g2.drawLine(cx, cy, cx+5, cy+3);
                    g2.fillOval(cx-2, cy-2, 4, 4);
                } else if (t.contains("ĐANG")) {
                    // Icon: mũi tên chạy
                    g2.drawRoundRect(cx-10, cy-5, 16, 10, 4, 4);
                    int[] ax = {cx+6, cx+11, cx+6};
                    int[] ay = {cy-4, cy, cy+4};
                    g2.fillPolygon(ax, ay, 3);
                    g2.drawLine(cx-7, cy, cx+2, cy);
                } else {
                    // Icon: dấu tích hoàn thành
                    g2.drawOval(cx-9, cy-9, 18, 18);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-5, cy, cx-1, cy+4);
                    g2.drawLine(cx-1, cy+4, cx+5, cy-4);
                }
                g2.dispose();
            }
        };
        ico.setPreferredSize(new Dimension(40, 40));
        ico.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.setOpaque(false);
        left.add(lblT,     BorderLayout.NORTH);
        left.add(lblValue, BorderLayout.CENTER);

        p.add(left, BorderLayout.CENTER);
        p.add(ico,  BorderLayout.EAST);
        return p;
    }

    private void updateStats() {
        int total = modelCT.getRowCount(), chua = 0, dang = 0, hoan = 0;
        for (int i = 0; i < total; i++) {
            Object tt = modelCT.getValueAt(i, 5);
            if (tt == null) continue;
            switch (tt.toString()) {
                case "Chưa Khởi Hành" -> chua++;
                case "Đang Khởi Hành" -> dang++;
                case "Đã Hoàn Thành"  -> hoan++;
            }
        }
        lblStatTotal.setText(String.valueOf(total));
        lblStatChua.setText(String.valueOf(chua));
        lblStatDang.setText(String.valueOf(dang));
        lblStatHoan.setText(String.valueOf(hoan));
    }

    private JPanel buildLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        // Tiêu đề
        JLabel title = new JLabel("QUẢN LÝ CHUYẾN TÀU");
        title.setFont(F_TITLE); title.setForeground(TEXT_DARK);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        top.add(title, BorderLayout.NORTH);
        top.add(buildFilterBar(), BorderLayout.CENTER);

        JPanel card = makeCard(new BorderLayout());
        JScrollPane sc = new JScrollPane(tableCT);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        styleScrollBar(sc.getVerticalScrollBar());
        styleScrollBar(sc.getHorizontalScrollBar());
        card.add(sc, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnBar.setOpaque(false);
        JButton btnThem = new JButton("  Thêm chuyến") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Vẽ icon + trái
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = 14, cy = getHeight() / 2;
                g2.drawLine(x - 5, cy, x + 5, cy);
                g2.drawLine(x, cy - 5, x, cy + 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnThem.setFont(F_LABEL);
        btnThem.setForeground(Color.WHITE);
        btnThem.setPreferredSize(new Dimension(155, 36));
        btnThem.setContentAreaFilled(false);
        btnThem.setBorderPainted(false);
        btnThem.setFocusPainted(false);
        btnThem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> openAddDialog());
        btnBar.add(btnThem);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(card,   BorderLayout.CENTER);
        pnl.add(btnBar, BorderLayout.SOUTH);
        return pnl;
    }

    // =========================================================================
    // BỘ LỌC
    // =========================================================================
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(BG_CARD);
        bar.setBorder(new ShadowBorder());

        // --- Tuyến ---
        bar.add(filterLbl("Tuyến:"));
        cbFilterTuyen = new JComboBox<>(new String[]{"Tất cả"});
        cbFilterTuyen.setFont(F_SMALL);
        cbFilterTuyen.setPreferredSize(new Dimension(150, 30));
        bar.add(cbFilterTuyen);

        // Sep
        bar.add(filterSep());

        // --- Ngày KH — DatePickerField tự vẽ ---
        bar.add(filterLbl("Ngày KH:"));
        dcFilterNgay = new DatePickerField("");
        dcFilterNgay.setPreferredSize(new Dimension(148, 32));
        dcFilterNgay.setToolTipText("Chọn ngày khởi hành để lọc");
        bar.add(dcFilterNgay);

        // Sep
        bar.add(filterSep());

        // --- Trạng thái ---
        bar.add(filterLbl("Trạng thái:"));
        cbFilterTT = new JComboBox<>(new String[]{"Tất cả", TT_CHUA, TT_DANG, TT_HOAN, TT_HUY});
        cbFilterTT.setFont(F_SMALL);
        cbFilterTT.setPreferredSize(new Dimension(160, 30));
        bar.add(cbFilterTT);

        // Sep
        bar.add(filterSep());

        // --- Nút Lọc ---
        JButton btnLoc = makeSmBtn("Lọc", true);
        btnLoc.addActionListener(e -> applyFilter());
        bar.add(btnLoc);

        // Load danh sách tuyến vào combo lọc
        SwingUtilities.invokeLater(this::loadTuyenFilter);
        return bar;
    }

    private JLabel filterLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(F_SMALL); l.setForeground(TEXT_MID); return l;
    }
    private JSeparator filterSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 24)); s.setForeground(BORDER); return s;
    }
    private JButton makeSmBtn(String text, boolean primary) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primary
                        ? (getModel().isRollover() ? ACCENT_HVR : ACCENT)
                        : (getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (!primary) { g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_SMALL);
        b.setForeground(primary ? Color.WHITE : BTN2_FG);
        b.setPreferredSize(new Dimension(primary ? 60 : 80, 30));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void loadTuyenFilter() {
        cbFilterTuyen.removeAllItems();
        cbFilterTuyen.addItem("Tất cả");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (int i = 0; i < modelCT.getRowCount(); i++) {
            Object v = modelCT.getValueAt(i, 4);
            if (v != null && !v.toString().isBlank()) set.add(v.toString().trim());
        }
        set.forEach(cbFilterTuyen::addItem);
    }

    private void applyFilter() {
        String fTuyen = (String) cbFilterTuyen.getSelectedItem();
        // Lấy ngày từ LGoodDatePicker — null nếu chưa chọn
        String fNgayStr = dcFilterNgay.getDate().trim();
        String fTT      = (String) cbFilterTT.getSelectedItem();

        boolean allTuyen = fTuyen == null || "Tất cả".equals(fTuyen);
        boolean allNgay  = fNgayStr.isEmpty();
        boolean allTT    = fTT == null || "Tất cả".equals(fTT);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelCT);
        tableCT.setRowSorter(sorter);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                boolean okTuyen = allTuyen || e.getStringValue(4).trim().equals(fTuyen);
                boolean okNgay  = allNgay  || e.getStringValue(7).trim().equals(fNgayStr);
                boolean okTT    = allTT    || e.getStringValue(5).trim().equals(fTT);
                return okTuyen && okNgay && okTT;
            }
        });
    }

    private void resetFilter() {
        cbFilterTuyen.setSelectedIndex(0);
        dcFilterNgay.resetDate();
        cbFilterTT.setSelectedIndex(0);
        tableCT.setRowSorter(null);
    }

    // =========================================================================
    // PANEL PHẢI
    // =========================================================================
    private JPanel buildRightPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        // Icon lịch vẽ tay
        JLabel icoLich = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.drawRoundRect(cx-9, cy-7, 18, 16, 3, 3);
                g2.drawLine(cx-9, cy-3, cx+9, cy-3);
                g2.drawLine(cx-4, cy-11, cx-4, cy-5);
                g2.drawLine(cx+4, cy-11, cx+4, cy-5);
                g2.fillOval(cx-6, cy, 3, 3);
                g2.fillOval(cx-1, cy, 3, 3);
                g2.fillOval(cx+4, cy, 3, 3);
                g2.fillOval(cx-6, cy+4, 3, 3);
                g2.fillOval(cx-1, cy+4, 3, 3);
                g2.dispose();
            }
        };
        icoLich.setPreferredSize(new Dimension(28, 28));

        JLabel title = new JLabel("CHI TIẾT LỊCH TRÌNH");
        title.setFont(F_TITLE); title.setForeground(TEXT_DARK);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        titleRow.add(icoLich);
        titleRow.add(title);

        JPanel top = new JPanel(); top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(titleRow);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel cardLT = makeCard(new BorderLayout());
        JScrollPane sc = new JScrollPane(tableLT);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        sc.setPreferredSize(new Dimension(0, 160));
        sc.setMinimumSize(new Dimension(0, 160));
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        styleScrollBar(sc.getVerticalScrollBar());
        cardLT.add(sc, BorderLayout.CENTER);

        JPanel cardInfo = buildInfoCard();

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(cardLT,   BorderLayout.CENTER);
        center.add(cardInfo, BorderLayout.SOUTH);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(center, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel buildInfoCard() {
        JPanel card = makeCard(new BorderLayout());
        // Kích thước cố định – tránh panel thay đổi khi cập nhật label
        card.setPreferredSize(new Dimension(0, 210));
        card.setMinimumSize(new Dimension(0, 210));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        JPanel body = new JPanel(); body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        body.add(sectionLbl("Thông tin Tàu"));
        body.add(Box.createVerticalStrut(6));
        body.add(makeTblHeader(new String[]{"Mã Tàu", "Tên Tàu", "Số Toa"}));
        body.add(makeTblRow(new JLabel[]{lblTauMa, lblTauTen, lblTauSoToa}));
        body.add(Box.createVerticalStrut(12));
        body.add(sectionLbl("Tuyến"));
        body.add(Box.createVerticalStrut(6));
        body.add(makeTblHeader(new String[]{"Mã Tuyến", "Ga Đi", "Ga Đến"}));
        body.add(makeTblRow(new JLabel[]{lblTuyenMa, lblTuyenGaDi, lblTuyenGaDen}));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // XÂY DỰNG BẢNG
    // =========================================================================
    private JTable buildTableCT() {
        JTable t = new JTable(modelCT) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Ẩn 4 cột cuối (index 6..9)
        for (int i = 6; i <= 9; i++) {
            t.getColumnModel().getColumn(i).setMinWidth(0);
            t.getColumnModel().getColumn(i).setMaxWidth(0);
            t.getColumnModel().getColumn(i).setWidth(0);
        }

        // Renderer màu cho cột Trạng Thái (index 5)
        t.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());

        int[] w = {120, 200, 90, 90, 180, 150};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyPaddingRenderer(t, 5);
        return t;
    }

    private JTable buildTableLT() {
        JTable t = new JTable(modelLT) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        int[] w = {80, 130, 80, 150};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        applyPaddingRenderer(t, 4);
        return t;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(36); t.setFont(F_CELL);
        t.setBackground(BG_CARD);
        t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK);
        t.setGridColor(BORDER);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new HeaderRenderer());
        h.setPreferredSize(new Dimension(0, 40));
        h.setReorderingAllowed(false);
    }

    private void applyPaddingRenderer(JTable t, int cols) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setFont(F_CELL);
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < cols; i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
    }

    // =========================================================================
    // TÍNH TRẠNG THÁI THEO THỜI GIAN THỰC
    // =========================================================================
    private String tinhTrangThai(String ngayKH, String gioDi, String gioDenRaw) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
            Date batDau = sdf.parse(ngayKH + " " + gioDi);

            String gioThuanTuy = gioDenRaw.replaceAll("\\s*\\(.*\\)", "").trim();
            int plusDays = gioDenRaw.contains("+2") ? 2 : gioDenRaw.contains("+1") ? 1 : 0;

            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(sdf.parse(ngayKH + " " + gioThuanTuy));
            calEnd.add(Calendar.DAY_OF_MONTH, plusDays);

            Date now = new Date();
            if (now.before(batDau))        return TT_CHUA;
            if (now.after(calEnd.getTime())) return TT_HOAN;
            return TT_DANG;
        } catch (Exception ex) {
            return TT_CHUA;
        }
    }

    // =========================================================================
    // LÀM MỚI CHI TIẾT
    // =========================================================================
    private void refreshDetail() {
        int row = tableCT.getSelectedRow();
        modelLT.setRowCount(0);
        if (row < 0) { resetInfo(); return; }

        // Tính lại trạng thái nếu không bị hủy
        String ttHienTai = modelCT.getValueAt(row, 5).toString();
        Object ngayKHObj = modelCT.getValueAt(row, 7);
        Object gioDiObj  = modelCT.getValueAt(row, 8);
        Object gioDenObj = modelCT.getValueAt(row, 9);

        if (!ttHienTai.equals(TT_HUY) && ngayKHObj != null
                && gioDiObj != null && gioDenObj != null) {
            String tt = tinhTrangThai(
                    ngayKHObj.toString(), gioDiObj.toString(), gioDenObj.toString());
            modelCT.setValueAt(tt, row, 5);
        }

        // Hiển thị lịch trình
        Object maLT = modelCT.getValueAt(row, 6);
        if (maLT != null && !maLT.toString().equals("-") && ngayKHObj != null)
            modelLT.addRow(new Object[]{maLT, ngayKHObj, gioDiObj, gioDenObj});

        // Info Tàu — lấy thông tin thật từ DB
        String maTauVal = modelCT.getValueAt(row, 2).toString();
        String tenTauVal = getTenTauFromDB(maTauVal);
        String soToaVal  = getSoToaFromDB(maTauVal);
        lblTauMa.setText(maTauVal);
        lblTauTen.setText(tenTauVal);
        lblTauSoToa.setText(soToaVal);

        // Info Tuyến — lấy Ga Đi / Ga Đến thật từ DB
        String maTuyenVal = modelCT.getValueAt(row, 3).toString();
        lblTuyenMa.setText(maTuyenVal);
        String[] gaInfo = getGaFromDB(maTuyenVal);
        lblTuyenGaDi.setText(gaInfo[0]);
        lblTuyenGaDen.setText(gaInfo[1]);
    }

    private void resetInfo() {
        lblTauMa.setText("-"); lblTauTen.setText("-"); lblTauSoToa.setText("-");
        lblTuyenMa.setText("-"); lblTuyenGaDi.setText("-"); lblTuyenGaDen.setText("-");
    }

    // =========================================================================
    // LẤY THÔNG TIN TÀU TỪ DB
    // =========================================================================
    private String getSoToaFromDB(String maTau) {
        try {
            List<Tau> ds = daoTau.getAllTau();
            for (Tau t : ds) {
                if (t.getMaTau().equalsIgnoreCase(maTau.trim()))
                    return String.valueOf(t.getSoToa());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "-";
    }

    // =========================================================================
    // LẤY GA ĐI / GA ĐẾN TỪ DB THEO MÃ TUYẾN
    // =========================================================================
    private String[] getGaFromDB(String maTuyen) {
        String sql = "SELECT t.gaDi, t.gaDen, g1.tenGa AS tenGaDi, g2.tenGa AS tenGaDen " +
                "FROM Tuyen t " +
                "LEFT JOIN Ga g1 ON t.gaDi  = g1.maGa " +
                "LEFT JOIN Ga g2 ON t.gaDen = g2.maGa " +
                "WHERE t.maTuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String gaDi  = rs.getString("tenGaDi");
                String gaDen = rs.getString("tenGaDen");
                return new String[]{
                        gaDi  != null ? gaDi  : rs.getString("gaDi"),
                        gaDen != null ? gaDen : rs.getString("gaDen")
                };
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new String[]{"-", "-"};
    }

    // =========================================================================
    // DIALOG THÊM
    // =========================================================================
    private void openAddDialog() {
        JDialog dlg = makeDialog("Thêm chuyến tàu mới");

        JTextField txtMaChuyen  = roField(peekMaChuyen());
        JTextField txtMaLT      = roField(peekMaLT());

        // Mã tàu: combo chọn tàu có sẵn HOẶC nhập tay
        String[] dsTau = getDanhSachTau();
        JComboBox<String> cbMaTau = new JComboBox<>(dsTau);
        cbMaTau.setEditable(true);
        cbMaTau.setFont(F_CELL);
        ((JTextField) cbMaTau.getEditor().getEditorComponent()).setText("");
        ((JTextField) cbMaTau.getEditor().getEditorComponent())
                .setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER,1,true),
                        BorderFactory.createEmptyBorder(4,8,4,8)));
        if (dsTau.length > 0) cbMaTau.setSelectedIndex(0);

        JTextField txtTenChuyen = roField("(tự sinh theo mã tàu + tuyến)");
        txtTenChuyen.setEditable(false);

        // Tuyến: load từ DB thật
        String[][] dsTuyen = getDanhSachTuyen(); // [i][0]=maTuyen, [i][1]=tenTuyen
        String[] tenTuyenArr = new String[dsTuyen.length];
        for (int i = 0; i < dsTuyen.length; i++) tenTuyenArr[i] = dsTuyen[i][1];
        JComboBox<String> cbTuyen = makeCombo(tenTuyenArr.length > 0 ? tenTuyenArr :
                new String[]{"Hà Nội - Sài Gòn", "Hà Nội - Đà Nẵng", "Sài Gòn - Hà Nội"});

        // Hàm cập nhật tên chuyến tự động: MaTau + " - " + TenTuyen
        Runnable updateTenChuyen = () -> {
            Object selTau = cbMaTau.getSelectedItem();
            String maTauStr = selTau != null ? selTau.toString().trim() : "";
            String tenTuyenStr = cbTuyen.getSelectedItem() != null ? cbTuyen.getSelectedItem().toString().trim() : "";
            if (!maTauStr.isEmpty() && !tenTuyenStr.isEmpty()) {
                txtTenChuyen.setText(maTauStr + " - " + tenTuyenStr);
            } else if (!maTauStr.isEmpty()) {
                txtTenChuyen.setText(maTauStr);
            } else {
                txtTenChuyen.setText("(tự sinh theo mã tàu + tuyến)");
            }
        };
        // Listener sẽ được gắn trong phần form bên dưới (kèm updateHint)

        JComboBox<String> cbSoNgay = makeCombo(new String[]{"+1 ngày", "+2 ngày"});

        DatePickerField   dpNgay  = new DatePickerField("");
        JComboBox<String> cbGioDi  = makeComboGio("06:00");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();

        int r = 0;
        addSep(form, gc, r++, "Thông tin Chuyến tàu", ACCENT);
        addRow(form, gc, r++, "Mã tàu *:",    cbMaTau);
        addRow(form, gc, r++, "Tuyến *:",      cbTuyen);
        addRow(form, gc, r++, "Số ngày đi:",   cbSoNgay);

        // Tên chuyến hiện như hint nhỏ (readonly, không chiếm row riêng)
        JLabel lblTenHint = new JLabel("Tên: " + txtTenChuyen.getText());
        lblTenHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTenHint.setForeground(TEXT_LIGHT);
        gc.gridx=0; gc.gridy=r++; gc.gridwidth=2; gc.weightx=1;
        gc.insets=new Insets(0,6,6,6);
        form.add(lblTenHint, gc);
        gc.gridwidth=1; gc.insets=new Insets(5,6,5,6);

        // Cập nhật hint khi thay đổi tàu/tuyến
        Runnable updateHint = () -> lblTenHint.setText("Tên: " + txtTenChuyen.getText());
        cbMaTau.addActionListener(e2 -> { updateTenChuyen.run(); updateHint.run(); });
        cbTuyen.addActionListener(e2 -> { updateTenChuyen.run(); updateHint.run(); });

        addSep(form, gc, r++, "Lịch trình", new Color(0x27AE60));
        addRow(form, gc, r++, "Ngày KH *:",   dpNgay);
        addRow(form, gc, r,   "Giờ đi *:",    cbGioDi);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            // --- VALIDATE Mã tàu ---
            Object selTau = cbMaTau.getSelectedItem();
            String maTau = selTau != null ? selTau.toString().trim() : "";
            if (maTau.isEmpty()) {
                warn("Vui lòng nhập hoặc chọn Mã tàu.");
                cbMaTau.requestFocus();
                return;
            }

            // --- VALIDATE Mã tuyến ---
            String tenTuyenChon = (String) cbTuyen.getSelectedItem();
            String maTuyenThuc  = getMaTuyenFromTen(tenTuyenChon);
            if (maTuyenThuc == null || maTuyenThuc.isEmpty()) {
                warn("Không tìm thấy mã tuyến trong database! Kiểm tra lại tuyến đã chọn.");
                return;
            }

            // --- VALIDATE Ngày ---
            String ngayKH = dpNgay.getDate().trim();
            if (ngayKH.isEmpty()) {
                warn("Vui lòng chọn ngày khởi hành."); return;
            }
            try { new SimpleDateFormat(DATE_FMT).parse(ngayKH); }
            catch (Exception ex) { warn("Ngày không hợp lệ! Định dạng: dd/MM/yyyy"); return; }

            String gioDiVal  = (String) cbGioDi.getSelectedItem();
            // Giờ đến = giờ đi + số ngày (tự động tính)
            String soNgay    = (String) cbSoNgay.getSelectedItem();
            String gioDenVal = gioDiVal + " (" + soNgay + ")";

            // --- KIỂM TRA TRÙNG LỊCH ---
            // Kiểm tra chồng lấp thời gian: chuyến mới không được bắt đầu trước khi chuyến cũ kết thúc
            String trungTenAdd = kiemTraTrungLich(maTau, ngayKH, gioDiVal, soNgay, null);
            if (trungTenAdd != null) {
                warn("Tàu " + maTau + " đang có chuyến \"" + trungTenAdd + "\" chưa kết thúc.\n" +
                        "Vui lòng chọn thời gian sau khi chuyến đó hoàn thành.");
                return;
            }

            String tenChuyen = txtTenChuyen.getText().trim();
            if (tenChuyen.startsWith("(") || tenChuyen.isEmpty()) {
                tenChuyen = maTau + " - " + tenTuyenChon;
            }

            String maChuyen = nextMaChuyen();
            String maLT     = nextMaLT();
            String tt       = tinhTrangThai(ngayKH, gioDiVal, gioDenVal);

            // 1. INSERT vào ChuyenTau
            boolean okChuyen = daoChuyenTau.insert(maChuyen, tenChuyen, maTau, maTuyenThuc);

            // 2. INSERT vào LichTrinh
            if (okChuyen) {
                daoLichTrinh.insert(maLT, ngayKH, gioDiVal, maChuyen);
            }

            if (okChuyen) {
                modelCT.addRow(new Object[]{
                        maChuyen, tenChuyen, maTau, maTuyenThuc, tenTuyenChon, tt,
                        maLT, ngayKH, gioDiVal, gioDenVal
                });
                updateStats();
                loadTuyenFilter();
                dlg.dispose();
            } else {
                cntChuyen--; cntLT--;
                warn("Lỗi khi lưu vào database! Kiểm tra lại Mã tàu.");
            }
        });

        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // DIALOG CẬP NHẬT (dùng cả từ nút và double-click)
    // =========================================================================
    private void openUpdateDialog() {
        int row = tableCT.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một chuyến tàu."); return; }

        String maChuyen  = modelCT.getValueAt(row, 0).toString();
        String maTau     = modelCT.getValueAt(row, 2).toString();
        String tenChuyen = modelCT.getValueAt(row, 1).toString();
        String maTuyen   = modelCT.getValueAt(row, 3).toString();
        String tuyen     = modelCT.getValueAt(row, 4).toString();
        String tt        = modelCT.getValueAt(row, 5).toString();

        Object maLTObj   = modelCT.getValueAt(row, 6);
        Object ngayKHObj = modelCT.getValueAt(row, 7);
        Object gioDiObj  = modelCT.getValueAt(row, 8);
        Object gioDenObj = modelCT.getValueAt(row, 9);

        String maLT      = maLTObj   != null ? maLTObj.toString()   : "-";
        String ngayKH    = ngayKHObj != null ? ngayKHObj.toString()  : "";
        String gioDiVal  = gioDiObj  != null ? gioDiObj.toString()   : "06:00";
        String gioDenRaw = gioDenObj != null ? gioDenObj.toString()  : "05:00 (+1 ngày)";

        String gioDenClean = gioDenRaw.replaceAll("\\s*\\(.*\\)", "").trim();
        String soNgayVal   = gioDenRaw.contains("+2") ? "+2 ngày" : "+1 ngày";

        JDialog dlg = makeDialog("Cập nhật chuyến tàu");

        JTextField txtMaChuyen  = roField(maChuyen);
        JTextField txtMaTau     = makeFieldVal(maTau);
        JTextField txtTenChuyen = roField(tenChuyen);
        JTextField txtMaTuyen   = roField(maTuyen);
        JTextField txtMaLT      = roField(maLT);

        // Tuyến: load từ DB thật
        String[][] dsTuyen = getDanhSachTuyen();
        String[] tenTuyenArr2 = new String[dsTuyen.length];
        for (int i = 0; i < dsTuyen.length; i++) tenTuyenArr2[i] = dsTuyen[i][1];
        JComboBox<String> cbTuyen = makeCombo(tenTuyenArr2.length > 0 ? tenTuyenArr2 :
                new String[]{"Hà Nội - Sài Gòn","Hà Nội - Đà Nẵng","Sài Gòn - Hà Nội"});
        // Chọn tuyến hiện tại — map từ maTuyen → tenTuyen
        for (String[] pair : dsTuyen) {
            if (pair[0].equals(tuyen) || pair[1].equals(tuyen)) {
                cbTuyen.setSelectedItem(pair[1]); break;
            }
        }
        if (cbTuyen.getSelectedIndex() < 0) cbTuyen.setSelectedItem(tuyen);

        // Hàm cập nhật tên chuyến tự động: MaTau + " - " + TenTuyen
        Runnable updateTenChuyenUpd = () -> {
            String maTauStr = txtMaTau.getText().trim();
            String tenTuyenStr = cbTuyen.getSelectedItem() != null ? cbTuyen.getSelectedItem().toString().trim() : "";
            if (!maTauStr.isEmpty() && !tenTuyenStr.isEmpty()) {
                txtTenChuyen.setText(maTauStr + " - " + tenTuyenStr);
            } else if (!maTauStr.isEmpty()) {
                txtTenChuyen.setText(maTauStr);
            }
        };
        // Listener sẽ được gắn trong phần form bên dưới (kèm updateHintUpd)

        JComboBox<String> cbSoNgay = makeCombo(new String[]{"+1 ngày", "+2 ngày"});
        cbSoNgay.setSelectedItem(soNgayVal);

        JComboBox<String> cbTT = makeCombo(DS_TRANG_THAI);
        cbTT.setSelectedItem(tt);

        DatePickerField   dpNgay  = new DatePickerField(ngayKH);
        JComboBox<String> cbGioDi  = makeComboGio(gioDiVal);
        JComboBox<String> cbGioDen = makeComboGio(gioDenClean);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();

        int r = 0;
        addSep(form, gc, r++, "Thông tin Chuyến tàu", ACCENT);
        addRow(form, gc, r++, "Mã tàu *:",    txtMaTau);
        addRow(form, gc, r++, "Tuyến *:",      cbTuyen);
        addRow(form, gc, r++, "Số ngày đi:",   cbSoNgay);
        addRow(form, gc, r++, "Trạng thái:",   cbTT);

        // Hiện tên chuyến như hint nhỏ
        JLabel lblTenHint = new JLabel("Tên: " + txtTenChuyen.getText());
        lblTenHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTenHint.setForeground(TEXT_LIGHT);
        gc.gridx=0; gc.gridy=r++; gc.gridwidth=2; gc.weightx=1;
        gc.insets=new Insets(0,6,6,6);
        form.add(lblTenHint, gc);
        gc.gridwidth=1; gc.insets=new Insets(5,6,5,6);

        Runnable updateHintUpd = () -> lblTenHint.setText("Tên: " + txtTenChuyen.getText());
        txtMaTau.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTenChuyenUpd.run(); updateHintUpd.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTenChuyenUpd.run(); updateHintUpd.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTenChuyenUpd.run(); updateHintUpd.run(); }
        });
        cbTuyen.addActionListener(e2 -> { updateTenChuyenUpd.run(); updateHintUpd.run(); });

        addSep(form, gc, r++, "Lịch trình", new Color(0x27AE60));
        addRow(form, gc, r++, "Ngày KH *:",   dpNgay);
        addRow(form, gc, r,   "Giờ đi *:",    cbGioDi);

        final int selRow = row;
        final String maLTFinal = maLT;
        final String maChuyenFinal = maChuyen;
        JButton btnCapNhat = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnCapNhat.addActionListener(e -> {
            String newGioDi     = (String) cbGioDi.getSelectedItem();
            String soNgayUpd    = (String) cbSoNgay.getSelectedItem();
            String newGioDen    = newGioDi + " (" + soNgayUpd + ")";
            String newTT        = (String) cbTT.getSelectedItem();
            String newMaTau     = txtMaTau.getText().trim();
            String newTenChuyen = txtTenChuyen.getText().trim();
            String newTuyen     = (String) cbTuyen.getSelectedItem();

            // --- VALIDATE Mã tàu ---
            if (newMaTau.isEmpty()) {
                warn("Vui lòng nhập Mã tàu."); txtMaTau.requestFocus(); return;
            }

            // --- VALIDATE Ngày ---
            String newNgayKH = dpNgay.getDate().trim();
            if (newNgayKH.isEmpty()) { warn("Vui lòng chọn ngày khởi hành."); return; }
            try { new SimpleDateFormat(DATE_FMT).parse(newNgayKH); }
            catch (Exception ex) { warn("Ngày không hợp lệ! Định dạng: dd/MM/yyyy"); return; }

            // --- KIỂM TRA TRÙNG LỊCH (bỏ qua chính chuyến đang cập nhật) ---
            String trungTenUpd = kiemTraTrungLich(newMaTau, newNgayKH, newGioDi, soNgayUpd, maChuyenFinal);
            if (trungTenUpd != null) {
                warn("Tàu " + newMaTau + " đang có chuyến \"" + trungTenUpd + "\" chưa kết thúc.\n" +
                        "Vui lòng chọn thời gian sau khi chuyến đó hoàn thành.");
                return;
            }

            if (!newTT.equals(TT_HUY))
                newTT = tinhTrangThai(newNgayKH, newGioDi, newGioDen);

            // 1. UPDATE ChuyenTau trong DB
            boolean okChuyen = daoChuyenTau.update(
                    maChuyenFinal, newTenChuyen, newMaTau,
                    modelCT.getValueAt(selRow, 3).toString());

            // 2. UPDATE LichTrinh trong DB
            daoLichTrinh.update(maLTFinal, newNgayKH, newGioDi, maChuyenFinal);

            if (okChuyen) {
                modelCT.setValueAt(newMaTau,     selRow, 2);
                modelCT.setValueAt(newTenChuyen, selRow, 1);
                modelCT.setValueAt(newTuyen,     selRow, 4);
                modelCT.setValueAt(newTT,        selRow, 5);
                modelCT.setValueAt(newNgayKH,    selRow, 7);
                modelCT.setValueAt(newGioDi,     selRow, 8);
                modelCT.setValueAt(newGioDen,    selRow, 9);
                refreshDetail();
                updateStats();
                dlg.dispose();
            } else {
                warn("Lỗi khi cập nhật database!");
            }
        });

        showDlg(dlg, form, btnCapNhat);
    }

    // =========================================================================
    // LOAD DỮ LIỆU TỪ DATABASE KHI KHỞI ĐỘNG
    // =========================================================================
    private void loadFromDB() {
        modelCT.setRowCount(0);
        try {
            List<ChuyenTauRow> dsChuyen = daoChuyenTau.getAll();
            List<LichTrinhRow> dsLT     = daoLichTrinh.getAll();

            // Đồng bộ bộ đếm với DB — tránh trùng mã khi thêm mới
            // Đếm số lớn nhất trong DB để tiếp tục từ đó
            cntChuyen = calcNextCnt(dsChuyen.stream()
                    .map(c -> c.maChuyen).toArray(String[]::new), "CT", 2);
            cntLT = calcNextCnt(dsLT.stream()
                    .map(l -> l.maLT).toArray(String[]::new), "LT", 2);

            for (ChuyenTauRow ct : dsChuyen) {
                String maLT  = "-", ngayKH = "-", gioDi = "-", gioDen = "-";
                for (LichTrinhRow lt : dsLT) {
                    if (lt.maChuyen.equals(ct.maChuyen)) {
                        maLT   = lt.maLT;
                        ngayKH = lt.ngayKhoiHanh;
                        gioDi  = lt.gioKhoiHanh;
                        gioDen = lt.gioKhoiHanh + " (+1 ngày)";
                        break;
                    }
                }
                String tenTuyen = getTenTuyenFromDB(ct.maTuyen);
                String tt = tinhTrangThai(ngayKH, gioDi, gioDen);
                modelCT.addRow(new Object[]{
                        ct.maChuyen, ct.tenChuyen, ct.maTau,
                        ct.maTuyen,  tenTuyen,     tt,
                        maLT, ngayKH, gioDi, gioDen
                });
            }
            updateStats(); // Cập nhật thẻ thống kê
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu từ database: " + e.getMessage());
        }
    }

    /**
     * Tính số thứ tự tiếp theo dựa trên danh sách mã hiện có trong DB.
     * VD: ["CHUYEN0001","CHUYEN0004"] → prefix="CHUYEN", skip=6 → trả về 5
     */
    private int calcNextCnt(String[] codes, String prefix, int skip) {
        int max = 0;
        for (String code : codes) {
            try {
                int num = Integer.parseInt(code.substring(prefix.length()));
                if (num > max) max = num;
            } catch (Exception ignored) {}
        }
        return max + 1;
    }

    // =========================================================================
    // KIỂM TRA TRÙNG LỊCH
    // Quy tắc: cùng maTau + cùng ngayKH + cùng giờ đi → trùng lịch
    // excludeMaChuyen: bỏ qua chính chuyến đang cập nhật (null khi thêm mới)
    // =========================================================================
    /**
     * Kiểm tra xem tàu có bị trùng/chồng lịch không.
     *
     * Nghiệp vụ: Một tàu KHÔNG thể bắt đầu chuyến mới
     * khi chuyến cũ chưa kết thúc.
     *
     * Ví dụ: SE1 khởi hành 06:00 ngày 04/04, +2 ngày
     *   → kết thúc: 06:00 ngày 06/04
     *   → chuyến mới của SE1 phải bắt đầu SAU 06:00 ngày 06/04
     *
     * @param maTau           mã tàu cần kiểm tra
     * @param ngayKHMoi       ngày khởi hành chuyến mới (dd/MM/yyyy)
     * @param gioDiMoi        giờ đi chuyến mới (HH:mm)
     * @param soNgayMoi       "+1 ngày" hoặc "+2 ngày" của chuyến mới
     * @param excludeMaChuyen mã chuyến bỏ qua khi cập nhật (null khi thêm)
     * @return tên chuyến bị xung đột, null nếu không trùng
     */
    private String kiemTraTrungLich(String maTau, String ngayKHMoi,
                                    String gioDiMoi, String soNgayMoi,
                                    String excludeMaChuyen) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
        Date batDauMoi, ketThucMoi;
        try {
            batDauMoi = sdf.parse(ngayKHMoi + " " + gioDiMoi);
            int plusMoi = soNgayMoi != null && soNgayMoi.contains("+2") ? 2 : 1;
            Calendar c = Calendar.getInstance();
            c.setTime(batDauMoi);
            c.add(Calendar.DAY_OF_MONTH, plusMoi);
            ketThucMoi = c.getTime();
        } catch (Exception e) {
            return null; // không parse được → bỏ qua
        }

        for (int i = 0; i < modelCT.getRowCount(); i++) {
            String maTauRow    = modelCT.getValueAt(i, 2).toString();
            String maChuyenRow = modelCT.getValueAt(i, 0).toString();
            String ttRow       = modelCT.getValueAt(i, 5).toString();
            Object ngayKHObj   = modelCT.getValueAt(i, 7);
            Object gioDiObj    = modelCT.getValueAt(i, 8);
            Object gioDenObj   = modelCT.getValueAt(i, 9);
            Object tenChuyenObj= modelCT.getValueAt(i, 1);

            // Bỏ qua chính chuyến đang cập nhật
            if (excludeMaChuyen != null && maChuyenRow.equals(excludeMaChuyen)) continue;

            // Chỉ kiểm tra cùng tàu, không kiểm tra chuyến đã hủy
            if (!maTauRow.equalsIgnoreCase(maTau)) continue;
            if (TT_HUY.equals(ttRow)) continue;
            if (ngayKHObj == null || gioDiObj == null) continue;

            try {
                // Thời gian bắt đầu chuyến hiện có
                Date batDauCu = sdf.parse(ngayKHObj.toString() + " " + gioDiObj.toString());

                // Thời gian kết thúc chuyến hiện có
                Date ketThucCu;
                if (gioDenObj != null && !gioDenObj.toString().equals("-")) {
                    String gioDenRaw = gioDenObj.toString();
                    String gioThuanTuy = gioDenRaw.replaceAll("\\s*\\(.*\\)", "").trim();
                    int plusDays = gioDenRaw.contains("+2") ? 2
                            : gioDenRaw.contains("+1") ? 1 : 0;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sdf.parse(ngayKHObj.toString() + " " + gioThuanTuy));
                    cal.add(Calendar.DAY_OF_MONTH, plusDays);
                    ketThucCu = cal.getTime();
                } else {
                    // Không có giờ đến → mặc định +1 ngày
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(batDauCu);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    ketThucCu = cal.getTime();
                }

                // Kiểm tra chồng lấp: [batDauMoi, ketThucMoi] ∩ [batDauCu, ketThucCu] ≠ ∅
                // Chồng lấp khi: batDauMoi < ketThucCu AND ketThucMoi > batDauCu
                boolean chongLap = batDauMoi.before(ketThucCu) && ketThucMoi.after(batDauCu);
                if (chongLap) {
                    return tenChuyenObj != null ? tenChuyenObj.toString() : maChuyenRow;
                }
            } catch (Exception ex) {
                // parse lỗi → bỏ qua dòng này
            }
        }
        return null; // Không trùng
    }

    // =========================================================================
    // LẤY DANH SÁCH TÀU TỪ DATABASE (cho combo Mã tàu)
    // =========================================================================
    private String[] getDanhSachTau() {
        try {
            List<Tau> ds = daoTau.getAllTau();
            if (ds == null || ds.isEmpty()) return new String[]{};
            String[] arr = new String[ds.size()];
            for (int i = 0; i < ds.size(); i++) arr[i] = ds.get(i).getMaTau();
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{};
        }
    }

    // =========================================================================
    // LẤY TÊN TÀU TỪ DATABASE THEO MÃ TÀU
    // =========================================================================
    private String getTenTauFromDB(String maTau) {
        try {
            List<Tau> ds = daoTau.getAllTau();
            for (Tau t : ds) {
                if (t.getMaTau().equalsIgnoreCase(maTau.trim()))
                    return t.getTenTau();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return maTau;
    }

    // =========================================================================
    // LẤY DANH SÁCH TUYẾN TỪ DB — trả về mảng [maTuyen, tenTuyen]
    // =========================================================================
    private String[][] getDanhSachTuyen() {
        String sql = "SELECT maTuyen, tenTuyen FROM Tuyen ORDER BY maTuyen ASC";
        List<String[]> list = new java.util.ArrayList<>();
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("maTuyen"), rs.getString("tenTuyen")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list.toArray(new String[0][]);
    }

    // =========================================================================
    // LẤY maTuyen TỪ tenTuyen
    // =========================================================================
    private String getMaTuyenFromTen(String tenTuyen) {
        if (tenTuyen == null) return null;
        String sql = "SELECT maTuyen FROM Tuyen WHERE tenTuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTuyen);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("maTuyen");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =========================================================================
    // LẤY tenTuyen TỪ maTuyen
    // =========================================================================
    private String getTenTuyenFromDB(String maTuyen) {
        if (maTuyen == null || maTuyen.equals("-")) return maTuyen;
        String sql = "SELECT tenTuyen FROM Tuyen WHERE maTuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("tenTuyen");
        } catch (Exception e) { e.printStackTrace(); }
        return maTuyen; // fallback
    }

    // =========================================================================
    // HELPER UI
    // =========================================================================
    /**
     * Tạo LGoodDatePicker đã style theo palette ứng dụng.
     * @param initDate ngày khởi tạo dạng "dd/MM/yyyy", null = không chọn sẵn
     */
    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }
    private JPanel makeTblHeader(String[] cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length, 0, 0));
        p.setBackground(TH_BG);
        p.setBorder(BorderFactory.createMatteBorder(1,1,0,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String c : cols) {
            JLabel l = new JLabel(c);
            l.setFont(F_LABEL); l.setForeground(TEXT_DARK);
            l.setBorder(BorderFactory.createEmptyBorder(6,10,6,4));
            l.setMinimumSize(new Dimension(70, 30));
            p.add(l);
        }
        return p;
    }
    private JPanel makeTblRow(JLabel[] labels) {
        JPanel p = new JPanel(new GridLayout(1, labels.length, 0, 0));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1,1,1,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JLabel l : labels) {
            l.setBorder(BorderFactory.createEmptyBorder(6,10,6,4));
            l.setMinimumSize(new Dimension(70, 30));
            p.add(l);
        }
        return p;
    }
    private static JLabel infoLbl(String t) { JLabel l=new JLabel(t); l.setFont(F_CELL); l.setForeground(TEXT_DARK); return l; }
    private JLabel sectionLbl(String t) {
        // Icon vẽ tay tương ứng với nội dung
        JLabel l = new JLabel(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2;
                if (getText().contains("Tàu")) {
                    // Icon tàu nhỏ
                    g2.drawRoundRect(4, cy-5, 14, 8, 3, 3);
                    g2.drawLine(7,  cy-5, 7,  cy-8);
                    g2.drawLine(14, cy-5, 14, cy-8);
                    g2.drawLine(7,  cy-8, 14, cy-8);
                    g2.fillOval(5,  cy+2, 3, 3);
                    g2.fillOval(13, cy+2, 3, 3);
                } else {
                    // Icon bản đồ / tuyến
                    g2.drawRoundRect(4, cy-6, 14, 11, 2, 2);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(7,  cy-3, 15, cy-3);
                    g2.drawLine(7,  cy,   13, cy);
                    g2.drawLine(7,  cy+3, 11, cy+3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(F_LABEL);
        l.setForeground(TEXT_DARK);
        l.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0)); // indent cho icon
        return l;
    }
    private JLabel sepLbl(String t, Color c) {
        String clean = t.replaceAll("[\\u2500\\u2502\\u250C\\u2510\\u2514\\u2518\\u251C\\u2524\\u252C\\u2534\\u253C-]+", "").trim();
        JLabel l = new JLabel(clean) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tw = fm.stringWidth(getText());
                int cy = getHeight() / 2;
                int tx = (getWidth() - tw) / 2;
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 70));
                g2.setStroke(new BasicStroke(1f));
                if (tx > 8) g2.drawLine(4, cy, tx - 8, cy);
                if (tx + tw + 8 < getWidth()) g2.drawLine(tx + tw + 8, cy, getWidth() - 4, cy);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private JTextField roField(String v) { JTextField tf=makeFieldVal(v); tf.setEditable(false); tf.setBackground(new Color(0xEEF2F8)); return tf; }
    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()&&!isFocusOwner()) { Graphics2D g2=(Graphics2D)g.create(); g2.setColor(TEXT_LIGHT); g2.setFont(new Font("Segoe UI",Font.ITALIC,12)); Insets ins=getInsets(); g2.drawString(hint,ins.left+4,getHeight()/2+5); g2.dispose(); }
            }
        };
        styleField(tf); return tf;
    }
    private JTextField makeFieldVal(Object v) { JTextField tf=new JTextField(v!=null?v.toString():""); styleField(tf); return tf; }
    private void styleField(JTextField tf) {
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(6,10,6,10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_FOC,2,true),BorderFactory.createEmptyBorder(5,9,5,9))); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(6,10,6,10))); }
        });
    }
    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb=new JComboBox<>(items); cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER,1,true),BorderFactory.createEmptyBorder(2,4,2,4))); return cb;
    }
    private JComboBox<String> makeComboGio(String def) { JComboBox<String> cb=makeCombo(GIO_24H); cb.setSelectedItem(def); return cb; }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc=new GridBagConstraints(); gc.insets=new Insets(5,6,5,6); gc.anchor=GridBagConstraints.WEST; gc.fill=GridBagConstraints.HORIZONTAL; return gc;
    }
    private void addSep(JPanel form, GridBagConstraints gc, int row, String txt, Color c) {
        gc.gridx=0; gc.gridy=row; gc.gridwidth=2; gc.weightx=1;
        JLabel lbl = sepLbl(txt, c);
        lbl.setPreferredSize(new Dimension(0, 26));
        form.add(lbl, gc);
        gc.gridwidth=1;
    }
    private void addRow(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx=0; gc.gridy=row; gc.weightx=0; JLabel l=new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID); form.add(l,gc);
        gc.gridx=1; gc.weightx=1; field.setPreferredSize(new Dimension(260,36)); form.add(field,gc);
    }
    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                switch(style){
                    case PRIMARY -> { g2.setColor(getModel().isRollover()?ACCENT_HVR:ACCENT); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    case DANGER  -> { g2.setColor(getModel().isRollover()?BTN_RED_HVR:BTN_RED); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    default      -> { g2.setColor(getModel().isRollover()?new Color(0xE0ECFF):BTN2_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style==BtnStyle.SECONDARY?BTN2_FG:Color.WHITE);
        b.setPreferredSize(new Dimension(style==BtnStyle.DANGER?80:140,36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JDialog makeDialog(String title) {
        Window owner=SwingUtilities.getWindowAncestor(this);
        JDialog d=(owner instanceof Frame)?new JDialog((Frame)owner,title,true):new JDialog((Dialog)owner,title,true);
        d.setLayout(new BorderLayout()); d.getContentPane().setBackground(BG_PAGE);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return d;
    }
    private void showDlg(JDialog dlg, JPanel form, JButton ok) {
        JButton huy=makeBtn("Hủy",BtnStyle.SECONDARY); huy.addActionListener(e->dlg.dispose());
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,14)); bar.setOpaque(false); bar.add(huy); bar.add(ok);
        dlg.add(form,BorderLayout.CENTER); dlg.add(bar,BorderLayout.SOUTH);
        dlg.setResizable(false);
        dlg.pack(); dlg.setMinimumSize(new Dimension(560, dlg.getHeight()));
        dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this,msg,"Thông báo",JOptionPane.WARNING_MESSAGE); }
    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(0xC0D4EE);trackColor=BG_PAGE;}
            @Override protected JButton createDecreaseButton(int o){return zBtn();}
            @Override protected JButton createIncreaseButton(int o){return zBtn();}
            private JButton zBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
    }

    // =========================================================================
    // RENDERER TRẠNG THÁI
    // =========================================================================
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
            JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            l.setBorder(BorderFactory.createEmptyBorder(0,8,0,4));
            l.setFont(new Font("Segoe UI",Font.BOLD,12));
            String val=v!=null?v.toString():"";
            switch(val){
                case "Chưa Khởi Hành" -> l.setForeground(CLR_CHUA);
                case "Đang Khởi Hành" -> l.setForeground(CLR_DANG);
                case "Đã Hoàn Thành"  -> l.setForeground(CLR_HOAN);
                case "Đã Hủy"         -> l.setForeground(CLR_HUY);
                default               -> l.setForeground(TEXT_DARK);
            }
            if(!sel) l.setBackground(row%2==0?BG_CARD:ROW_ALT);
            return l;
        }
    }

    // =========================================================================
    // RENDERER HEADER
    // =========================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer(){setHorizontalAlignment(LEFT);}
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
            JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
            l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,6)); return l;
        }
    }

    // =========================================================================
    // SHADOW BORDER
    // =========================================================================
    private static class ShadowBorder extends AbstractBorder {
        private static final int S=4;
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i=S;i>0;i--){g2.setColor(new Color(100,140,200,(int)(20.0*(S-i)/S)));g2.drawRoundRect(x+i,y+i,w-2*i-1,h-2*i-1,12,12);}
            g2.setColor(new Color(0xE2EAF4));g2.drawRoundRect(x,y,w-1,h-1,12,12);
            g2.setColor(BG_CARD);g2.setClip(new RoundRectangle2D.Float(x+1,y+1,w-2,h-2,12,12));g2.fillRect(x+1,y+1,w-2,h-2);g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(S,S,S,S);}
        @Override public Insets getBorderInsets(Component c,Insets ins){ins.set(S,S,S,S);return ins;}
    }

    // =========================================================================
    // DATE PICKER
    // =========================================================================
    private class DatePickerField extends JPanel {
        private final JTextField   txt;
        private final Calendar     cal;
        private JPanel             pnlGrid;
        private JComboBox<String>  cbThang;
        private JComboBox<Integer> cbNam;
        private JWindow            popup;
        private static final String[] TEN_THANG = {
                "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
                "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
        };
        private static final String[] TEN_THU = {"T2","T3","T4","T5","T6","T7","CN"};

        DatePickerField(String init) {
            setLayout(new BorderLayout());
            setOpaque(false);
            cal = Calendar.getInstance();
            if (init != null && !init.isEmpty()) {
                try { cal.setTime(new SimpleDateFormat(DATE_FMT).parse(init)); }
                catch (Exception ignored) {}
            }
            String disp = (init != null && !init.isEmpty())
                    ? init : new SimpleDateFormat(DATE_FMT).format(cal.getTime());

            // ── Text field hiển thị ngày ──────────────────────────────────
            txt = new JTextField(disp);
            txt.setFont(F_CELL);
            txt.setForeground(TEXT_DARK);
            txt.setBackground(new Color(0xF8FAFD));
            txt.setEditable(false);
            txt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            txt.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 34)));

            // ── Icon lịch vẽ tay ─────────────────────────────────────────
            JLabel ico = new JLabel() {
                private boolean hovered = false;
                {
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                        @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                    });
                }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color ic = hovered ? ACCENT : TEXT_MID;
                    g2.setColor(ic);
                    int cx = getWidth()/2, cy = getHeight()/2;
                    // Khung lịch
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawRoundRect(cx-8, cy-7, 16, 14, 3, 3);
                    // Đường ngang header
                    g2.drawLine(cx-8, cy-3, cx+8, cy-3);
                    // Gai trên
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-4, cy-11, cx-4, cy-5);
                    g2.drawLine(cx+4, cy-11, cx+4, cy-5);
                    // Dots ngày
                    g2.setStroke(new BasicStroke(1f));
                    int[] dx = {cx-5, cx, cx+5, cx-5, cx};
                    int[] dy = {cy,   cy, cy,   cy+4, cy+4};
                    for (int i = 0; i < 5; i++) g2.fillOval(dx[i]-2, dy[i]-2, 4, 4);
                    g2.dispose();
                }
            };
            ico.setPreferredSize(new Dimension(30, 34));
            ico.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ico.setToolTipText("Chọn ngày");

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(txt, BorderLayout.CENTER);
            wrap.add(ico, BorderLayout.EAST);
            add(wrap, BorderLayout.CENTER);

            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { toggle(); }
            };
            txt.addMouseListener(ma);
            ico.addMouseListener(ma);
        }

        private void toggle(){if(popup!=null&&popup.isVisible()){popup.dispose();popup=null;return;}showPop();}
        private void showPop(){
            popup=new JWindow(SwingUtilities.getWindowAncestor(this));
            popup.setLayout(new BorderLayout());

            // Panel chính với shadow border
            JPanel p=new JPanel(new BorderLayout(0,8));
            p.setBackground(BG_CARD);
            p.setBorder(BorderFactory.createCompoundBorder(
                    new ShadowBorder(),
                    BorderFactory.createEmptyBorder(10,10,10,10)));

            // Header tháng/năm
            p.add(navBar(),BorderLayout.NORTH);

            // Separator mỏng
            JSeparator sep=new JSeparator();
            sep.setForeground(BORDER);
            p.add(sep,BorderLayout.CENTER);

            // Grid ngày
            pnlGrid=new JPanel(new GridLayout(0,7,2,2));
            pnlGrid.setBackground(BG_CARD);
            pnlGrid.setBorder(BorderFactory.createEmptyBorder(4,0,0,0));

            JPanel south=new JPanel(new BorderLayout());
            south.setOpaque(false);
            south.add(pnlGrid,BorderLayout.CENTER);

            // Nút "Hôm nay"
            JButton btnToday=new JButton("Hôm nay"){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(getModel().isRollover()){g2.setColor(new Color(0xEBF3FF));g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);}
                    g2.dispose();super.paintComponent(g);
                }
            };
            btnToday.setFont(F_SMALL);btnToday.setForeground(ACCENT);
            btnToday.setContentAreaFilled(false);btnToday.setBorderPainted(false);
            btnToday.setFocusPainted(false);btnToday.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnToday.addActionListener(e->{
                cal.setTime(new Date());
                cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
                cbNam.setSelectedItem(cal.get(Calendar.YEAR));
                txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));
                fillGrid();popup.dispose();popup=null;
            });

            JPanel todayBar=new JPanel(new FlowLayout(FlowLayout.CENTER,0,4));
            todayBar.setBackground(BG_CARD);
            todayBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,BORDER));
            todayBar.add(btnToday);
            south.add(todayBar,BorderLayout.SOUTH);

            p.add(south,BorderLayout.SOUTH);

            fillGrid();
            popup.add(p);popup.pack();
            popup.setSize(Math.max(240,popup.getWidth()),popup.getHeight());
            Point loc=txt.getLocationOnScreen();
            popup.setLocation(loc.x,loc.y+txt.getHeight()+4);
            popup.setVisible(true);
            popup.addWindowFocusListener(new java.awt.event.WindowFocusListener(){
                @Override public void windowGainedFocus(java.awt.event.WindowEvent e){}
                @Override public void windowLostFocus(java.awt.event.WindowEvent e){if(popup!=null){popup.dispose();popup=null;}}
            });
        }
        private JPanel navBar() {
            JPanel nav = new JPanel(new BorderLayout(6,0));
            nav.setBackground(BG_CARD);
            nav.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));

            JButton prev = navBtn("\u2039"); // ‹
            JButton next = navBtn("\u203a"); // ›
            prev.addActionListener(e -> {
                cal.add(Calendar.MONTH,-1);
                cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
                cbNam.setSelectedItem(cal.get(Calendar.YEAR));
                fillGrid();
            });
            next.addActionListener(e -> {
                cal.add(Calendar.MONTH, 1);
                cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
                cbNam.setSelectedItem(cal.get(Calendar.YEAR));
                fillGrid();
            });

            cbThang = new JComboBox<>(TEN_THANG);
            cbThang.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cbThang.setSelectedIndex(cal.get(Calendar.MONTH));
            cbThang.setPreferredSize(new Dimension(90, 28));
            cbThang.addActionListener(e -> { cal.set(Calendar.MONTH, cbThang.getSelectedIndex()); fillGrid(); });

            int y = Calendar.getInstance().get(Calendar.YEAR);
            Integer[] yrs = new Integer[16];
            for (int i = 0; i < 16; i++) yrs[i] = y - 5 + i;
            cbNam = new JComboBox<>(yrs);
            cbNam.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cbNam.setSelectedItem(cal.get(Calendar.YEAR));
            cbNam.setPreferredSize(new Dimension(64, 28));
            cbNam.addActionListener(e -> {
                if (cbNam.getSelectedItem() != null) {
                    cal.set(Calendar.YEAR, (Integer) cbNam.getSelectedItem()); fillGrid();
                }
            });

            JPanel ctr = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            ctr.setBackground(BG_CARD);
            ctr.add(cbThang); ctr.add(cbNam);

            nav.add(prev, BorderLayout.WEST);
            nav.add(ctr,  BorderLayout.CENTER);
            nav.add(next, BorderLayout.EAST);
            return nav;
        }

        private void fillGrid() {
            pnlGrid.removeAll();

            // Tên thứ — CN màu đỏ nhạt
            for (int i = 0; i < TEN_THU.length; i++) {
                JLabel l = new JLabel(TEN_THU[i], SwingConstants.CENTER);
                l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l.setPreferredSize(new Dimension(28, 22));
                l.setForeground(i == 6 ? new Color(0xEF4444) : TEXT_MID);
                pnlGrid.add(l);
            }

            Calendar tmp = (Calendar) cal.clone();
            tmp.set(Calendar.DAY_OF_MONTH, 1);
            int first = (tmp.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            Calendar today = Calendar.getInstance();
            int todayD = today.get(Calendar.DAY_OF_MONTH);
            boolean sameMonth = today.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    && today.get(Calendar.YEAR)  == cal.get(Calendar.YEAR);
            int chosen = -1;
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(new SimpleDateFormat(DATE_FMT).parse(txt.getText()));
                if (c.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                        && c.get(Calendar.YEAR) == cal.get(Calendar.YEAR))
                    chosen = c.get(Calendar.DAY_OF_MONTH);
            } catch (Exception ignored) {}

            for (int i = 0; i < first; i++) pnlGrid.add(new JLabel());

            int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            final int fc = chosen;
            for (int d = 1; d <= days; d++) {
                final int nd = d;
                boolean isToday = sameMonth && d == todayD;
                boolean isSel   = d == fc;
                boolean isSun   = (first + d - 1) % 7 == 6; // Chủ nhật

                JButton b = new JButton(String.valueOf(d)) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isSel) {
                            g2.setColor(ACCENT);
                            g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
                        } else if (getModel().isRollover()) {
                            g2.setColor(new Color(0xDDEEFF));
                            g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
                        } else if (isToday) {
                            g2.setColor(new Color(0xEBF5FF));
                            g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 8, 8);
                            g2.setColor(ACCENT);
                            g2.setStroke(new BasicStroke(1.2f));
                            g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 8, 8);
                        }
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                b.setFont(new Font("Segoe UI", isToday ? Font.BOLD : Font.PLAIN, 11));
                b.setForeground(isSel ? Color.WHITE
                        : isSun ? new Color(0xEF4444)
                        : isToday ? ACCENT
                        : TEXT_DARK);
                b.setPreferredSize(new Dimension(28, 28));
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                b.setMargin(new Insets(0,0,0,0));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e -> {
                    cal.set(Calendar.DAY_OF_MONTH, nd);
                    txt.setText(new SimpleDateFormat(DATE_FMT).format(cal.getTime()));
                    if (popup != null) { popup.dispose(); popup = null; }
                });
                pnlGrid.add(b);
            }
            pnlGrid.revalidate();
            pnlGrid.repaint();
        }

        private JButton navBtn(String t) {
            JButton b = new JButton(t);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            b.setForeground(ACCENT);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setMargin(new Insets(0,0,0,0));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(26, 26));
            return b;
        }

        public String getDate() { return txt.getText(); }
        public void resetDate() {
            cal.setTime(new Date());
            txt.setText("");
        }
    }
}