package com.gui;

import com.dao.DAO_Gia;
import com.dao.DAO_Gia.GiaHeaderRow;
import com.dao.DAO_Gia.GiaDetailRow;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;

/**
 * Tab Quản lý Giá vé
 *
 * Cấu trúc DB (data2.sql):
 *  - GiaHeader : maGia, moTa, ngayApDung, ngayKetThuc
 *  - GiaDetail : maGia, maLoaiToa, maLoaiVe, maTuyen, gia
 *
 * Nghiệp vụ:
 *  - 1 GiaHeader (mã bảng giá) có nhiều GiaDetail
 *  - Chọn dòng GiaHeader → hiển thị GiaDetail tương ứng bên phải
 *  - Double-click → mở dialog cập nhật
 *  - Loại toa: LT_GMC (Ghế mềm có điều hòa), LT_GCC (Ghế cứng có điều hòa), LT_GN4 (Nằm 4 chỗ)
 *  - Loại vé:  LV01 (Người lớn), LV02 (Trẻ em - giảm 50%), LV03 (Người cao tuổi - giảm 30%)
 *  - Tuyến:    T01 (HN→SG), T02 (SG→HN), T03 (HN→ĐN)
 */
public class TAB_Gia extends JPanel {

    // =========================================================================
    // MÀU SẮC
    // =========================================================================
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color ACCENT_FOC  = new Color(0x4D9DE0);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT  = new Color(0xA0AEC0);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color ROW_ALT     = new Color(0xF7FAFF);
    private static final Color BTN2_BG     = new Color(0xF0F4FA);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);
    private static final Color BTN_RED     = new Color(0xC0392B);
    private static final Color BTN_RED_HVR = new Color(0xE74C3C);
    private static final Color TH_BG       = new Color(0xE8F0FB);
    private static final Color CLR_ON      = new Color(0x27AE60);
    private static final Color CLR_OFF     = new Color(0x7F8C8D);

    // =========================================================================
    // FONT
    // =========================================================================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // =========================================================================
    // CỘT BẢNG
    // GiaHeader: 4 hiển thị + 1 ẩn (ngayKetThuc raw)
    // =========================================================================
    private static final String[] COLS_GH = {
            "Mã Bảng Giá", "Mô Tả", "Ngày Áp Dụng", "Ngày Kết Thúc", "Trạng Thái",
            "_ngayKetThucRaw"  // ẩn, index 5
    };
    private static final String[] COLS_GD = {
            "Loại Toa", "Tuyến", "Giá (VND)",
            "_maLoaiToa", "_maTuyen"   // ẩn index 3, 4
    };

    // =========================================================================
    // DỮ LIỆU DANH MỤC — khớp data2.sql
    // =========================================================================
    // maLoaiToa → tenLoaiToa
    // Danh mục load động từ DB — tránh hardcode sai mã
    private String[][] DS_LOAI_TOA = {};
    private String[][] DS_LOAI_VE  = {};
    private String[][] DS_TUYEN    = {};

    private static final String[] TRANG_THAI_ARR = {"Đang áp dụng", "Ngừng áp dụng"};

    private static final String DATE_FMT = "dd/MM/yyyy";

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // DAO
    // =========================================================================
    private final DAO_Gia daoGia = new DAO_Gia();

    // =========================================================================
    // MODEL & TABLE
    // =========================================================================
    private final DefaultTableModel modelGH;
    private final DefaultTableModel modelGD;
    private final JTable            tblGH;
    private final JTable            tblGD;

    // Thông tin detail bên phải
    private final JLabel lblMaGia    = infoLbl("-");
    private final JLabel lblMoTa     = infoLbl("-");
    private final JLabel lblTuNgay   = infoLbl("-");
    private final JLabel lblDenNgay  = infoLbl("-");
    private final JLabel lblTrangThai= infoLbl("-");

    // =========================================================================
    // STAT BAR — labels thống kê
    // =========================================================================
    private final JLabel lblStatTotal  = new JLabel("0");
    private final JLabel lblStatDang   = new JLabel("0");
    private final JLabel lblStatNgung  = new JLabel("0");
    private final JLabel lblStatDetail = new JLabel("0");

    // =========================================================================
    // KHỞI TẠO
    // =========================================================================
    public TAB_Gia() {
        UIManager.put("PopupMenu.consumeEventOnClose", Boolean.TRUE);
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        modelGH = new DefaultTableModel(COLS_GH, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelGD = new DefaultTableModel(COLS_GD, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblGH = buildTableGH();
        tblGD = buildTableGD();

        tblGH.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshDetail();
        });
        tblGH.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openUpdateHeaderDialog();
            }
        });
        tblGD.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openUpdateDetailDialog();
            }
        });

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        JPanel leftPanel  = buildLeftPanel();
        JPanel rightPanel = buildRightPanel();

        // Panel trái cố định 700px, phải fill còn lại
        leftPanel.setPreferredSize(new Dimension(700, 0));
        leftPanel.setMinimumSize(new Dimension(700, 0));
        leftPanel.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));

        GridBagConstraints gcL = new GridBagConstraints();
        gcL.gridx = 0; gcL.gridy = 0; gcL.weightx = 0.0; gcL.weighty = 1.0;
        gcL.fill = GridBagConstraints.BOTH; gcL.insets = new Insets(0, 0, 0, 6);
        body.add(leftPanel, gcL);

        GridBagConstraints gcR = new GridBagConstraints();
        gcR.gridx = 1; gcR.gridy = 0; gcR.weightx = 1.0; gcR.weighty = 1.0;
        gcR.fill = GridBagConstraints.BOTH; gcR.insets = new Insets(0, 6, 0, 0);
        body.add(rightPanel, gcR);

        add(buildStatBar(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        loadFromDB();
    }

    // =========================================================================
    // PANEL TRÁI — danh sách GiaHeader
    // =========================================================================
    // =========================================================================
    // STAT BAR — 4 thẻ thống kê
    // =========================================================================
    private JPanel buildStatBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 16, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        bar.add(buildStatCard("TỔNG BẢNG GIÁ",   lblStatTotal,  new Color(37,  99, 235)));
        bar.add(buildStatCard("ĐANG ÁP DỤNG",     lblStatDang,   new Color(34, 197, 94)));
        bar.add(buildStatCard("NGỪNG ÁP DỤNG",    lblStatNgung,  new Color(239, 68, 68)));
        bar.add(buildStatCard("TỔNG CHI TIẾT GIÁ",lblStatDetail, new Color(100,116,139)));
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

        // Icon vẽ tay góc phải
        JLabel ico = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth()/2, cy = getHeight()/2;
                if (title.contains("TỔNG BẢNG")) {
                    // Icon: stack 3 tờ
                    g2.drawRoundRect(cx-8, cy-6, 16, 12, 3, 3);
                    g2.drawLine(cx-6, cy-9, cx+6, cy-9);
                    g2.drawLine(cx-5, cy-12, cx+5, cy-12);
                } else if (title.contains("ĐANG")) {
                    // Icon: tích trong vòng tròn
                    g2.drawOval(cx-8, cy-8, 16, 16);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-4, cy, cx-1, cy+4);
                    g2.drawLine(cx-1, cy+4, cx+5, cy-4);
                } else if (title.contains("NGỪNG")) {
                    // Icon: vòng tròn gạch ngang
                    g2.drawOval(cx-8, cy-8, 16, 16);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-5, cy, cx+5, cy);
                } else {
                    // Icon: bảng giá có $
                    g2.drawRoundRect(cx-8, cy-7, 16, 14, 3, 3);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-5, cy-3, cx+5, cy-3);
                    g2.drawLine(cx-5, cy,   cx+3, cy);
                    g2.drawLine(cx-5, cy+3, cx+1, cy+3);
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
        int total = modelGH.getRowCount();
        int dang = 0, ngung = 0;
        for (int i = 0; i < total; i++) {
            Object tt = modelGH.getValueAt(i, 4);
            if (tt != null && tt.toString().equals("Đang áp dụng")) dang++;
            else ngung++;
        }
        int detail = modelGD.getRowCount();
        lblStatTotal .setText(String.valueOf(total));
        lblStatDang  .setText(String.valueOf(dang));
        lblStatNgung .setText(String.valueOf(ngung));
        lblStatDetail.setText(String.valueOf(detail));
    }

    private JPanel buildLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("QUẢN LÝ BẢNG GIÁ VÉ");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_DARK);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel card = makeCard(new BorderLayout());
        JScrollPane sc = new JScrollPane(tblGH);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        styleScrollBar(sc.getVerticalScrollBar());
        styleScrollBar(sc.getHorizontalScrollBar());
        card.add(sc, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnBar.setOpaque(false);

        // Nút Thêm với icon +
        JButton btnThem = new JButton("  Thêm bảng giá") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = 14, cy = getHeight() / 2;
                g2.drawLine(x - 5, cy, x + 5, cy);
                g2.drawLine(x, cy - 5, x, cy + 5);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnThem.setFont(F_LABEL); btnThem.setForeground(Color.WHITE);
        btnThem.setPreferredSize(new Dimension(160, 36));
        btnThem.setContentAreaFilled(false); btnThem.setBorderPainted(false);
        btnThem.setFocusPainted(false);
        btnThem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> openAddHeaderDialog());

        // Nút Xóa bảng giá — icon thùng rác
        JButton btnXoa = new JButton("  Xóa bảng giá") {
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
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnXoa.setFont(F_LABEL);
        btnXoa.setForeground(Color.WHITE);
        btnXoa.setPreferredSize(new Dimension(145, 36));
        btnXoa.setContentAreaFilled(false);
        btnXoa.setBorderPainted(false);
        btnXoa.setFocusPainted(false);
        btnXoa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnXoa.addActionListener(e -> deleteHeader());

        btnBar.add(btnThem);
        btnBar.add(btnXoa);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(card,   BorderLayout.CENTER);
        pnl.add(btnBar, BorderLayout.SOUTH);
        return pnl;
    }

    // =========================================================================
    // PANEL PHẢI — chi tiết giá + thông tin bảng giá
    // =========================================================================
    private JPanel buildRightPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(BG_PAGE);

        JLabel title = new JLabel("CHI TIẾT GIÁ VÉ");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_DARK);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Bảng GiaDetail
        JPanel cardDetail = makeCard(new BorderLayout());
        JScrollPane scDetail = new JScrollPane(tblGD);
        scDetail.setBorder(BorderFactory.createEmptyBorder());
        scDetail.getViewport().setBackground(BG_CARD);
        scDetail.setPreferredSize(new Dimension(0, 200));
        scDetail.setMinimumSize(new Dimension(0, 200));
        styleScrollBar(scDetail.getVerticalScrollBar());
        cardDetail.add(scDetail, BorderLayout.CENTER);

        // Nút thêm/xóa detail — vẽ icon tự
        JPanel detailBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        detailBtnBar.setOpaque(false);

        // Nút Thêm chi tiết — icon +
        JButton btnThemDetail = new JButton("  Thêm chi tiết") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = 14, cy = getHeight() / 2;
                g2.drawLine(x - 5, cy, x + 5, cy);
                g2.drawLine(x, cy - 5, x, cy + 5);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnThemDetail.setFont(F_LABEL);
        btnThemDetail.setForeground(Color.WHITE);
        btnThemDetail.setPreferredSize(new Dimension(145, 36));
        btnThemDetail.setContentAreaFilled(false);
        btnThemDetail.setBorderPainted(false);
        btnThemDetail.setFocusPainted(false);
        btnThemDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Nút Xóa chi tiết — icon thùng rác
        JButton btnXoaDetail = new JButton("  Xóa") {
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
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnXoaDetail.setFont(F_LABEL);
        btnXoaDetail.setForeground(Color.WHITE);
        btnXoaDetail.setPreferredSize(new Dimension(90, 36));
        btnXoaDetail.setContentAreaFilled(false);
        btnXoaDetail.setBorderPainted(false);
        btnXoaDetail.setFocusPainted(false);
        btnXoaDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnThemDetail.addActionListener(e -> openAddDetailDialog());
        btnXoaDetail .addActionListener(e -> deleteDetail());
        detailBtnBar.add(btnThemDetail);
        detailBtnBar.add(btnXoaDetail);
        cardDetail.add(detailBtnBar, BorderLayout.SOUTH);

        // Card thông tin bảng giá đang chọn
        JPanel cardInfo = buildInfoCard();

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(cardDetail, BorderLayout.CENTER);
        center.add(cardInfo,   BorderLayout.SOUTH);

        pnl.add(top,    BorderLayout.NORTH);
        pnl.add(center, BorderLayout.CENTER);
        return pnl;
    }

    // =========================================================================
    // CARD THÔNG TIN BẢNG GIÁ ĐANG CHỌN
    // =========================================================================
    private JPanel buildInfoCard() {
        JPanel card = makeCard(new BorderLayout());
        card.setPreferredSize(new Dimension(0, 190));
        card.setMinimumSize(new Dimension(0, 190));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        body.add(sectionLbl("Thông tin Bảng Giá Đang Chọn"));
        body.add(Box.createVerticalStrut(8));
        body.add(makeTblHeader(new String[]{"Mã Bảng Giá", "Mô Tả"}));
        body.add(makeTblRow(new JLabel[]{lblMaGia, lblMoTa}));
        body.add(Box.createVerticalStrut(10));
        body.add(makeTblHeader(new String[]{"Ngày Áp Dụng", "Ngày Kết Thúc", "Trạng Thái"}));
        body.add(makeTblRow(new JLabel[]{lblTuNgay, lblDenNgay, lblTrangThai}));

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // XÂY DỰNG BẢNG
    // =========================================================================
    private JTable buildTableGH() {
        JTable t = new JTable(modelGH) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Ẩn cột raw index 5
        t.getColumnModel().getColumn(5).setMinWidth(0);
        t.getColumnModel().getColumn(5).setMaxWidth(0);
        t.getColumnModel().getColumn(5).setWidth(0);

        // Renderer trạng thái cột 4
        t.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());

        int[] w = {110, 200, 110, 110, 130};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyPaddingRenderer(t, 4); // không đè renderer cột 4
        return t;
    }

    private JTable buildTableGD() {
        JTable t = new JTable(modelGD) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        styleTable(t);
        // Ẩn 2 cột mã raw (index 3, 4)
        for (int i = 3; i <= 4; i++) {
            t.getColumnModel().getColumn(i).setMinWidth(0);
            t.getColumnModel().getColumn(i).setMaxWidth(0);
            t.getColumnModel().getColumn(i).setWidth(0);
        }
        int[] w = {180, 180, 150};
        for (int i = 0; i < w.length; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        applyPaddingRenderer(t, 3);
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

    private void applyPaddingRenderer(JTable t, int upTo) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setFont(F_CELL);
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < upTo; i++) {
            // Không ghi đè renderer cột trạng thái của tblGH
            if (t == tblGH && i == 4) continue;
            t.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    // =========================================================================
    // LÀM MỚI CHI TIẾT
    // =========================================================================
    private void refreshDetail() {
        modelGD.setRowCount(0);
        int row = tblGH.getSelectedRow();
        if (row < 0) { resetInfo(); return; }

        String maGia    = modelGH.getValueAt(row, 0).toString();
        String moTa     = modelGH.getValueAt(row, 1).toString();
        String tuNgay   = modelGH.getValueAt(row, 2).toString();
        String denNgay  = modelGH.getValueAt(row, 3).toString();
        String trangThai= modelGH.getValueAt(row, 4).toString();

        lblMaGia    .setText(maGia);
        lblMoTa     .setText(moTa);
        lblTuNgay   .setText(tuNgay);
        lblDenNgay  .setText(denNgay);
        lblTrangThai.setText(trangThai);
        lblTrangThai.setForeground(trangThai.equals("Đang áp dụng") ? CLR_ON : CLR_OFF);

        // Load GiaDetail tương ứng maGia từ DB (hoặc cache trong memory)
        loadDetailForMaGia(maGia);
    }

    private void resetInfo() {
        lblMaGia.setText("-"); lblMoTa.setText("-");
        lblTuNgay.setText("-"); lblDenNgay.setText("-"); lblTrangThai.setText("-");
        lblTrangThai.setForeground(TEXT_MID);
    }

    // =========================================================================
    // LOAD DỮ LIỆU — data2.sql: GiaHeader + GiaDetail
    // =========================================================================
    private void loadFromDB() {
        loadDanhMuc(); // Load mã loại toa, loại vé, tuyến từ DB trước
        modelGH.setRowCount(0);
        try {
            List<GiaHeaderRow> list = daoGia.getAllHeader();
            java.time.LocalDate today = java.time.LocalDate.now();
            for (GiaHeaderRow h : list) {
                // Tính trạng thái dựa trên ngày hiện tại
                String trangThai = "Ngừng áp dụng";
                try {
                    java.time.LocalDate den = java.time.LocalDate.parse(h.ngayKetThuc);
                    java.time.LocalDate tu  = java.time.LocalDate.parse(h.ngayApDung);
                    if (!today.isBefore(tu) && !today.isAfter(den)) trangThai = "Đang áp dụng";
                } catch (Exception ignored) {}

                modelGH.addRow(new Object[]{
                        h.maGia,
                        h.moTa,
                        formatNgay(h.ngayApDung),
                        formatNgay(h.ngayKetThuc),
                        trangThai,
                        h.ngayKetThuc   // raw ẩn
                });
            }
            if (modelGH.getRowCount() > 0) {
                tblGH.setRowSelectionInterval(0, 0);
            }
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    // =========================================================================
    // LOAD DANH MỤC TỪ DB — tránh hardcode sai mã
    // =========================================================================
    private void loadDanhMuc() {
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection()) {

            // LoaiToa: maLoaiToa, tenLoaiToa ✓
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT maLoaiToa, tenLoaiToa FROM LoaiToa ORDER BY maLoaiToa");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                java.util.List<String[]> list = new java.util.ArrayList<>();
                while (rs.next())
                    list.add(new String[]{rs.getString("maLoaiToa"), rs.getString("tenLoaiToa")});
                if (!list.isEmpty()) DS_LOAI_TOA = list.toArray(new String[0][]);
            } catch (Exception e) { e.printStackTrace(); }

            // LoaiVe: maLoai (PK), tenLoai, mucGiam — KHÔNG phải maLoaiVe/tenLoaiVe
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT maLoai, tenLoai, mucGiam FROM LoaiVe ORDER BY maLoai");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                java.util.List<String[]> list = new java.util.ArrayList<>();
                while (rs.next())
                    list.add(new String[]{
                            rs.getString("maLoai"),
                            rs.getString("tenLoai"),
                            String.valueOf((int)(rs.getDouble("mucGiam") * 100)) // 0.5 → "50"
                    });
                if (!list.isEmpty()) DS_LOAI_VE = list.toArray(new String[0][]);
            } catch (Exception e) { e.printStackTrace(); }

            // Tuyen: maTuyen, tenTuyen ✓
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "SELECT maTuyen, tenTuyen FROM Tuyen ORDER BY maTuyen");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                java.util.List<String[]> list = new java.util.ArrayList<>();
                while (rs.next())
                    list.add(new String[]{rs.getString("maTuyen"), rs.getString("tenTuyen")});
                if (!list.isEmpty()) DS_TUYEN = list.toArray(new String[0][]);
            } catch (Exception e) { e.printStackTrace(); }

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDetailForMaGia(String maGia) {
        modelGD.setRowCount(0);
        try {
            List<GiaDetailRow> list = daoGia.getDetailByMaGia(maGia);
            for (GiaDetailRow d : list) {
                modelGD.addRow(new Object[]{
                        tenLoaiToa(d.maLoaiToa),
                        tenTuyen(d.maTuyen),
                        formatGia(d.gia),
                        d.maLoaiToa,   // ẩn index 3
                        d.maTuyen      // ẩn index 4
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateStats();
    }

    // =========================================================================
    // DIALOG THÊM GIA HEADER
    // =========================================================================
    private void openAddHeaderDialog() {
        JDialog dlg = makeDialog("Thêm Bảng Giá Mới");

        // Tự sinh mã bảng giá
        String maTuSinh = genMaGia();

        JTextField txtMoTa    = makeField("VD: Bảng giá năm 2027");
        DatePickerField dpTu  = new DatePickerField("");
        DatePickerField dpDen = new DatePickerField("");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;

        addSep(form, gc, r++, "Thông tin Bảng Giá", ACCENT);
        addRow(form, gc, r++, "Mô tả *:",          txtMoTa);
        addRow(form, gc, r++, "Ngày áp dụng *:",   dpTu);
        addRow(form, gc, r,   "Ngày kết thúc *:",  dpDen);

        // Hint mã tự sinh
        JLabel lblMaHint = new JLabel("Mã: " + maTuSinh + "  (tự sinh)");
        lblMaHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMaHint.setForeground(TEXT_LIGHT);
        gc.gridx=0; gc.gridy=r+1; gc.gridwidth=2; gc.weightx=1;
        gc.insets=new Insets(2,6,4,6);
        form.add(lblMaHint, gc);
        gc.gridwidth=1; gc.insets=new Insets(5,6,5,6);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String moTa = txtMoTa.getText().trim();

            // --- Validate mô tả ---
            if (moTa.isEmpty()) {
                warn("Vui lòng nhập Mô tả bảng giá."); txtMoTa.requestFocus(); return;
            }

            // --- Validate ngày áp dụng ---
            String tuDisp = dpTu.getDate().trim();
            if (tuDisp.isEmpty()) { warn("Vui lòng chọn Ngày áp dụng."); return; }
            String tuRaw;
            try { tuRaw = toDbDate(tuDisp); java.time.LocalDate.parse(tuRaw); }
            catch (Exception ex) { warn("Ngày áp dụng không hợp lệ!"); return; }

            // --- Validate ngày kết thúc ---
            String denDisp = dpDen.getDate().trim();
            if (denDisp.isEmpty()) { warn("Vui lòng chọn Ngày kết thúc."); return; }
            String denRaw;
            try { denRaw = toDbDate(denDisp); java.time.LocalDate.parse(denRaw); }
            catch (Exception ex) { warn("Ngày kết thúc không hợp lệ!"); return; }

            // --- Validate thứ tự ngày ---
            if (!java.time.LocalDate.parse(denRaw).isAfter(java.time.LocalDate.parse(tuRaw))) {
                warn("Ngày kết thúc phải sau Ngày áp dụng!"); return;
            }

            // --- Validate trùng khoảng thời gian ---
            String trungTen = kiemTraTrungThoiGian(tuRaw, denRaw, null);
            if (trungTen != null) {
                warn("Khoảng thời gian bị trùng với bảng giá \"" + trungTen + "\"!\n"
                        + "Vui lòng chọn khoảng thời gian khác."); return;
            }

            boolean ok = daoGia.insertHeader(maTuSinh, moTa, tuRaw, denRaw);
            if (ok) {
                String tt = tinhTrangThai(tuRaw, denRaw);
                modelGH.addRow(new Object[]{maTuSinh, moTa, tuDisp, denDisp, tt, denRaw});
                updateStats();
                dlg.dispose();
            } else {
                warn("Lỗi khi lưu vào database! Mã bảng giá có thể đã tồn tại.");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    /** Tự sinh mã bảng giá dạng BGxx */
    private String genMaGia() {
        int max = 0;
        for (int i = 0; i < modelGH.getRowCount(); i++) {
            String ma = modelGH.getValueAt(i, 0).toString();
            try {
                int n = Integer.parseInt(ma.replaceAll("[^0-9]", ""));
                if (n > max) max = n;
            } catch (Exception ignored) {}
        }
        return String.format("BG%02d", max + 1);
    }

    /** Kiểm tra trùng khoảng thời gian với các bảng giá đã có */
    private String kiemTraTrungThoiGian(String tuRawMoi, String denRawMoi, String excludeMaGia) {
        try {
            java.time.LocalDate tuMoi  = java.time.LocalDate.parse(tuRawMoi);
            java.time.LocalDate denMoi = java.time.LocalDate.parse(denRawMoi);
            for (int i = 0; i < modelGH.getRowCount(); i++) {
                String maGia = modelGH.getValueAt(i, 0).toString();
                if (excludeMaGia != null && maGia.equals(excludeMaGia)) continue;

                // Bỏ qua bảng giá đã Ngừng áp dụng
                Object ttObj = modelGH.getValueAt(i, 4);
                if (ttObj != null && ttObj.toString().equals("Ngừng áp dụng")) continue;

                Object denRawObj = modelGH.getValueAt(i, 5);
                Object tuDispObj = modelGH.getValueAt(i, 2);
                if (denRawObj == null || tuDispObj == null) continue;
                try {
                    java.time.LocalDate tuCu  = java.time.LocalDate.parse(toDbDate(tuDispObj.toString()));
                    java.time.LocalDate denCu = java.time.LocalDate.parse(denRawObj.toString());
                    // Chồng lấp: tuMoi < denCu AND denMoi > tuCu
                    if (tuMoi.isBefore(denCu) && denMoi.isAfter(tuCu)) {
                        return modelGH.getValueAt(i, 1).toString();
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    // =========================================================================
    // DIALOG CẬP NHẬT GIA HEADER
    // =========================================================================
    private void openUpdateHeaderDialog() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá."); return; }

        String maGia    = modelGH.getValueAt(row, 0).toString();
        String moTa     = modelGH.getValueAt(row, 1).toString();
        String tuNgay   = modelGH.getValueAt(row, 2).toString();
        String denNgay  = modelGH.getValueAt(row, 3).toString();
        String trangThai= modelGH.getValueAt(row, 4).toString();

        JDialog dlg = makeDialog("Cập Nhật Bảng Giá");

        JTextField txtMoTa    = makeFieldVal(moTa);
        DatePickerField dpTu  = new DatePickerField(tuNgay);
        DatePickerField dpDen = new DatePickerField(denNgay);
        JComboBox<String> cbTT = makeCombo(TRANG_THAI_ARR);
        cbTT.setSelectedItem(trangThai);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;

        addSep(form, gc, r++, "Thông tin Bảng Giá", ACCENT);

        // Hint mã (không chỉnh được)
        JLabel lblMaHint = new JLabel("Mã: " + maGia);
        lblMaHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMaHint.setForeground(TEXT_LIGHT);
        gc.gridx=0; gc.gridy=r++; gc.gridwidth=2; gc.weightx=1;
        gc.insets=new Insets(0,6,4,6);
        form.add(lblMaHint, gc);
        gc.gridwidth=1; gc.insets=new Insets(5,6,5,6);

        addRow(form, gc, r++, "Mô tả *:",          txtMoTa);
        addRow(form, gc, r++, "Ngày áp dụng *:",   dpTu);
        addRow(form, gc, r++, "Ngày kết thúc *:",  dpDen);
        addRow(form, gc, r,   "Trạng thái:",        cbTT);

        final int selRow = row;
        JButton btnLuu = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String moTaNew = txtMoTa.getText().trim();
            if (moTaNew.isEmpty()) { warn("Vui lòng nhập Mô tả."); txtMoTa.requestFocus(); return; }

            String tuDisp = dpTu.getDate().trim();
            if (tuDisp.isEmpty()) { warn("Vui lòng chọn Ngày áp dụng."); return; }
            String tuRaw;
            try { tuRaw = toDbDate(tuDisp); java.time.LocalDate.parse(tuRaw); }
            catch (Exception ex) { warn("Ngày áp dụng không hợp lệ!"); return; }

            String denDisp = dpDen.getDate().trim();
            if (denDisp.isEmpty()) { warn("Vui lòng chọn Ngày kết thúc."); return; }
            String denRaw;
            try { denRaw = toDbDate(denDisp); java.time.LocalDate.parse(denRaw); }
            catch (Exception ex) { warn("Ngày kết thúc không hợp lệ!"); return; }

            if (!java.time.LocalDate.parse(denRaw).isAfter(java.time.LocalDate.parse(tuRaw))) {
                warn("Ngày kết thúc phải sau Ngày áp dụng!"); return;
            }

            String trungTen = kiemTraTrungThoiGian(tuRaw, denRaw, maGia);
            if (trungTen != null) {
                warn("Khoảng thời gian bị trùng với bảng giá \"" + trungTen + "\"!\n"
                        + "Vui lòng chọn khoảng thời gian khác."); return;
            }

            boolean ok = daoGia.updateHeader(maGia, moTaNew, tuRaw, denRaw);
            if (ok) {
                // Ưu tiên trạng thái user chọn thủ công
                String ttMoi = (String) cbTT.getSelectedItem();
                // Nếu user không chọn thủ công "Ngừng áp dụng" thì tính lại theo ngày
                if (ttMoi == null || ttMoi.equals("Đang áp dụng")) {
                    ttMoi = tinhTrangThai(tuRaw, denRaw);
                }
                modelGH.setValueAt(moTaNew, selRow, 1);
                modelGH.setValueAt(tuDisp,  selRow, 2);
                modelGH.setValueAt(denDisp, selRow, 3);
                modelGH.setValueAt(ttMoi,   selRow, 4);
                modelGH.setValueAt(denRaw,  selRow, 5);
                refreshDetail();
                updateStats();
                dlg.dispose();
            } else {
                warn("Lỗi khi cập nhật database!");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // XÓA GIA HEADER — Validate đã có hóa đơn dùng chưa
    // =========================================================================
    private void deleteHeader() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá để xóa."); return; }
        String maGia = modelGH.getValueAt(row, 0).toString();

        // --- Kiểm tra hóa đơn phát sinh ---
        int soVe = demSoVeDungBangGia(maGia);
        if (soVe > 0) {
            warn("Không thể xóa bảng giá " + maGia + "!\n" +
                    "Đã có " + soVe + " vé được bán theo bảng giá này.\n" +
                    "Để bảo toàn dữ liệu, hãy chuyển trạng thái sang \"Ngừng áp dụng\" thay vì xóa.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Xóa bảng giá " + maGia + "?\nTất cả chi tiết giá liên quan cũng sẽ bị xóa.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            boolean deleted = daoGia.deleteHeader(maGia);
            if (deleted) {
                modelGH.removeRow(row);
                modelGD.setRowCount(0);
                resetInfo();
                updateStats();
            } else {
                warn("Lỗi khi xóa! Kiểm tra lại ràng buộc khóa ngoại.");
            }
        }
    }

    // =========================================================================
    // DIALOG THÊM GIA DETAIL
    // =========================================================================
    private void openAddDetailDialog() {
        int row = tblGH.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một bảng giá trước."); return; }
        String maGia = modelGH.getValueAt(row, 0).toString();

        // --- Validate bảng giá còn hiệu lực ---
        String trangThai = modelGH.getValueAt(row, 4).toString();
        if ("Ngừng áp dụng".equals(trangThai)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bảng giá này đã NGỪNG ÁP DỤNG.\n" +
                            "Thêm chi tiết giá vào đây sẽ không có tác dụng thực tế.\n" +
                            "Bạn vẫn muốn tiếp tục?",
                    "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        JDialog dlg = makeDialog("Thêm Chi Tiết Giá — " + maGia);

        // ComboBox Loại Toa
        String[] tenToaArr = new String[DS_LOAI_TOA.length];
        for (int i = 0; i < DS_LOAI_TOA.length; i++) tenToaArr[i] = DS_LOAI_TOA[i][1];
        JComboBox<String> cbToa = makeCombo(tenToaArr);

        // ComboBox Tuyến
        String[] tenTuyenArr = new String[DS_TUYEN.length];
        for (int i = 0; i < DS_TUYEN.length; i++) tenTuyenArr[i] = DS_TUYEN[i][1];
        JComboBox<String> cbTuyen = makeCombo(tenTuyenArr);

        JTextField txtGia = makeField("VD: 1150000  (giá người lớn - các loại vé khác tự giảm theo quy định)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Loại toa:",  cbToa);
        addRow(form, gc, r++, "Tuyến:",     cbTuyen);
        addRow(form, gc, r,   "Giá (VND):", txtGia);

        JButton btnLuu = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String giaStr = txtGia.getText().trim();

            if (giaStr.isEmpty()) { warn("Vui lòng nhập Giá vé."); txtGia.requestFocus(); return; }
            long giaVal;
            try {
                giaVal = Long.parseLong(giaStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                warn("Giá vé không hợp lệ! Chỉ nhập số nguyên dương."); return;
            }
            if (giaVal <= 0) { warn("Giá vé phải lớn hơn 0!"); return; }
            if (giaVal > 100_000_000) { warn("Giá vé vượt quá giới hạn cho phép (100 triệu VND)."); return; }

            int idxToa   = cbToa.getSelectedIndex();
            int idxTuyen = cbTuyen.getSelectedIndex();
            String maToa      = DS_LOAI_TOA[idxToa][0];
            String maTuyenSel = DS_TUYEN[idxTuyen][0];

            // --- Validate trùng tổ hợp (Toa + Tuyến) ---
            for (int i = 0; i < modelGD.getRowCount(); i++) {
                if (modelGD.getValueAt(i, 3).toString().equals(maToa)
                        && modelGD.getValueAt(i, 4).toString().equals(maTuyenSel)) {
                    warn("Tổ hợp (Loại toa / Tuyến) này đã tồn tại trong bảng giá!\n"
                            + "Vui lòng chọn tổ hợp khác hoặc cập nhật dòng đã có.");
                    return;
                }
            }

            // --- Insert 1 dòng (giá cơ sở) ---
            // Mức giảm theo loại vé (trẻ em, sinh viên) được xử lý ở Khuyến mãi khi tính tiền
            boolean ok = daoGia.insertDetail(maGia, maToa, maTuyenSel, giaVal);

            if (ok) {
                modelGD.addRow(new Object[]{
                        tenLoaiToa(maToa), tenTuyen(maTuyenSel),
                        formatGia(giaVal), maToa, maTuyenSel
                });
                updateStats();
                dlg.dispose();
            } else {
                warn("Lỗi khi lưu vào database!");
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    // =========================================================================
    // DIALOG CẬP NHẬT GIA DETAIL — Auto-versioning thông minh
    // Nếu bảng giá đã có vé bán:
    //   - Lần sửa đầu → clone bảng giá, đóng bảng cũ
    //   - Các lần sửa tiếp → update trực tiếp bảng clone (chưa có vé)
    // =========================================================================
    private void openUpdateDetailDialog() {
        int row = tblGD.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một dòng chi tiết giá."); return; }

        String tenToa   = modelGD.getValueAt(row, 0).toString();
        String tenTuyen = modelGD.getValueAt(row, 1).toString();
        String giaHienTai = modelGD.getValueAt(row, 2).toString()
                .replace(".", "").replace(" VND", "");

        JDialog dlg = makeDialog("Cập Nhật Chi Tiết Giá");

        JTextField txtToa   = roField(tenToa);
        JTextField txtTuyen = roField(tenTuyen);
        JTextField txtGia   = makeFieldVal(giaHienTai);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = defaultGC();
        int r = 0;
        addRow(form, gc, r++, "Loại toa:",  txtToa);
        addRow(form, gc, r++, "Tuyến:",     txtTuyen);
        addRow(form, gc, r,   "Giá (VND):", txtGia);

        String maToa      = modelGD.getValueAt(row, 3).toString();
        String maTuyenSel = modelGD.getValueAt(row, 4).toString();
        int ghRow = tblGH.getSelectedRow();
        String maGiaHdr   = modelGH.getValueAt(ghRow, 0).toString();

        final int selRow = row;
        JButton btnLuu = makeBtn("Cập nhật", BtnStyle.PRIMARY);
        btnLuu.addActionListener(e -> {
            String giaStr = txtGia.getText().trim();
            if (giaStr.isEmpty()) { warn("Vui lòng nhập Giá vé."); txtGia.requestFocus(); return; }
            long giaVal;
            try {
                giaVal = Long.parseLong(giaStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                warn("Giá vé không hợp lệ! Chỉ nhập số nguyên dương."); return;
            }
            if (giaVal <= 0) { warn("Giá vé phải lớn hơn 0!"); return; }
            if (giaVal > 100_000_000) { warn("Giá vé vượt quá giới hạn cho phép (100 triệu VND)."); return; }

            long giaCu;
            try { giaCu = Long.parseLong(giaHienTai.replaceAll("[^0-9]", "")); }
            catch (Exception ex) { giaCu = 0; }
            if (giaVal == giaCu) { dlg.dispose(); return; }

            int soVe = demSoVeDungBangGia(maGiaHdr);

            if (soVe == 0) {
                // ── Chưa có vé bán → sửa trực tiếp ──
                if (daoGia.updateDetail(maGiaHdr, maToa, maTuyenSel, giaVal)) {
                    modelGD.setValueAt(formatGia(giaVal), selRow, 2);
                    dlg.dispose();
                } else {
                    warn("Lỗi khi cập nhật database!");
                }
            } else {
                // ── Đã có vé → auto-versioning ──
                // Hỏi user chọn ngày áp dụng giá mới
                java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
                DatePickerField dpApDung = new DatePickerField(formatNgay(tomorrow.toString()));

                JPanel pnlChonNgay = new JPanel(new BorderLayout(8, 8));
                pnlChonNgay.setOpaque(false);
                pnlChonNgay.add(new JLabel("<html>Đã có <b>" + soVe + "</b> vé bán theo bảng giá này.<br><br>"
                        + "Chọn <b>ngày áp dụng</b> giá mới:<br>"
                        + "<i>(Bảng giá cũ sẽ kết thúc vào ngày trước đó)</i></html>"), BorderLayout.NORTH);
                pnlChonNgay.add(dpApDung, BorderLayout.CENTER);

                int choice = JOptionPane.showConfirmDialog(dlg, pnlChonNgay,
                        "Tạo phiên bản giá mới",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) return;

                String ngayApDungDisp = dpApDung.getDate().trim();
                if (ngayApDungDisp.isEmpty()) { warn("Vui lòng chọn ngày áp dụng."); return; }

                java.time.LocalDate ngayApDung;
                try {
                    ngayApDung = java.time.LocalDate.parse(toDbDate(ngayApDungDisp));
                } catch (Exception ex) { warn("Ngày áp dụng không hợp lệ!"); return; }

                if (!ngayApDung.isAfter(java.time.LocalDate.now())) {
                    warn("Ngày áp dụng phải sau hôm nay!"); return;
                }

                // Kiểm tra đã có clone cho ngày này chưa
                String maGiaClone = timBangGiaClone(maGiaHdr, ngayApDung);

                if (maGiaClone != null) {
                    // Đã có clone → update trực tiếp
                    if (daoGia.updateDetail(maGiaClone, maToa, maTuyenSel, giaVal)) {
                        loadFromDB();
                        selectBangGia(maGiaClone);
                        dlg.dispose();
                    } else {
                        warn("Lỗi khi cập nhật bảng giá mới!");
                    }
                } else {
                    // Chưa có clone → tạo mới
                    String maNew = cloneBangGia(maGiaHdr, maToa, maTuyenSel, giaVal, ngayApDung);
                    if (maNew != null) {
                        loadFromDB();
                        selectBangGia(maNew);
                        dlg.dispose();
                        JOptionPane.showMessageDialog(TAB_Gia.this,
                                "Đã tạo bảng giá mới thành công!\n" +
                                        "Giá mới áp dụng từ " + ngayApDungDisp + ".\n" +
                                        "Bảng giá cũ kết thúc vào " + formatNgay(ngayApDung.minusDays(1).toString()) + ".\n" +
                                        "Bạn có thể tiếp tục sửa các dòng khác trong bảng mới.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        warn("Lỗi khi tạo phiên bản mới!");
                    }
                }
            }
        });
        showDlg(dlg, form, btnLuu);
    }

    /**
     * Tìm bảng giá clone đã được tạo cho ngày áp dụng chỉ định (chưa có vé bán).
     * @param ngayApDung ngày áp dụng cần tìm
     * @return maGia của bảng clone, hoặc null nếu chưa có
     */
    private String timBangGiaClone(String maGiaGoc, java.time.LocalDate ngayApDung) {
        String sql = "SELECT gh.maGia FROM GiaHeader gh " +
                "WHERE gh.ngayApDung = ? " +
                "AND gh.moTa LIKE ? " +
                "AND NOT EXISTS (SELECT 1 FROM Ve v " +
                "  JOIN LichTrinh lt ON v.maLT = lt.maLT " +
                "  JOIN GiaHeader gh2 ON gh2.maLT = lt.maLT " +
                "  WHERE gh2.maGia = gh.maGia)";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ngayApDung.toString());
            ps.setString(2, "%(cập nhật%");
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("maGia");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Clone bảng giá: đóng bảng cũ → tạo mới → copy detail + áp giá mới.
     * @param ngayApDung ngày áp dụng bảng giá mới (user chọn)
     * @return maGia mới, hoặc null nếu lỗi
     */
    private String cloneBangGia(String maGiaCu, String maToaSua,
                                String maTuyenSua, long giaMoi,
                                java.time.LocalDate ngayApDung) {
        try {
            String moTaCu = null, denRawCu = null;
            for (int i = 0; i < modelGH.getRowCount(); i++) {
                if (modelGH.getValueAt(i, 0).toString().equals(maGiaCu)) {
                    moTaCu   = modelGH.getValueAt(i, 1).toString();
                    denRawCu = modelGH.getValueAt(i, 5).toString();
                    break;
                }
            }
            if (moTaCu == null) return null;

            // Ngày kết thúc bảng cũ = ngày trước ngày áp dụng mới
            java.time.LocalDate ngayDongCu = ngayApDung.minusDays(1);

            // Ngày kết thúc bảng mới: giữ nguyên từ bảng cũ (nếu > ngày áp dụng)
            java.time.LocalDate denCu;
            try { denCu = java.time.LocalDate.parse(denRawCu); }
            catch (Exception ex) { denCu = ngayApDung.plusYears(1); }
            java.time.LocalDate denMoi = denCu.isAfter(ngayApDung) ? denCu : ngayApDung.plusMonths(6);

            // 1. Đóng bảng giá cũ: ngayKetThuc = ngày trước ngày áp dụng mới
            try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                         "UPDATE GiaHeader SET ngayKetThuc = ? WHERE maGia = ?")) {
                ps.setString(1, ngayDongCu.toString());
                ps.setString(2, maGiaCu);
                ps.executeUpdate();
            }

            // 2. Tạo bảng giá mới
            String maGiaMoi = genMaGia();
            String moTaMoi  = moTaCu.replaceAll("\\s*\\(cập nhật.*\\)", "")
                    + " (cập nhật " + formatNgay(ngayApDung.toString()) + ")";
            if (!daoGia.insertHeader(maGiaMoi, moTaMoi, ngayApDung.toString(), denMoi.toString()))
                return null;

            // 3. Clone detail, thay giá dòng đang sửa
            List<GiaDetailRow> dsCu = daoGia.getDetailByMaGia(maGiaCu);
            for (GiaDetailRow d : dsCu) {
                long gia = d.gia;
                if (d.maLoaiToa.equals(maToaSua) && d.maTuyen.equals(maTuyenSua)) {
                    gia = giaMoi;
                }
                daoGia.insertDetail(maGiaMoi, d.maLoaiToa, d.maTuyen, gia);
            }
            return maGiaMoi;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Chọn bảng giá theo maGia trong tblGH */
    private void selectBangGia(String maGia) {
        for (int i = 0; i < modelGH.getRowCount(); i++) {
            if (modelGH.getValueAt(i, 0).toString().equals(maGia)) {
                tblGH.setRowSelectionInterval(i, i);
                tblGH.scrollRectToVisible(tblGH.getCellRect(i, 0, true));
                break;
            }
        }
    }

    // =========================================================================
    // XÓA GIA DETAIL — Validate nếu đã có hóa đơn dùng
    // =========================================================================
    private void deleteDetail() {
        int row = tblGD.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một dòng chi tiết giá để xóa."); return; }

        String maToa      = modelGD.getValueAt(row, 3).toString();
        String maTuyenSel = modelGD.getValueAt(row, 4).toString();
        String maGiaHdr   = modelGH.getValueAt(tblGH.getSelectedRow(), 0).toString();

        // --- Đếm số vé đã bán với tổ hợp (maGia + Toa + Tuyến) ---
        int soVeDung = demSoVeDungChiTietGia(maGiaHdr, maToa, maTuyenSel);
        if (soVeDung > 0) {
            warn("Không thể xóa chi tiết giá này!\n" +
                    "Đã có " + soVeDung + " vé được bán với tổ hợp này.\n" +
                    "Để bảo toàn dữ liệu lịch sử, hãy tạo bảng giá mới thay vì xóa.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dòng chi tiết giá này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (daoGia.deleteDetail(maGiaHdr, maToa, maTuyenSel)) {
                modelGD.removeRow(row);
                updateStats();
            } else {
                warn("Lỗi khi xóa chi tiết giá!");
            }
        }
    }

    /**
     * Đếm số vé đã bán với tổ hợp maGia + maLoaiToa + maTuyen.
     * Một vé dùng tổ hợp này khi:
     *  - Lịch trình (Ve.maLT) nằm trong bảng giá (GiaHeader.maLT)
     *  - Loại toa của chỗ ngồi khớp maLoaiToa
     *  - Tuyến khớp maTuyen
     */
    private int demSoVeDungChiTietGia(String maGia, String maLoaiToa, String maTuyen) {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN ChoNgoi cn ON v.maCho = cn.maCho " +
                "JOIN Toa t       ON cn.maToa = t.maToa " +
                "JOIN LichTrinh lt ON v.maLT = lt.maLT " +
                "JOIN ChuyenTau ct ON lt.maChuyen = ct.maChuyen " +
                "JOIN GiaHeader gh ON gh.maLT = lt.maLT " +
                "WHERE gh.maGia = ? AND t.maLoaiToa = ? AND ct.maTuyen = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            ps.setString(2, maLoaiToa);
            ps.setString(3, maTuyen);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /** Đếm tổng số vé đã bán với một bảng giá (dùng để validate xóa/sửa header) */
    private int demSoVeDungBangGia(String maGia) {
        String sql = "SELECT COUNT(*) FROM Ve v " +
                "JOIN GiaHeader gh ON gh.maLT = v.maLT " +
                "WHERE gh.maGia = ?";
        try (java.sql.Connection conn = com.connectDB.ConnectDB.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGia);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // =========================================================================
    // HELPER UI — giống TAB_LichTrinh
    // =========================================================================
    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder());
        return p;
    }

    private JPanel makeTblHeader(String[] cols) {
        JPanel p = new JPanel(new GridLayout(1, cols.length));
        p.setBackground(TH_BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, BORDER));
        for (String c : cols) {
            JLabel l = new JLabel(c);
            l.setFont(F_LABEL); l.setForeground(TEXT_DARK);
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 4));
            p.add(l);
        }
        return p;
    }

    private JPanel makeTblRow(JLabel[] labels) {
        JPanel p = new JPanel(new GridLayout(1, labels.length));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER));
        for (JLabel l : labels) {
            l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 4));
            p.add(l);
        }
        return p;
    }

    private static JLabel infoLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(F_CELL); l.setForeground(TEXT_DARK); return l;
    }

    private JLabel sectionLbl(String t) {
        JLabel l = new JLabel(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cy = getHeight() / 2;
                // Icon bảng giá (khung với $ bên trong)
                g2.drawRoundRect(4, cy-6, 14, 12, 2, 2);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(7, cy-3, 15, cy-3);
                g2.drawLine(7, cy,   13, cy);
                g2.drawLine(7, cy+3, 11, cy+3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(F_LABEL);
        l.setForeground(TEXT_DARK);
        l.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        return l;
    }

    private JLabel sepLbl(String t, Color c) {
        String clean = t.replaceAll("[\\u2500-\\u257F]+", "").trim();
        JLabel l = new JLabel(clean) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tw = fm.stringWidth(getText()), cy = getHeight() / 2;
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

    private void addSep(JPanel form, GridBagConstraints gc, int row, String txt, Color c) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1;
        JLabel lbl = sepLbl(txt, c);
        lbl.setPreferredSize(new Dimension(0, 26));
        form.add(lbl, gc);
        gc.gridwidth = 1;
    }

    private JTextField roField(String v) {
        JTextField tf = makeFieldVal(v);
        tf.setEditable(false);
        tf.setBackground(new Color(0xEEF2F8));
        return tf;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    Insets ins = getInsets();
                    g2.drawString(hint, ins.left + 4, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        styleField(tf); return tf;
    }

    private JTextField makeFieldVal(Object v) {
        JTextField tf = new JTextField(v != null ? v.toString() : "");
        styleField(tf); return tf;
    }

    private void styleField(JTextField tf) {
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK);
        tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_FOC, 2, true),
                        BorderFactory.createEmptyBorder(5, 9, 5, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_CELL); cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        return cb;
    }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        return gc;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel l = new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID);
        form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1;
        field.setPreferredSize(new Dimension(260, 36));
        form.add(field, gc);
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                switch (style) {
                    case PRIMARY   -> { g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    case DANGER    -> { g2.setColor(getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                    default        -> { g2.setColor(getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(BORDER);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL);
        b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(style == BtnStyle.DANGER ? 80 : 150, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JDialog makeDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = (owner instanceof Frame)
                ? new JDialog((Frame) owner, title, true)
                : new JDialog((Dialog) owner, title, true);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(BG_PAGE);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return d;
    }

    private void showDlg(JDialog dlg, JPanel form, JButton ok) {
        JButton huy = makeBtn("Hủy", BtnStyle.SECONDARY);
        huy.addActionListener(e -> dlg.dispose());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        bar.setOpaque(false); bar.add(huy); bar.add(ok);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bar,  BorderLayout.SOUTH);
        dlg.setResizable(false);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(460, dlg.getHeight()));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xC0D4EE); trackColor = BG_PAGE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zBtn(); }
            private JButton zBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    // =========================================================================
    // HELPER — tra tên từ mã (dùng cho loadDetailForMaGia)
    // =========================================================================
    private String tenLoaiToa(String ma) {
        for (String[] r : DS_LOAI_TOA) if (r[0].equals(ma)) return r[1];
        return ma;
    }
    private String tenLoaiVe(String ma) {
        for (String[] r : DS_LOAI_VE) if (r[0].equals(ma)) return r[1];
        return ma;
    }
    private String tenTuyen(String ma) {
        for (String[] r : DS_TUYEN) if (r[0].equals(ma)) return r[1];
        return ma;
    }

    // =========================================================================
    // HELPER — tính trạng thái từ ngày raw yyyy-MM-dd
    // =========================================================================
    private String tinhTrangThai(String tuRaw, String denRaw) {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate tu  = java.time.LocalDate.parse(tuRaw);
            java.time.LocalDate den = java.time.LocalDate.parse(denRaw);
            if (!today.isBefore(tu) && !today.isAfter(den)) return "Đang áp dụng";
        } catch (Exception ignored) {}
        return "Ngừng áp dụng";
    }

    // =========================================================================
    // HELPER — chuyển đổi định dạng ngày
    // =========================================================================
    /** dd/MM/yyyy → yyyy-MM-dd để lưu DB */
    private String toDbDate(String display) {
        try {
            String[] p = display.split("/");
            if (p.length == 3) return p[2] + "-" + p[1] + "-" + p[0];
        } catch (Exception ignored) {}
        return display; // fallback: trả nguyên nếu đã đúng định dạng
    }

    /** yyyy-MM-dd → dd/MM/yyyy để hiển thị */
    private String formatNgay(String raw) {
        try {
            String[] p = raw.split("-");
            if (p.length == 3) return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception ignored) {}
        return raw;
    }

    private String formatGia(long gia) {
        return String.format("%,d", gia).replace(',', '.') + " VND";
    }

    // =========================================================================
    // RENDERER TRẠNG THÁI
    // =========================================================================
    private static class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String val = v != null ? v.toString() : "";
            if (val.equals("Đang áp dụng")) l.setForeground(new Color(0x27AE60));
            else                             l.setForeground(new Color(0x7F8C8D));
            if (!sel) l.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
            return l;
        }
    }

    // =========================================================================
    // RENDERER HEADER
    // =========================================================================
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
            return l;
        }
    }

    // =========================================================================
    // SHADOW BORDER
    // =========================================================================
    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = S; i > 0; i--) {
                g2.setColor(new Color(100, 140, 200, (int)(20.0 * (S - i) / S)));
                g2.drawRoundRect(x + i, y + i, w - 2*i - 1, h - 2*i - 1, 12, 12);
            }
            g2.setColor(new Color(0xE2EAF4)); g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.setColor(BG_CARD);
            g2.setClip(new RoundRectangle2D.Float(x+1, y+1, w-2, h-2, 12, 12));
            g2.fillRect(x+1, y+1, w-2, h-2);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(S, S, S, S); }
        @Override public Insets getBorderInsets(Component c, Insets ins) { ins.set(S, S, S, S); return ins; }
    }

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
    } // end DatePickerField
}