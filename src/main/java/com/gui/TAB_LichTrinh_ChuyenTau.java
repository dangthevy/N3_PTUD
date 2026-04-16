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
 *  - maChuyen : CT + XXXX    (CT01 → CT9999, tự động sinh, chỉ tăng khi xác nhận)
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
    // Bảng model chuyến tàu: 6 cột hiển thị (không còn hidden cols lịch trình)
    // Lịch trình được load riêng từ DB theo maChuyen khi chọn dòng
    // =========================================================================
    private static final String[] COLS_CT = {
            "Mã Chuyến", "Tên Chuyến", "Trạng thái"
            // Mã Tàu (index 3), Mã Tuyến (index 4), Tuyến (index 5) — ẩn, dùng nội bộ
    };
    private static final String[] COLS_LT = {
            "Mã LT", "Ngày Khởi Hành", "Giờ Đi", "Ngày Đến", "Giờ Đến", "Trạng thái"
    };

    // =========================================================================
    // TRẠNG THÁI
    // =========================================================================
    private static final String TT_CHUA = "Chưa Khởi Hành";
    private static final String TT_DANG = "Đang Khởi Hành";
    private static final String TT_HOAN = "Đã Hoàn Thành";
    private static final String TT_HUY  = "Đã Hủy";
    private static final String[] DS_TRANG_THAI = {TT_CHUA, TT_DANG, TT_HOAN, TT_HUY};

    // Trạng thái hoạt động chuyến tàu (hiển thị bảng trái)
    private static final String HOAT_DONG      = "Hoạt động";
    private static final String NGUNG_HOAT_DONG = "Ngưng hoạt động";

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
    private String peekMaLT()     { return String.format("LT%02d", cntLT);     }

    private String nextMaChuyen() { return String.format("CT%02d", cntChuyen++); }
    private String nextMaLT()     { return String.format("LT%02d", cntLT++);     }

    // =========================================================================
    // THÀNH PHẦN CHÍNH
    // =========================================================================
    private final DefaultTableModel modelCT;
    private final DefaultTableModel modelLT;
    private final JTable            tableCT;
    private final JTable            tableLT;

    private final JLabel lblTrangThai  = infoLbl("-"); // Trạng thái chuyến đang chọn
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

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER, SUCCESS }

    // =========================================================================
    // BỘ LỌC – fields
    // =========================================================================
    private JComboBox<String> cbFilterTuyen;
    private JComboBox<String> cbFilterTrangThai;

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

        // Model 6 cột: 3 hiển thị + 3 ẩn (Mã Tàu, Mã Tuyến, Tuyến)
        modelCT = new DefaultTableModel(
                new String[]{"Mã Chuyến", "Tên Chuyến", "Trạng thái", "Mã Tàu", "Mã Tuyến", "Tuyến"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCT = buildTableCT();
        // Ẩn 3 cột cuối khỏi view (vẫn có trong model)
        tableCT.removeColumn(tableCT.getColumnModel().getColumn(5)); // Tuyến
        tableCT.removeColumn(tableCT.getColumnModel().getColumn(4)); // Mã Tuyến
        tableCT.removeColumn(tableCT.getColumnModel().getColumn(3)); // Mã Tàu

        modelLT = new DefaultTableModel(COLS_LT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLT = buildTableLT();

        // Chọn dòng → cập nhật chi tiết
        tableCT.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshDetail();
        });

        // Double-click → cập nhật (chặn nếu ngưng hoạt động)
        tableCT.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableCT.getSelectedRow();
                    if (row >= 0 && NGUNG_HOAT_DONG.equals(modelCT.getValueAt(row, 2).toString())) {
                        warn("Chuyến tàu đang ngưng hoạt động!\nKhông thể chỉnh sửa.");
                        return;
                    }
                    openUpdateDialog();
                }
            }
        });

        // Panel cố định 45/55 — dùng GridBagLayout, KHÔNG JSplitPane
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        JPanel leftPanel  = buildLeftPanel();
        JPanel rightPanel = buildRightPanel();

        GridBagConstraints gcLeft = new GridBagConstraints();
        gcLeft.gridx=0; gcLeft.gridy=0; gcLeft.weightx=0.30; gcLeft.weighty=1.0;
        gcLeft.fill=GridBagConstraints.BOTH; gcLeft.insets=new Insets(0,0,0,6);
        body.add(leftPanel, gcLeft);

        GridBagConstraints gcRight = new GridBagConstraints();
        gcRight.gridx=1; gcRight.gridy=0; gcRight.weightx=0.70; gcRight.weighty=1.0;
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

        bar.add(buildStatCard("TỔNG SỐ LỊCH TRÌNH", lblStatTotal, new Color(37,  99, 235)));
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
        // Đếm tất cả lịch trình từ DB (maLT LT01..LT9999)
        List<LichTrinhRow> tatCaLT = daoLichTrinh.getAll();
        int total = tatCaLT.size(), chua = 0, dang = 0, hoan = 0;
        for (LichTrinhRow lt : tatCaLT) {
            String nd = (lt.ngayDen != null && !lt.ngayDen.isEmpty())
                    ? lt.ngayDen
                    : tinhNgayDen(lt.ngayKhoiHanh, lt.gioKhoiHanh, 1440);
            String tt = tinhTrangThai(lt.ngayKhoiHanh, lt.gioKhoiHanh, nd);
            switch (tt) {
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
        top.add(buildFilterBar(), BorderLayout.SOUTH);

        JPanel card = makeCard(new BorderLayout());
        JScrollPane sc = new JScrollPane(tableCT);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(sc.getVerticalScrollBar());
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

        // Nút Xóa chuyến — vẽ icon thùng rác bằng Graphics2D
        JButton btnXoaChuyen = new JButton("  Xóa chuyến") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Vẽ icon thùng rác
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 14, cy = getHeight() / 2;
                // Nắp
                g2.drawLine(cx - 6, cy - 5, cx + 6, cy - 5);
                g2.drawLine(cx - 2, cy - 5, cx - 2, cy - 8);
                g2.drawLine(cx + 2, cy - 5, cx + 2, cy - 8);
                g2.drawLine(cx - 2, cy - 8, cx + 2, cy - 8);
                // Thân
                g2.drawLine(cx - 5, cy - 5, cx - 4, cy + 6);
                g2.drawLine(cx + 5, cy - 5, cx + 4, cy + 6);
                g2.drawLine(cx - 4, cy + 6, cx + 4, cy + 6);
                // Sọc trong
                g2.drawLine(cx - 1, cy - 3, cx - 1, cy + 4);
                g2.drawLine(cx + 2, cy - 3, cx + 2, cy + 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnXoaChuyen.setFont(F_LABEL);
        btnXoaChuyen.setForeground(Color.WHITE);
        btnXoaChuyen.setPreferredSize(new Dimension(145, 36));
        btnXoaChuyen.setContentAreaFilled(false);
        btnXoaChuyen.setBorderPainted(false);
        btnXoaChuyen.setFocusPainted(false);
        btnXoaChuyen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXoaChuyen.addActionListener(e -> {
            int row = tableCT.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn một chuyến tàu để xóa!"); return; }
            String maChuyen = modelCT.getValueAt(row, 0).toString();
            String tenChuyen = modelCT.getValueAt(row, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa chuyến " + maChuyen + " (" + tenChuyen + ")?\nTất cả lịch trình, giá vé liên quan cũng sẽ bị xóa.",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection()) {
                    // Kiểm tra có vé đã bán cho chuyến này không
                    String sqlCheck = "SELECT COUNT(*) FROM Ve v " +
                            "INNER JOIN LichTrinh lt ON v.maLT = lt.maLT " +
                            "WHERE lt.maChuyen = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                        ps.setString(1, maChuyen);
                        java.sql.ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            warn("Không thể xóa chuyến " + maChuyen + "!\n" +
                                    "Đã có " + rs.getInt(1) + " vé được bán cho lịch trình thuộc chuyến này.");
                            return;
                        }
                    }
                    // Không có vé → xóa an toàn: GiaDetail → GiaHeader → LichTrinh → ChuyenTau
                    String sqlGD = "DELETE gd FROM GiaDetail gd " +
                            "INNER JOIN GiaHeader g ON gd.maGia = g.maGia " +
                            "INNER JOIN LichTrinh lt ON g.maLT = lt.maLT " +
                            "WHERE lt.maChuyen = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlGD)) {
                        ps.setString(1, maChuyen); ps.executeUpdate();
                    }
                    String sqlGH = "DELETE g FROM GiaHeader g " +
                            "INNER JOIN LichTrinh lt ON g.maLT = lt.maLT " +
                            "WHERE lt.maChuyen = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlGH)) {
                        ps.setString(1, maChuyen); ps.executeUpdate();
                    }
                    daoLichTrinh.deleteByMaChuyen(maChuyen);
                    if (daoChuyenTau.delete(maChuyen)) {
                        modelCT.removeRow(row);
                        modelLT.setRowCount(0);
                        resetInfo();
                        updateStats();
                        loadTuyenFilter();
                    } else {
                        warn("Không thể xóa chuyến tàu này!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    warn("Lỗi khi xóa: " + ex.getMessage());
                }
            }
        });
        btnBar.add(btnXoaChuyen);

        // (nút Tạo hàng loạt đã chuyển sang panel phải)

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

        // --- Trạng thái ---
        bar.add(filterLbl("Trạng thái:"));
        cbFilterTrangThai = new JComboBox<>(new String[]{"Tất cả", HOAT_DONG, NGUNG_HOAT_DONG});
        cbFilterTrangThai.setFont(F_SMALL);
        cbFilterTrangThai.setPreferredSize(new Dimension(140, 30));
        bar.add(cbFilterTrangThai);

        // Sep
        bar.add(filterSep());

        // --- Nút Lọc ---
        JButton btnLoc = makeSmBtn("Lọc", true);
        btnLoc.addActionListener(e -> applyFilter());
        bar.add(btnLoc);

        // --- Nút Đặt lại ---
        JButton btnReset = makeSmBtn("Đặt lại", false);
        btnReset.addActionListener(e -> resetFilter());
        bar.add(btnReset);

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
        if (cbFilterTuyen == null) return;
        cbFilterTuyen.removeAllItems();
        cbFilterTuyen.addItem("Tất cả");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (int i = 0; i < modelCT.getRowCount(); i++) {
            Object v = modelCT.getValueAt(i, 5); // cột ẩn Tuyến = index 5
            if (v != null && !v.toString().isBlank()) set.add(v.toString().trim());
        }
        set.forEach(cbFilterTuyen::addItem);
    }

    private void applyFilter() {
        if (cbFilterTuyen == null || cbFilterTrangThai == null) return;
        String fTuyen = (String) cbFilterTuyen.getSelectedItem();
        String fTT    = (String) cbFilterTrangThai.getSelectedItem();

        boolean allTuyen = fTuyen == null || "Tất cả".equals(fTuyen);
        boolean allTT    = fTT == null || "Tất cả".equals(fTT);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelCT);
        tableCT.setRowSorter(sorter);
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                boolean okTuyen = allTuyen || e.getStringValue(5).trim().equals(fTuyen); // Tuyến = col 5
                boolean okTT    = allTT    || e.getStringValue(2).trim().equals(fTT);    // Trạng thái = col 2
                return okTuyen && okTT;
            }
        });
    }

    private void resetFilter() {
        if (cbFilterTuyen == null || cbFilterTrangThai == null) return;
        cbFilterTuyen.setSelectedIndex(0);
        cbFilterTrangThai.setSelectedIndex(0);
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
        styleScrollBar(sc.getVerticalScrollBar());
        cardLT.add(sc, BorderLayout.CENTER);

        // Nút thêm / tạo hàng loạt / xóa lịch trình cho chuyến đang chọn
        JPanel btnLTBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnLTBar.setOpaque(false);
        JButton btnThemLT  = makeBtn("+ Thêm lịch trình", BtnStyle.PRIMARY);

        // Nút Tạo hàng loạt — vẽ icon grid bằng Graphics2D
        JButton btnBatchLT = new JButton("  Tạo hàng loạt") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x16A34A) : new Color(0x22C55E));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 16, cy = getHeight() / 2;
                g2.drawRoundRect(cx - 6, cy - 6, 12, 12, 2, 2);
                g2.drawLine(cx - 6, cy - 2, cx + 6, cy - 2);
                g2.drawLine(cx - 6, cy + 2, cx + 6, cy + 2);
                g2.drawLine(cx - 2, cy - 6, cx - 2, cy + 6);
                g2.drawLine(cx + 2, cy - 6, cx + 2, cy + 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBatchLT.setFont(F_LABEL);
        btnBatchLT.setForeground(Color.WHITE);
        btnBatchLT.setContentAreaFilled(false);
        btnBatchLT.setBorderPainted(false);
        btnBatchLT.setFocusPainted(false);
        btnBatchLT.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Nút Xóa — vẽ icon thùng rác bằng Graphics2D
        JButton btnXoaLT = new JButton("  Xóa") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 14, cy = getHeight() / 2;
                g2.drawLine(cx - 5, cy - 5, cx + 5, cy - 5);
                g2.drawLine(cx - 2, cy - 5, cx - 2, cy - 7);
                g2.drawLine(cx + 2, cy - 5, cx + 2, cy - 7);
                g2.drawLine(cx - 2, cy - 7, cx + 2, cy - 7);
                g2.drawLine(cx - 4, cy - 5, cx - 3, cy + 5);
                g2.drawLine(cx + 4, cy - 5, cx + 3, cy + 5);
                g2.drawLine(cx - 3, cy + 5, cx + 3, cy + 5);
                g2.drawLine(cx, cy - 3, cx, cy + 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnXoaLT.setFont(F_LABEL);
        btnXoaLT.setForeground(Color.WHITE);
        btnXoaLT.setContentAreaFilled(false);
        btnXoaLT.setBorderPainted(false);
        btnXoaLT.setFocusPainted(false);
        btnXoaLT.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnThemLT .setPreferredSize(new Dimension(155, 32));
        btnBatchLT.setPreferredSize(new Dimension(150, 32));
        btnXoaLT  .setPreferredSize(new Dimension(80,  32));
        btnLTBar.add(btnThemLT);
        btnLTBar.add(btnBatchLT);
        btnLTBar.add(btnXoaLT);
        cardLT.add(btnLTBar, BorderLayout.SOUTH);

        // Nút Tạo hàng loạt → mở dialog batch LT cho chuyến đang chọn
        btnBatchLT.addActionListener(e -> {
            int row = tableCT.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn một chuyến tàu trước!"); return; }
            String ttChuyen = modelCT.getValueAt(row, 2).toString();
            if (NGUNG_HOAT_DONG.equals(ttChuyen)) {
                warn("Chuyến tàu đang ngưng hoạt động!\nKhông thể tạo lịch trình."); return;
            }
            String maChuyen = modelCT.getValueAt(row, 0).toString();
            String maTau    = modelCT.getValueAt(row, 3).toString();
            String maTuyen  = modelCT.getValueAt(row, 4).toString();
            openBatchLTDialog(maChuyen, maTau, maTuyen);
        });

        // Nút Thêm lịch trình → mở dialog thêm LT cho chuyến đang chọn
        btnThemLT.addActionListener(e -> {
            int row = tableCT.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn một chuyến tàu trước!"); return; }
            String ttChuyen = modelCT.getValueAt(row, 2).toString();
            if (NGUNG_HOAT_DONG.equals(ttChuyen)) {
                warn("Chuyến tàu đang ngưng hoạt động!\nKhông thể thêm lịch trình."); return;
            }
            String maChuyen  = modelCT.getValueAt(row, 0).toString();
            String maTau     = modelCT.getValueAt(row, 3).toString();
            String maTuyen   = modelCT.getValueAt(row, 4).toString();
            openAddLTDialog(maChuyen, maTau, maTuyen);
        });

        // Nút Xóa lịch trình → xóa NHIỀU dòng đang chọn trong tableLT (Ctrl/Shift)
        btnXoaLT.addActionListener(e -> {
            int[] selectedRows = tableLT.getSelectedRows();
            if (selectedRows.length == 0) { warn("Vui lòng chọn ít nhất một lịch trình để xóa!"); return; }

            // Lấy danh sách mã LT để xóa
            java.util.List<String> dsXoa = new java.util.ArrayList<>();
            for (int idx : selectedRows) {
                dsXoa.add(modelLT.getValueAt(idx, 0).toString());
            }

            String msg = selectedRows.length == 1
                    ? "Xóa lịch trình " + dsXoa.get(0) + "?"
                    : "Xóa " + selectedRows.length + " lịch trình đã chọn?\n(" + String.join(", ", dsXoa) + ")";
            int confirm = JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int soXoa = 0;
                try {
                    try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection()) {
                        // Kiểm tra lịch trình nào đã có vé bán
                        java.util.List<String> coVe = new java.util.ArrayList<>();
                        for (String maLT : dsXoa) {
                            String sqlCheck = "SELECT COUNT(*) FROM Ve WHERE maLT = ?";
                            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                                ps.setString(1, maLT);
                                java.sql.ResultSet rs = ps.executeQuery();
                                if (rs.next() && rs.getInt(1) > 0) coVe.add(maLT);
                            }
                        }
                        if (!coVe.isEmpty()) {
                            warn("Không thể xóa lịch trình đã có vé bán!\n" +
                                    "Lịch trình có vé: " + String.join(", ", coVe));
                            return;
                        }
                        // Không có vé → xóa an toàn
                        for (String maLT : dsXoa) {
                            // Xóa GiaDetail
                            String sqlGD = "DELETE gd FROM GiaDetail gd INNER JOIN GiaHeader g ON gd.maGia = g.maGia WHERE g.maLT = ?";
                            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlGD)) {
                                ps.setString(1, maLT); ps.executeUpdate();
                            }
                            // Xóa GiaHeader
                            String sqlG = "DELETE FROM GiaHeader WHERE maLT = ?";
                            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlG)) {
                                ps.setString(1, maLT); ps.executeUpdate();
                            }
                            // Xóa LichTrinh
                            if (daoLichTrinh.delete(maLT)) soXoa++;
                        }
                    }
                    // Xóa từ model (từ dưới lên để không lệch index)
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        modelLT.removeRow(selectedRows[i]);
                    }
                    updateStats();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    warn("Lỗi khi xóa: " + ex.getMessage());
                }
            }
        });

        // Double-click vào tableLT → sửa lịch trình đó
        tableLT.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tableLT.getSelectedRow() >= 0) {
                    int ctRow = tableCT.getSelectedRow();
                    if (ctRow < 0) return;
                    String maTau   = modelCT.getValueAt(ctRow, 3).toString(); // cột ẩn
                    String maTuyen = modelCT.getValueAt(ctRow, 4).toString(); // cột ẩn
                    int ltRow = tableLT.getSelectedRow();
                    String maLT    = modelLT.getValueAt(ltRow, 0).toString();
                    String ngayKH  = modelLT.getValueAt(ltRow, 1).toString();
                    String gioDi   = modelLT.getValueAt(ltRow, 2).toString();
                    String ngayDen = modelLT.getValueAt(ltRow, 3).toString();
                    openEditLTDialog(ltRow, maLT, ngayKH, gioDi, ngayDen, maTau, maTuyen);
                }
            }
        });

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
        // Thu gọn — chỉ chiếm đủ khoảng cần thiết
        card.setPreferredSize(new Dimension(0, 180));
        card.setMinimumSize(new Dimension(0, 140));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel body = new JPanel(); body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        body.add(sectionLbl("Thông tin Tàu"));
        body.add(Box.createVerticalStrut(3));
        body.add(makeTblHeaderCompact(new String[]{"Mã Tàu", "Tên Tàu", "Số Toa"}));
        body.add(makeTblRowCompact(new JLabel[]{lblTauMa, lblTauTen, lblTauSoToa}));
        body.add(Box.createVerticalStrut(6));
        body.add(sectionLbl("Tuyến"));
        body.add(Box.createVerticalStrut(3));
        body.add(makeTblHeaderCompact(new String[]{"Mã Tuyến", "Ga Đi", "Ga Đến"}));
        body.add(makeTblRowCompact(new JLabel[]{lblTuyenMa, lblTuyenGaDi, lblTuyenGaDen}));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** Header nhỏ gọn cho info card */
    private JPanel makeTblHeaderCompact(String[] cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length, 0, 0));
        p.setBackground(TH_BG);
        p.setBorder(BorderFactory.createMatteBorder(1,1,0,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String col : cols) {
            JLabel l = new JLabel(col);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(TEXT_DARK);
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
            p.add(l);
        }
        return p;
    }

    /** Row nhỏ gọn cho info card */
    private JPanel makeTblRowCompact(JLabel[] labels) {
        JPanel p = new JPanel(new GridLayout(1, labels.length, 0, 0));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1,1,1,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JLabel l : labels) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));
            p.add(l);
        }
        return p;
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
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setFillsViewportHeight(true);

        // 3 cột hiển thị: Mã Chuyến, Tên Chuyến, Trạng thái
        // + 3 cột ẩn: Mã Tàu (3), Mã Tuyến (4), Tuyến (5)
        applyPaddingRenderer(t, 2); // padding cho 2 cột đầu
        // Cột Trạng thái (index 2) dùng renderer màu riêng
        t.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object v,
                                                                     boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String val = v != null ? v.toString() : "";
                if (HOAT_DONG.equals(val)) l.setForeground(new Color(0x22C55E));
                else                       l.setForeground(new Color(0xEF4444));
                if (!sel) l.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return l;
            }
        });

        // Ẩn 3 cột: Mã Tàu (3), Mã Tuyến (4), Tuyến (5) — vẫn có trong model
        // Thêm 3 cột ẩn vào model header (không hiển thị)
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
        // 6 cột: Mã LT, Ngày KH, Giờ Đi, Ngày Đến, Giờ Đến, Trạng thái
        int[] w = {60, 105, 60, 95, 60, 110};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        applyPaddingRenderer(t, 5);
        // Cột Trạng thái (index 5) dùng renderer màu
        t.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());
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
        h.setResizingAllowed(false);
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
    /**
     * Tính trạng thái chuyến tàu dựa trên ngayDen đã tính sẵn.
     * ngayDen có định dạng "dd/MM/yyyy HH:mm" — được tính = ngayDi + thoiGianChay (phút)
     */
    private String tinhTrangThai(String ngayKH, String gioDi, String ngayDen) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
            Date batDau  = sdf.parse(ngayKH + " " + gioDi);
            Date ketThuc = sdf.parse(ngayDen); // ngayDen đã là "dd/MM/yyyy HH:mm"
            Date now     = new Date();
            if (now.before(batDau))   return TT_CHUA;
            if (now.after(ketThuc))   return TT_HOAN;
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

        String maChuyen = modelCT.getValueAt(row, 0).toString();

        // Load TẤT CẢ lịch trình của chuyến này từ DB
        List<LichTrinhRow> dsLT = daoLichTrinh.getByMaChuyen(maChuyen);
        // Sắp xếp theo ngày + giờ khởi hành
        dsLT.sort((a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
                Date da = sdf.parse(a.ngayKhoiHanh + " " + a.gioKhoiHanh);
                Date db = sdf.parse(b.ngayKhoiHanh + " " + b.gioKhoiHanh);
                return da.compareTo(db);
            } catch (Exception e) { return 0; }
        });
        for (LichTrinhRow lt : dsLT) {
            String ngayDenFull = lt.ngayDen != null && !lt.ngayDen.isEmpty()
                    ? lt.ngayDen
                    : tinhNgayDen(lt.ngayKhoiHanh, lt.gioKhoiHanh, 1440);
            // Ngày đến: chỉ dd/MM/yyyy
            String ngayDenHienThi = ngayDenFull.length() >= 10 ? ngayDenFull.substring(0, 10) : ngayDenFull;
            // Giờ đến: lấy phần HH:mm từ ngayDenFull (format "dd/MM/yyyy HH:mm")
            String gioDenHienThi = ngayDenFull.length() >= 16 ? ngayDenFull.substring(11, 16) : "--:--";
            // Giờ đi: định dạng HH:mm
            String gioDiHienThi = lt.gioKhoiHanh;
            if (gioDiHienThi != null && gioDiHienThi.length() > 5) {
                gioDiHienThi = gioDiHienThi.substring(0, 5);
            }
            // Tính trạng thái realtime
            String ttRealtime = tinhTrangThai(lt.ngayKhoiHanh, lt.gioKhoiHanh, ngayDenFull);
            modelLT.addRow(new Object[]{
                    lt.maLT, lt.ngayKhoiHanh, gioDiHienThi, ngayDenHienThi, gioDenHienThi, ttRealtime
            });
        }

        // Cập nhật lại trạng thái chuyến dựa trên tất cả LT
        String ttHienThi = tinhTrangThaiChuyen(maChuyen, dsLT);
        lblTrangThai.setText(ttHienThi);
        if (TT_DANG.equals(ttHienThi))      lblTrangThai.setForeground(new Color(0x22C55E));
        else if (TT_CHUA.equals(ttHienThi)) lblTrangThai.setForeground(new Color(0xF59E0B));
        else if (TT_HUY.equals(ttHienThi))  lblTrangThai.setForeground(new Color(0xEF4444));
        else                                 lblTrangThai.setForeground(new Color(0x6B7280));

        // Info Tàu — lấy thông tin thật từ DB
        String maTauVal = modelCT.getValueAt(row, 3).toString(); // cột ẩn index 3
        String tenTauVal = getTenTauFromDB(maTauVal);
        String soToaVal  = getSoToaFromDB(maTauVal);
        lblTauMa.setText(maTauVal);
        lblTauTen.setText(tenTauVal);
        lblTauSoToa.setText(soToaVal);

        // Info Tuyến — lấy Ga Đi / Ga Đến thật từ DB
        String maTuyenVal = modelCT.getValueAt(row, 4).toString(); // cột ẩn index 4
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

    /**
     * Kiểm tra tàu có đang hoạt động không (HOATDONG).
     * @return true nếu tàu đang hoạt động, false nếu ngưng/bảo trì
     */
    private boolean getTrangThaiTauFromDB(String maTau) {
        try {
            List<Tau> ds = daoTau.getAllTau();
            for (Tau t : ds) {
                if (t.getMaTau().equalsIgnoreCase(maTau.trim()))
                    return "HOATDONG".equals(t.getTrangThaiTau().name());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return true; // mặc định hoạt động nếu không tìm thấy
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
            // Tên chuyến = tên tàu (lấy từ DB), không kèm tuyến
            String tenTauDB = getTenTauFromDB(maTauStr);
            if (!maTauStr.isEmpty()) {
                txtTenChuyen.setText(tenTauDB.isEmpty() ? maTauStr : tenTauDB);
            } else {
                txtTenChuyen.setText("(tự sinh theo mã tàu)");
            }
        };
        // Listener sẽ được gắn trong phần form bên dưới (kèm updateHint)

        // cbSoNgay đã bỏ — thay bằng thoiGianChay lấy từ Tuyen DB
        DatePickerField   dpNgay  = new DatePickerField("");
        JComboBox<String> cbGioDi = makeComboGio("06:00");

        // Label hiển thị ngày đến dự kiến (tự tính khi chọn tuyến/ngày/giờ)
        JLabel lblNgayDen = new JLabel("-- Chọn tuyến, ngày và giờ --");
        lblNgayDen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgayDen.setForeground(new Color(0x22C55E));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();

        int r = 0;
        addSep(form, gc, r++, "Thông tin Chuyến tàu", ACCENT);
        addRow(form, gc, r++, "Mã tàu *:",    cbMaTau);
        addRow(form, gc, r++, "Tuyến *:",      cbTuyen);
        addRow(form, gc, r++, "Ngày đến:", lblNgayDen);

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
        // Cập nhật ngàyĐến khi tuyến/ngày/giờ thay đổi
        Runnable updateNgayDen = () -> {
            try {
                String tenTuyenHt = cbTuyen.getSelectedItem() != null ? cbTuyen.getSelectedItem().toString() : "";
                String maTuyenHt  = getMaTuyenFromTen(tenTuyenHt);
                int thoiGian = getThoiGianChayFromTuyen(maTuyenHt);
                String ngay  = dpNgay.getDate().trim();
                String gio   = cbGioDi.getSelectedItem() != null ? cbGioDi.getSelectedItem().toString() : "06:00";
                if (!ngay.isEmpty()) {
                    String nd = tinhNgayDen(ngay, gio, thoiGian);
                    String ndNgay = nd.length() >= 10 ? nd.substring(0, 10) : nd;
                    lblNgayDen.setText(ndNgay + "  (" + (thoiGian/60) + "h" + (thoiGian%60 > 0 ? thoiGian%60 + "m" : "") + ")");
                }
            } catch (Exception ignored) {}
        };
        cbMaTau.addActionListener(e2 -> { updateTenChuyen.run(); updateHint.run(); });
        cbTuyen.addActionListener(e2 -> { updateTenChuyen.run(); updateHint.run(); updateNgayDen.run(); });
        cbGioDi.addActionListener(e2 -> updateNgayDen.run());
        dpNgay.addPropertyChangeListener("date", e2 -> updateNgayDen.run());

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

            String gioDiVal = (String) cbGioDi.getSelectedItem();

            // Lấy thoiGianChay từ DB theo tuyến đã chọn
            int thoiGianPhut = getThoiGianChayFromTuyen(maTuyenThuc);
            // Tính ngày đến = ngày đi + thoiGianChay phút
            String ngayDenVal = tinhNgayDen(ngayKH, gioDiVal, thoiGianPhut);

            // --- KIỂM TRA TRÙNG LỊCH (dùng thoiGianPhut thay soNgay) ---
            String trungTenAdd = kiemTraTrungLich(maTau, ngayKH, gioDiVal, thoiGianPhut, null);
            if (trungTenAdd != null) {
                warn("Tàu " + maTau + " đang có chuyến \"" + trungTenAdd + "\" chưa kết thúc.\n" +
                        "Vui lòng chọn thời gian sau khi chuyến đó hoàn thành.");
                return;
            }

            String tenChuyen = txtTenChuyen.getText().trim();
            if (tenChuyen.startsWith("(") || tenChuyen.isEmpty()) {
                // Tên chuyến = tên tàu (không kèm tuyến)
                String tenTauFallback = getTenTauFromDB(maTau);
                tenChuyen = tenTauFallback.isEmpty() ? maTau : tenTauFallback;
            }

            String maChuyen = nextMaChuyen();
            String maLT     = nextMaLT();
            String tt = tinhTrangThai(ngayKH, gioDiVal, ngayDenVal);

            // 1. INSERT vào ChuyenTau
            boolean okChuyen = daoChuyenTau.insert(maChuyen, tenChuyen, maTau, maTuyenThuc);

            // 2. INSERT vào LichTrinh (lịch trình đầu tiên)
            if (okChuyen) {
                daoLichTrinh.insert(maLT, ngayKH, gioDiVal, ngayDenVal, maChuyen);
            }

            if (okChuyen) {
                // 6 cột: Mã Chuyến, Tên Chuyến, Trạng thái, Mã Tàu(ẩn), Mã Tuyến(ẩn), Tuyến(ẩn)
                String ttHD = getTrangThaiTauFromDB(maTau) ? HOAT_DONG : NGUNG_HOAT_DONG;
                modelCT.addRow(new Object[]{
                        maChuyen, tenChuyen, ttHD, maTau, maTuyenThuc, tenTuyenChon
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
        String maTau     = modelCT.getValueAt(row, 3).toString(); // cột ẩn
        String tenChuyen = modelCT.getValueAt(row, 1).toString();
        String maTuyen   = modelCT.getValueAt(row, 4).toString(); // cột ẩn
        String tuyen     = modelCT.getValueAt(row, 5).toString(); // cột ẩn

        // Lấy thông tin LT từ DB (không còn hidden cols trong modelCT)
        List<LichTrinhRow> dsLTUpd = daoLichTrinh.getByMaChuyen(maChuyen);
        String maLT     = "-", ngayKH = "", gioDiVal = "06:00", ngayDenVal = "";
        if (!dsLTUpd.isEmpty()) {
            LichTrinhRow ltFirst = dsLTUpd.get(0);
            maLT      = ltFirst.maLT;
            ngayKH    = ltFirst.ngayKhoiHanh;
            gioDiVal  = ltFirst.gioKhoiHanh;
            ngayDenVal = ltFirst.ngayDen != null ? ltFirst.ngayDen : "";
        }

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
            if (!maTauStr.isEmpty()) {
                String tenTauDB = getTenTauFromDB(maTauStr);
                txtTenChuyen.setText(tenTauDB.isEmpty() ? maTauStr : tenTauDB);
            }
        };

        DatePickerField   dpNgay = new DatePickerField(ngayKH);
        JComboBox<String> cbGioDi = makeComboGio(gioDiVal);

        // Label ngày đến dự kiến (tự tính từ tuyến + ngày + giờ)
        JLabel lblNgayDenUpd = new JLabel(ngayDenVal.isEmpty() ? "-- --" : ngayDenVal);
        lblNgayDenUpd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgayDenUpd.setForeground(new Color(0x22C55E));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();

        int r = 0;
        addSep(form, gc, r++, "Thông tin Chuyến tàu", ACCENT);
        addRow(form, gc, r++, "Mã tàu *:",    txtMaTau);
        addRow(form, gc, r++, "Tuyến *:",      cbTuyen);
        addRow(form, gc, r++, "Ngày đến:", lblNgayDenUpd);

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
        // Cập nhật ngàyĐến khi tuyến/ngày/giờ thay đổi
        Runnable updateNgayDenUpd = () -> {
            try {
                String tenTuyenHt = cbTuyen.getSelectedItem() != null ? cbTuyen.getSelectedItem().toString() : "";
                String maTuyenHt  = getMaTuyenFromTen(tenTuyenHt);
                int thoiGian = getThoiGianChayFromTuyen(maTuyenHt);
                String ngay  = dpNgay.getDate().trim();
                String gio   = cbGioDi.getSelectedItem() != null ? cbGioDi.getSelectedItem().toString() : "06:00";
                if (!ngay.isEmpty()) {
                    String nd = tinhNgayDen(ngay, gio, thoiGian);
                    String ndNgay = nd.length() >= 10 ? nd.substring(0, 10) : nd;
                    lblNgayDenUpd.setText(ndNgay + "  (" + (thoiGian/60) + "h" + (thoiGian%60 > 0 ? thoiGian%60 + "m" : "") + ")");
                }
            } catch (Exception ignored) {}
        };
        cbTuyen.addActionListener(e2 -> { updateTenChuyenUpd.run(); updateHintUpd.run(); updateNgayDenUpd.run(); });
        cbGioDi.addActionListener(e2 -> updateNgayDenUpd.run());
        dpNgay.addPropertyChangeListener("date", e2 -> updateNgayDenUpd.run());

        addSep(form, gc, r++, "Lịch trình", new Color(0x27AE60));
        addRow(form, gc, r++, "Ngày KH *:",   dpNgay);
        addRow(form, gc, r,   "Giờ đi *:",    cbGioDi);

        final int selRow = row;
        final String maLTFinal = maLT;
        final String maChuyenFinal = maChuyen;
        JButton btnCapNhat = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnCapNhat.addActionListener(e -> {
            String newGioDi  = (String) cbGioDi.getSelectedItem();
            // Tính ngayDen từ tuyến mới được chọn
            String newTuyenChon  = (String) cbTuyen.getSelectedItem();
            String newMaTuyenTmp = getMaTuyenFromTen(newTuyenChon);
            int thoiGianPhutUpd  = getThoiGianChayFromTuyen(newMaTuyenTmp);
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
            String trungTenUpd = kiemTraTrungLich(newMaTau, newNgayKH, newGioDi, thoiGianPhutUpd, maChuyenFinal);
            if (trungTenUpd != null) {
                warn("Tàu " + newMaTau + " đang có chuyến \"" + trungTenUpd + "\" chưa kết thúc.\n" +
                        "Vui lòng chọn thời gian sau khi chuyến đó hoàn thành.");
                return;
            }

            String newNgayDen = tinhNgayDen(newNgayKH, newGioDi, thoiGianPhutUpd);

            // 1. UPDATE ChuyenTau trong DB
            boolean okChuyen = daoChuyenTau.update(
                    maChuyenFinal, newTenChuyen, newMaTau,
                    modelCT.getValueAt(selRow, 4).toString()); // maTuyen cột ẩn

            // 2. UPDATE LichTrinh trong DB
            daoLichTrinh.update(maLTFinal, newNgayKH, newGioDi, newNgayDen, maChuyenFinal);

            if (okChuyen) {
                modelCT.setValueAt(newMaTau,     selRow, 3); // cột ẩn
                modelCT.setValueAt(newTenChuyen, selRow, 1);
                modelCT.setValueAt(newTuyen,     selRow, 5); // cột ẩn
                // LT không còn lưu trong modelCT — refresh từ DB
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

            // Đồng bộ bộ đếm tránh trùng mã
            cntChuyen = calcNextCnt(dsChuyen.stream()
                    .map(c -> c.maChuyen).toArray(String[]::new), "CT", 2);
            cntLT = calcNextCnt(dsLT.stream()
                    .map(l -> l.maLT).toArray(String[]::new), "LT", 2);

            for (ChuyenTauRow ct : dsChuyen) {
                String tenTuyen = getTenTuyenFromDB(ct.maTuyen);
                // Trạng thái hoạt động: dựa trên trạng thái tàu từ DB
                String ttHoatDong = getTrangThaiTauFromDB(ct.maTau) ? HOAT_DONG : NGUNG_HOAT_DONG;

                // 6 cột: Mã Chuyến, Tên Chuyến, Trạng thái, Mã Tàu(ẩn), Mã Tuyến(ẩn), Tuyến(ẩn)
                modelCT.addRow(new Object[]{
                        ct.maChuyen, ct.tenChuyen, ttHoatDong,
                        ct.maTau, ct.maTuyen, tenTuyen
                });
            }
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu từ database: " + e.getMessage());
        }
    }

    /**
     * Tính trạng thái chuyến tàu dựa trên TẤT CẢ lịch trình của nó.
     * - Nếu có ít nhất 1 LT đang chạy → "Đang Khởi Hành"
     * - Nếu có ít nhất 1 LT chưa chạy → "Chưa Khởi Hành"
     * - Nếu tất cả LT đã hoàn thành → "Đã Hoàn Thành"
     * - Nếu không có LT nào → "Chưa Khởi Hành"
     */

    /**
     * Lấy trạng thái chuyến từ DB (dùng thay modelCT col 5 đã bỏ).
     */
    private String getTrangThaiChuyenFromDB(String maChuyen) {
        List<LichTrinhRow> dsLT = daoLichTrinh.getByMaChuyen(maChuyen);
        return tinhTrangThaiChuyen(maChuyen, dsLT);
    }

    private String tinhTrangThaiChuyen(String maChuyen, List<LichTrinhRow> dsLT) {
        boolean coDang = false, coChua = false;
        for (LichTrinhRow lt : dsLT) {
            if (!lt.maChuyen.equals(maChuyen)) continue;
            String nd = lt.ngayDen != null && !lt.ngayDen.isEmpty()
                    ? lt.ngayDen
                    : tinhNgayDen(lt.ngayKhoiHanh, lt.gioKhoiHanh,
                    getThoiGianChayFromTuyen(null));
            String tt = tinhTrangThai(lt.ngayKhoiHanh, lt.gioKhoiHanh, nd);
            if (TT_DANG.equals(tt)) coDang = true;
            if (TT_CHUA.equals(tt)) coChua = true;
        }
        if (coDang) return TT_DANG;
        if (coChua) return TT_CHUA;
        // Không có LT nào hoặc tất cả đã hoàn thành
        boolean coLT = dsLT.stream().anyMatch(lt -> lt.maChuyen.equals(maChuyen));
        return coLT ? TT_HOAN : TT_CHUA;
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

    // =========================================================================
    // DIALOG THÊM LỊCH TRÌNH VÀO CHUYẾN TÀU ĐÃ CÓ
    // Cho phép thêm nhiều lịch trình vào 1 chuyến tàu.
    // Validate: tàu không được chồng lịch với các LT hiện có.
    // =========================================================================
    private void openAddLTDialog(String maChuyen, String maTau, String maTuyen) {
        JDialog dlg = makeDialog("Thêm lịch trình – " + maChuyen);

        DatePickerField   dpNgay = new DatePickerField("");
        JComboBox<String> cbGio  = makeComboGio("06:00");

        // Lấy thoiGianChay từ tuyến để tính ngayDen preview
        int thoiGianPhut = getThoiGianChayFromTuyen(maTuyen);

        JLabel lblNgayDen = new JLabel("-- Chọn ngày và giờ --");
        lblNgayDen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgayDen.setForeground(new Color(0x22C55E));

        Runnable updateND = () -> {
            String ngay = dpNgay.getDate().trim();
            String gio  = cbGio.getSelectedItem() != null ? cbGio.getSelectedItem().toString() : "06:00";
            if (!ngay.isEmpty()) {
                String nd = tinhNgayDen(ngay, gio, thoiGianPhut);
                // Chỉ hiện ngày (dd/MM/yyyy)
                String ndNgay = nd.length() >= 10 ? nd.substring(0, 10) : nd;
                lblNgayDen.setText(ndNgay + "  (" + (thoiGianPhut/60) + "h" +
                        (thoiGianPhut%60>0 ? thoiGianPhut%60+"m" : "") + ")");
            }
        };
        dpNgay.addPropertyChangeListener("date", e -> updateND.run());
        cbGio.addActionListener(e -> updateND.run());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addSep(form, gc, r++, "Thêm lịch trình vào " + maChuyen, ACCENT);
        addRow(form, gc, r++, "Ngày khởi hành *:", dpNgay);
        addRow(form, gc, r++, "Giờ đi *:",          cbGio);
        addRow(form, gc, r,   "Ngày đến:", lblNgayDen);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String ngayKH = dpNgay.getDate().trim();
            String gioDi  = (String) cbGio.getSelectedItem();

            if (ngayKH.isEmpty()) { warn("Vui lòng chọn ngày khởi hành!"); return; }
            try { new java.text.SimpleDateFormat(DATE_FMT).parse(ngayKH); }
            catch (Exception ex) { warn("Ngày không hợp lệ!"); return; }

            // Kiểm tra trùng lịch — truyền excludeMaLT=null (thêm mới)
            String trung = kiemTraTrungLich(maTau, ngayKH, gioDi, thoiGianPhut, null, null);
            if (trung != null) {
                warn("Tàu " + maTau + " đang có chuyến \"" + trung + "\" trùng thời gian!");
                return;
            }

            String maLT    = nextMaLT();
            String ngayDen = tinhNgayDen(ngayKH, gioDi, thoiGianPhut);

            if (daoLichTrinh.insert(maLT, ngayKH, gioDi, ngayDen, maChuyen)) {
                String ttNewLT = tinhTrangThai(ngayKH, gioDi, ngayDen);
                String ndHienThi = ngayDen.length() >= 10 ? ngayDen.substring(0, 10) : ngayDen;
                String gdHienThi = ngayDen.length() >= 16 ? ngayDen.substring(11, 16) : "--:--";
                modelLT.addRow(new Object[]{maLT, ngayKH, gioDi, ndHienThi, gdHienThi, ttNewLT});
                // Cập nhật trạng thái chuyến
                int ctRow = tableCT.getSelectedRow();
                if (ctRow >= 0) {
                    List<LichTrinhRow> dsLT = daoLichTrinh.getByMaChuyen(maChuyen);
                }
                updateStats();
                dlg.dispose();
            } else {
                warn("Lỗi khi lưu vào database!");
            }
        });

        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // DIALOG SỬA LỊCH TRÌNH (double-click trong tableLT)
    // =========================================================================
    // =========================================================================
    // DIALOG SỬA LỊCH TRÌNH (double-click trong tableLT)
    // =========================================================================
    private void openEditLTDialog(int ltRow, String maLT, String ngayKH,
                                  String gioDi, String ngayDen,
                                  String maTau, String maTuyen) {
        JDialog dlg = makeDialog("Cập nhật lịch trình – " + maLT);

        int thoiGianPhut = getThoiGianChayFromTuyen(maTuyen);

        DatePickerField   dpNgay = new DatePickerField(ngayKH);
        JComboBox<String> cbGio  = makeComboGio(gioDi);

        // Hiển thị ngày đến chỉ ngày (dd/MM/yyyy)
        String ngayDenInit = ngayDen != null && !ngayDen.isEmpty()
                ? (ngayDen.length() >= 10 ? ngayDen.substring(0, 10) : ngayDen)
                : "-- --";
        JLabel lblNgayDen = new JLabel(ngayDenInit);
        lblNgayDen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgayDen.setForeground(new Color(0x22C55E));

        Runnable updateND = () -> {
            String ngay = dpNgay.getDate().trim();
            String gio  = cbGio.getSelectedItem() != null ? cbGio.getSelectedItem().toString() : "06:00";
            if (!ngay.isEmpty()) {
                String nd = tinhNgayDen(ngay, gio, thoiGianPhut);
                lblNgayDen.setText(nd.length() >= 10 ? nd.substring(0, 10) : nd);
            }
        };
        dpNgay.addPropertyChangeListener("date", e -> updateND.run());
        cbGio.addActionListener(e -> updateND.run());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addSep(form, gc, r++, "Cập nhật lịch trình " + maLT, ACCENT);
        addRow(form, gc, r++, "Ngày khởi hành *:", dpNgay);
        addRow(form, gc, r++, "Giờ đi *:",          cbGio);
        addRow(form, gc, r,   "Ngày đến:", lblNgayDen);

        JButton btnCapNhat = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnCapNhat.addActionListener(e -> {
            String newNgay = dpNgay.getDate().trim();
            String newGio  = (String) cbGio.getSelectedItem();

            if (newNgay.isEmpty()) { warn("Vui lòng chọn ngày khởi hành!"); return; }

            // Kiểm tra trùng lịch — loại trừ chính LT này (excludeMaLT = maLT)
            String trung = kiemTraTrungLich(maTau, newNgay, newGio, thoiGianPhut, null, maLT);
            if (trung != null) {
                warn("Tàu " + maTau + " đang có chuyến \"" + trung + "\" trùng thời gian!");
                return;
            }

            String newNgayDen = tinhNgayDen(newNgay, newGio, thoiGianPhut);

            int ctRow = tableCT.getSelectedRow();
            String maChuyen = ctRow >= 0 ? modelCT.getValueAt(ctRow, 0).toString() : "";

            if (daoLichTrinh.update(maLT, newNgay, newGio, newNgayDen, maChuyen)) {
                String ndHienThi = newNgayDen.length() >= 10 ? newNgayDen.substring(0, 10) : newNgayDen;
                String ndHienThi2 = newNgayDen.length() >= 10 ? newNgayDen.substring(0, 10) : newNgayDen;
                String gdHienThi2 = newNgayDen.length() >= 16 ? newNgayDen.substring(11, 16) : "--:--";
                modelLT.setValueAt(newNgay,     ltRow, 1);
                modelLT.setValueAt(newGio,      ltRow, 2);
                modelLT.setValueAt(ndHienThi2,  ltRow, 3);
                modelLT.setValueAt(gdHienThi2,  ltRow, 4);
                modelLT.setValueAt(tinhTrangThai(newNgay, newGio, newNgayDen), ltRow, 5);
                // Cập nhật trạng thái chuyến
                if (ctRow >= 0) {
                    java.util.List<com.dao.DAO_LichTrinh.LichTrinhRow> dsLT =
                            daoLichTrinh.getByMaChuyen(maChuyen);
                }
                dlg.dispose();
            } else {
                warn("Lỗi khi cập nhật database!");
            }
        });

        showDlg(dlg, form, btnCapNhat);
    }


    // =========================================================================
    // DIALOG TẠO LỊCH TRÌNH HÀNG LOẠT CHO MỘT CHUYẾN TÀU
    //
    // Người dùng chọn:
    //   - Các thứ trong tuần (2-4-6...)
    //   - Khoảng thời gian (từ ngày → đến ngày)
    //   - Giờ khởi hành
    //
    // Hệ thống tự sinh tất cả lịch trình phù hợp.
    // Validate: không được trùng với lịch trình đã có của cùng tàu.
    // Nếu trùng → hỏi người dùng: Bỏ qua / Bỏ qua tất cả / Dừng lại.
    //
    // Định dạng giờ hỗ trợ: HH:mm hoặc HH:mm:ss
    // =========================================================================
    private void openBatchLTDialog(String maChuyen, String maTau, String maTuyen) {
        JDialog dlg = makeDialog("Tạo hàng loạt – " + maChuyen);

        // Thời gian chạy tuyến (phút) — để kiểm tra trùng lịch ngầm
        int thoiGianPhut = getThoiGianChayFromTuyen(maTuyen);

        // ── Chọn các thứ trong tuần ──────────────────────────────────────────
        // Quy ước VN: Thứ 2 = Monday, Thứ 3 = Tuesday ... CN = Sunday
        String[] labelThu = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
        int[]    calThu   = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
        JCheckBox[] cbThu = new JCheckBox[7];

        JPanel pnlThu = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlThu.setOpaque(false);
        for (int i = 0; i < 7; i++) {
            cbThu[i] = new JCheckBox(labelThu[i]);
            cbThu[i].setFont(F_CELL);
            cbThu[i].setOpaque(false);
            // Mặc định chọn Thứ 2, Thứ 4, Thứ 6
            if (i == 0 || i == 2 || i == 4) cbThu[i].setSelected(true);
            pnlThu.add(cbThu[i]);
        }

        // ── Giờ khởi hành — hỗ trợ HH:mm và HH:mm:ss ───────────────────────
        JComboBox<String> cbGioDi = makeComboGio("06:00");

        // ── Khoảng ngày ──────────────────────────────────────────────────────
        DatePickerField dpTuNgay  = new DatePickerField("");
        DatePickerField dpDenNgay = new DatePickerField("");

        // Label preview số lịch trình sẽ tạo
        JLabel lblPreview = new JLabel("Chọn thứ và khoảng ngày để xem trước");
        lblPreview.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblPreview.setForeground(new Color(0x22C55E));

        // Hàm tính preview
        Runnable updatePreview = () -> {
            try {
                String tuNgay  = dpTuNgay.getDate().trim();
                String denNgay = dpDenNgay.getDate().trim();
                if (tuNgay.isEmpty() || denNgay.isEmpty()) return;
                java.util.Date dStart = new SimpleDateFormat(DATE_FMT).parse(tuNgay);
                java.util.Date dEnd   = new SimpleDateFormat(DATE_FMT).parse(denNgay);
                if (dEnd.before(dStart)) {
                    lblPreview.setText("Ngày kết thúc phải sau ngày bắt đầu!");
                    lblPreview.setForeground(Color.RED);
                    return;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(dStart);
                int count = 0;
                while (!cal.getTime().after(dEnd)) {
                    int dow = cal.get(Calendar.DAY_OF_WEEK);
                    for (int i = 0; i < 7; i++) {
                        if (cbThu[i].isSelected() && calThu[i] == dow) { count++; break; }
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                lblPreview.setText("Dự kiến tạo " + count + " lịch trình (chưa trừ ngày trùng)");
                lblPreview.setForeground(new Color(0x22C55E));
            } catch (Exception ignored) {}
        };

        dpTuNgay.addPropertyChangeListener("date", e -> updatePreview.run());
        dpDenNgay.addPropertyChangeListener("date", e -> updatePreview.run());
        for (JCheckBox cb : cbThu) cb.addActionListener(e -> updatePreview.run());

        // ── Build form ────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();

        int r = 0;
        addSep(form, gc, r++, "Lịch trình hàng loạt cho " + maChuyen, ACCENT);

        // Row checkbox thứ
        JLabel lblThuLabel = new JLabel("Các thứ *:");
        lblThuLabel.setFont(F_LABEL); lblThuLabel.setForeground(TEXT_DARK);
        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 1; gc.weightx = 0;
        gc.insets = new Insets(5, 6, 5, 6);
        form.add(lblThuLabel, gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(pnlThu, gc);
        r++;

        addRow(form, gc, r++, "Giờ khởi hành *:", cbGioDi);
        addRow(form, gc, r++, "Từ ngày *:",        dpTuNgay);
        addRow(form, gc, r++, "Đến ngày *:",       dpDenNgay);

        // Row preview
        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2; gc.weightx = 1;
        gc.insets = new Insets(4, 6, 8, 6);
        form.add(lblPreview, gc);

        // ── Nút Tạo ──────────────────────────────────────────────────────────
        JButton btnTao = makeBtn("Tạo hàng loạt", BtnStyle.PRIMARY);
        btnTao.addActionListener(e -> {
            // Validate thứ
            boolean coThu = false;
            for (JCheckBox cb : cbThu) if (cb.isSelected()) { coThu = true; break; }
            if (!coThu) { warn("Vui lòng chọn ít nhất một thứ trong tuần!"); return; }

            // Validate ngày
            String tuNgayStr  = dpTuNgay.getDate().trim();
            String denNgayStr = dpDenNgay.getDate().trim();
            if (tuNgayStr.isEmpty())  { warn("Vui lòng chọn ngày bắt đầu!"); return; }
            if (denNgayStr.isEmpty()) { warn("Vui lòng chọn ngày kết thúc!"); return; }

            java.util.Date dStart, dEnd;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT);
                dStart = sdf.parse(tuNgayStr);
                dEnd   = sdf.parse(denNgayStr);
            } catch (Exception ex) { warn("Ngày không hợp lệ! Định dạng: dd/MM/yyyy"); return; }
            if (dEnd.before(dStart)) { warn("Ngày kết thúc phải sau ngày bắt đầu!"); return; }

            // Validate và chuẩn hóa giờ (hỗ trợ HH:mm và HH:mm:ss)
            String gioDiRaw = cbGioDi.getSelectedItem() != null
                    ? cbGioDi.getSelectedItem().toString().trim() : "06:00";
            String gioDi = chuanHoaGio(gioDiRaw);
            if (gioDi == null) { warn("Giờ khởi hành không hợp lệ!\nĐịnh dạng: HH:mm hoặc HH:mm:ss"); return; }

            // ── Duyệt từng ngày ───────────────────────────────────────────────
            Calendar cal = Calendar.getInstance();
            cal.setTime(dStart);

            int soThanhCong = 0, soTrung = 0;
            boolean dungLai = false, skipTatCa = false;
            java.util.List<String> danhSachNgay = new java.util.ArrayList<>();

            while (!cal.getTime().after(dEnd) && !dungLai) {
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                boolean phaiTao = false;
                for (int i = 0; i < 7; i++) {
                    if (cbThu[i].isSelected() && calThu[i] == dow) { phaiTao = true; break; }
                }

                if (phaiTao) {
                    String ngayNay = new SimpleDateFormat(DATE_FMT).format(cal.getTime());

                    // Kiểm tra trùng với LT khác trong cùng tàu
                    String tenTrung = kiemTraTrungLich(maTau, ngayNay, gioDi, thoiGianPhut, null, null);

                    // Kiểm tra trùng với LT khác trong cùng chuyến (nội bộ)
                    if (tenTrung == null) {
                        tenTrung = kiemTrungNoiBoLT(maChuyen, ngayNay, gioDi, danhSachNgay);
                    }

                    if (tenTrung != null) {
                        if (skipTatCa) {
                            soTrung++;
                        } else {
                            String thongBao =
                                    "<html><b>&#9888; Phát hiện trùng lịch!</b><br><br>" +
                                            "Ngày: <b>" + ngayNay + "</b>&nbsp;&nbsp;Giờ: <b>" + gioDi + "</b><br>" +
                                            "Lý do: " + tenTrung + "<br><br>" +
                                            "Bạn muốn làm gì?</html>";
                            Object[] opts = {"Bỏ qua ngày này", "Bỏ qua tất cả", "Dừng lại"};
                            int choice = JOptionPane.showOptionDialog(dlg, thongBao,
                                    "Trùng lịch – " + ngayNay,
                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);
                            if (choice == 0)      soTrung++;
                            else if (choice == 1) { soTrung++; skipTatCa = true; }
                            else                  { dungLai = true; }
                        }
                    } else {
                        // Tạo lịch trình mới
                        String maLT    = nextMaLT();
                        String ngayDen = tinhNgayDen(ngayNay, gioDi, thoiGianPhut);
                        String tt      = tinhTrangThai(ngayNay, gioDi, ngayDen);

                        if (daoLichTrinh.insert(maLT, ngayNay, gioDi, ngayDen, maChuyen)) {
                            danhSachNgay.add(ngayNay + " " + gioDi);
                            soThanhCong++;
                        } else {
                            cntLT--;
                        }
                    }
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Refresh UI
            refreshDetail();
            int ctRow = tableCT.getSelectedRow();
            if (ctRow >= 0) {
                java.util.List<LichTrinhRow> dsLT = daoLichTrinh.getByMaChuyen(maChuyen);
            }
            updateStats();
            dlg.dispose();

            // Thông báo kết quả
            String msg = soThanhCong > 0
                    ? "Tạo thành công " + soThanhCong + " lịch trình!" + (soTrung > 0 ? "\n(Bỏ qua " + soTrung + " ngày trùng)" : "")
                    : "Không có lịch trình nào được tạo!";

            JOptionPane.showMessageDialog(this, msg,
                    soThanhCong > 0 ? "Thành công" : "Không tạo được",
                    soThanhCong > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });

        showDlg(dlg, form, btnTao);
    }

    /**
     * Chuẩn hóa giờ nhập vào: hỗ trợ HH:mm và HH:mm:ss
     * Trả về HH:mm hoặc null nếu không hợp lệ.
     */
    /**
     * Chuẩn hóa chuỗi giờ về định dạng HH:mm.
     * Chấp nhận: HH:mm hoặc HH:mm:ss
     * @return "HH:mm" nếu hợp lệ, null nếu sai định dạng
     */
    private String chuanHoaGio(String gio) {
        if (gio == null || gio.trim().isEmpty()) return null;
        gio = gio.trim();
        try {
            // HH:mm:ss → lấy phần HH:mm
            if (gio.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                new SimpleDateFormat("HH:mm:ss").parse(gio);
                return String.format("%02d:%02d",
                        Integer.parseInt(gio.split(":")[0]),
                        Integer.parseInt(gio.split(":")[1]));
            }
            // HH:mm
            if (gio.matches("\\d{1,2}:\\d{2}")) {
                new SimpleDateFormat("HH:mm").parse(gio);
                return String.format("%02d:%02d",
                        Integer.parseInt(gio.split(":")[0]),
                        Integer.parseInt(gio.split(":")[1]));
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Kiểm tra trùng lịch nội bộ trong cùng một chuyến tàu.
     * Dùng khi tạo hàng loạt để tránh tạo 2 LT cùng ngày giờ trong 1 chuyến.
     *
     * @param maChuyen     mã chuyến cần kiểm tra
     * @param ngayMoi      ngày khởi hành mới (dd/MM/yyyy)
     * @param gioMoi       giờ đi mới (HH:mm)
     * @param danhSachDaTao danh sách "ngayKH gioDi" đã tạo trong phiên này
     * @return mô tả xung đột, null nếu không trùng
     */
    private String kiemTrungNoiBoLT(String maChuyen, String ngayMoi, String gioMoi,
                                    java.util.List<String> danhSachDaTao) {
        // Kiểm tra trùng với LT đã có trong DB
        java.util.List<LichTrinhRow> existing = daoLichTrinh.getByMaChuyen(maChuyen);
        for (LichTrinhRow lt : existing) {
            if (lt.ngayKhoiHanh.equals(ngayMoi) && lt.gioKhoiHanh.equals(gioMoi)) {
                return "Chuyến " + maChuyen + " đã có LT ngày " + ngayMoi + " lúc " + gioMoi;
            }
        }
        // Kiểm tra trùng với LT vừa tạo trong phiên này
        String key = ngayMoi + " " + gioMoi;
        if (danhSachDaTao.contains(key)) {
            return "Trùng với lịch trình vừa tạo trong phiên này (" + ngayMoi + " " + gioMoi + ")";
        }
        return null;
    }

    private String kiemTraTrungLich(String maTau, String ngayKHMoi,
                                    String gioDiMoi, int thoiGianPhut,
                                    String excludeMaChuyen) {
        return kiemTraTrungLich(maTau, ngayKHMoi, gioDiMoi, thoiGianPhut, excludeMaChuyen, null);
    }



    private String kiemTraTrungLich(String maTau, String ngayKHMoi,
                                    String gioDiMoi, int thoiGianPhut,
                                    String excludeMaChuyen, String excludeMaLT) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
        Date batDauMoi, ketThucMoi;
        try {
            String gioDiChuan = chuanHoaGio(gioDiMoi);
            if (gioDiChuan == null) gioDiChuan = gioDiMoi;
            batDauMoi = sdf.parse(ngayKHMoi + " " + gioDiChuan);
            Calendar c = Calendar.getInstance();
            c.setTime(batDauMoi);
            c.add(Calendar.MINUTE, thoiGianPhut > 0 ? thoiGianPhut : 1440);
            ketThucMoi = c.getTime();
        } catch (Exception e) {
            return null;
        }

        // ── CHECK 1: Cùng tàu không được chồng lịch ──────────────────────
        String sql1 = "SELECT lt.maLT, " +
                "CONVERT(VARCHAR, lt.ngayKhoiHanh, 103) AS ngayKH, " +
                "CONVERT(VARCHAR, lt.gioKhoiHanh, 108) AS gioDi, " +
                "CASE WHEN lt.ngayDen IS NOT NULL THEN FORMAT(lt.ngayDen, 'dd/MM/yyyy HH:mm') ELSE '' END AS ngayDen, " +
                "ct.maChuyen, ct.tenChuyen " +
                "FROM LichTrinh lt JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                "WHERE ct.maTau = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setString(1, maTau);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maChuyenRow = rs.getString("maChuyen");
                String maLTRow = rs.getString("maLT");
                if (excludeMaLT != null && maLTRow.equals(excludeMaLT)) continue;
                if (excludeMaChuyen != null && maChuyenRow.equals(excludeMaChuyen)) continue;

                String ngayKHDb = rs.getString("ngayKH");
                String gioDiDb  = rs.getString("gioDi");
                String ngayDenDb = rs.getString("ngayDen");
                if (ngayKHDb == null || gioDiDb == null) continue;

                try {
                    String gioDbChuan = chuanHoaGio(gioDiDb);
                    if (gioDbChuan == null) gioDbChuan = gioDiDb;
                    Date batDauCu = sdf.parse(ngayKHDb + " " + gioDbChuan);
                    Date ketThucCu;
                    if (ngayDenDb != null && !ngayDenDb.isEmpty()) {
                        ketThucCu = sdf.parse(ngayDenDb);
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(batDauCu);
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        ketThucCu = cal.getTime();
                    }
                    if (batDauMoi.before(ketThucCu) && ketThucMoi.after(batDauCu)) {
                        return "Tàu " + maTau + " đang chạy chuyến " + rs.getString("tenChuyen") + " (" + ngayKHDb + ")";
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) { e.printStackTrace(); }

        // ── CHECK 2: Cùng tuyến cùng thời điểm chỉ 1 tàu ────────────────
        // Lấy maTuyen từ maTau thông qua chuyến đang tạo
        String maTuyen = null;
        String sqlTuyen = "SELECT ct.maTuyen FROM ChuyenTau ct WHERE ct.maTau = ? " +
                "UNION SELECT ct2.maTuyen FROM ChuyenTau ct2 WHERE ct2.maChuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT ct.maTuyen FROM ChuyenTau ct WHERE ct.maTau = ?")) {
            ps.setString(1, maTau);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) maTuyen = rs.getString("maTuyen");
        } catch (Exception ignored) {}

        if (maTuyen != null) {
            String sql2 = "SELECT lt.maLT, " +
                    "CONVERT(VARCHAR, lt.ngayKhoiHanh, 103) AS ngayKH, " +
                    "CONVERT(VARCHAR, lt.gioKhoiHanh, 108) AS gioDi, " +
                    "CASE WHEN lt.ngayDen IS NOT NULL THEN FORMAT(lt.ngayDen, 'dd/MM/yyyy HH:mm') ELSE '' END AS ngayDen, " +
                    "ct.maChuyen, ct.tenChuyen, ct.maTau " +
                    "FROM LichTrinh lt JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                    "WHERE ct.maTuyen = ? AND ct.maTau != ?";
            try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, maTuyen);
                ps.setString(2, maTau);
                java.sql.ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String maLTRow = rs.getString("maLT");
                    String maChuyenRow = rs.getString("maChuyen");
                    if (excludeMaLT != null && maLTRow.equals(excludeMaLT)) continue;
                    if (excludeMaChuyen != null && maChuyenRow.equals(excludeMaChuyen)) continue;

                    String ngayKHDb = rs.getString("ngayKH");
                    String gioDiDb  = rs.getString("gioDi");
                    String ngayDenDb = rs.getString("ngayDen");
                    if (ngayKHDb == null || gioDiDb == null) continue;

                    try {
                        String gioDbChuan = chuanHoaGio(gioDiDb);
                        if (gioDbChuan == null) gioDbChuan = gioDiDb;
                        Date batDauCu = sdf.parse(ngayKHDb + " " + gioDbChuan);
                        Date ketThucCu;
                        if (ngayDenDb != null && !ngayDenDb.isEmpty()) {
                            ketThucCu = sdf.parse(ngayDenDb);
                        } else {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(batDauCu);
                            cal.add(Calendar.DAY_OF_MONTH, 1);
                            ketThucCu = cal.getTime();
                        }
                        if (batDauMoi.before(ketThucCu) && ketThucMoi.after(batDauCu)) {
                            return "Tuyến " + maTuyen + " đã có tàu " + rs.getString("maTau") +
                                    " chạy chuyến " + rs.getString("tenChuyen") + " (" + ngayKHDb + ")";
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        return null;
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

    // =========================================================================
    // LẤY THỜI GIAN CHẠY CỦA TUYẾN (phút) — dùng để tính ngayDen
    // =========================================================================
    /**
     * Truy vấn thoiGianChay (phút) từ bảng Tuyen theo maTuyen.
     * VD: Sài Gòn → Hà Nội = 1920 phút (32 tiếng)
     *
     * @return số phút, hoặc 1440 (1 ngày) nếu không tìm thấy
     */
    private int getThoiGianChayFromTuyen(String maTuyen) {
        if (maTuyen == null || maTuyen.isEmpty()) return 1440;
        String sql = "SELECT thoiGianChay FROM Tuyen WHERE maTuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTuyen);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int phut = rs.getInt("thoiGianChay");
                return phut > 0 ? phut : 1440;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 1440; // mặc định 1 ngày
    }

    // =========================================================================
    // TÍNH NGÀY ĐẾN từ ngàyĐi + giờĐi + thoiGianChay (phút)
    // =========================================================================
    /**
     * Tính ngày giờ đến dự kiến.
     *
     * @param ngayDi       "dd/MM/yyyy"
     * @param gioDi        "HH:mm"
     * @param thoiGianPhut số phút hành trình
     * @return "dd/MM/yyyy HH:mm" — ngày giờ đến dự kiến
     */
    /**
     * Tính ngày giờ đến dự kiến.
     * Hỗ trợ gioDi dạng HH:mm hoặc HH:mm:ss (tự động chuẩn hóa về HH:mm)
     *
     * @param ngayDi       "dd/MM/yyyy"
     * @param gioDi        "HH:mm" hoặc "HH:mm:ss"
     * @param thoiGianPhut số phút hành trình (từ Tuyen.thoiGianChay)
     * @return "dd/MM/yyyy HH:mm" — ngày giờ đến dự kiến
     */
    private String tinhNgayDen(String ngayDi, String gioDi, int thoiGianPhut) {
        try {
            // Chuẩn hóa giờ về HH:mm trước khi parse
            String gioDiChuan = chuanHoaGio(gioDi);
            if (gioDiChuan == null) gioDiChuan = "00:00";

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT + " HH:mm");
            java.util.Date batDau = sdf.parse(ngayDi + " " + gioDiChuan);
            Calendar cal = Calendar.getInstance();
            cal.setTime(batDau);
            cal.add(Calendar.MINUTE, thoiGianPhut > 0 ? thoiGianPhut : 1440);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return ngayDi + " 00:00"; // fallback
        }
    }

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
     */
    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }
    private JPanel makeTblHeader(String[] cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length, 0, 0));
        p.setBackground(TH_BG);
        p.setBorder(BorderFactory.createMatteBorder(1,1,0,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String col : cols) {
            JLabel l = new JLabel(col);
            l.setFont(F_LABEL); l.setForeground(TEXT_DARK);
            l.setBorder(BorderFactory.createEmptyBorder(8,12,8,4));
            l.setMinimumSize(new Dimension(90, 36));
            p.add(l);
        }
        return p;
    }
    private JPanel makeTblRow(JLabel[] labels) {
        JPanel p = new JPanel(new GridLayout(1, labels.length, 0, 0));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1,1,1,1,BORDER));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JLabel l : labels) {
            l.setBorder(BorderFactory.createEmptyBorder(8,12,8,4));
            l.setMinimumSize(new Dimension(90, 36));
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
                    case PRIMARY  -> { g2.setColor(getModel().isRollover()?ACCENT_HVR:ACCENT); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    case DANGER   -> { g2.setColor(getModel().isRollover()?BTN_RED_HVR:BTN_RED); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    case SUCCESS  -> { g2.setColor(getModel().isRollover()?new Color(0x16A34A):new Color(0x22C55E)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    default      -> { g2.setColor(getModel().isRollover()?new Color(0xE0ECFF):BTN2_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style==BtnStyle.SECONDARY?BTN2_FG:Color.WHITE);
        b.setPreferredSize(new Dimension(style==BtnStyle.DANGER?80:140,36));
        if (style==BtnStyle.SUCCESS) b.setPreferredSize(new Dimension(155,36));
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