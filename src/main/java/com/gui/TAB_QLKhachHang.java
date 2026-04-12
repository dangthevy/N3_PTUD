package com.gui;

import com.dao.DAO_KhachHang;
import com.entities.NhanVien;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.util.Vector;

public class TAB_QLKhachHang extends JPanel {

    // ================= COLOR (copy từ TAB_QLNhanVien) =================
    private static final Color BG_PAGE    = new Color(0xF4F7FB);
    private static final Color BG_CARD    = Color.WHITE;
    private static final Color ACCENT     = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR = new Color(0x2270CC);
    private static final Color TEXT_DARK  = new Color(0x1E2B3C);
    private static final Color TEXT_MID   = new Color(0x5A6A7D);
    private static final Color TEXT_LIGHT = new Color(0xA0AEC0);
    private static final Color BORDER     = new Color(0xE2EAF4);
    private static final Color ROW_ALT    = new Color(0xF7FAFF);
    private static final Color ROW_SEL    = new Color(0xDDEEFF);
    private static final Color BTN_RED    = new Color(0xC0392B);
    private static final Color BTN_RED_HVR= new Color(0xE74C3C);
    private static final Color COLOR_BORDER     = new Color(226, 232, 240);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    // ================= FONT (copy từ TAB_QLNhanVien) =================
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private static final String[] COLS = {"Mã KH", "Tên", "Email", "SĐT", "CCCD"};

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblTotal   = new JLabel("0");
    private JLabel lblCoEmail = new JLabel("0");
    private DAO_KhachHang kh_dao = new DAO_KhachHang();

    public TAB_QLKhachHang(NhanVien nv) {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tableModel = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = buildTable();

        // NORTH xếp dọc giống TAB_QLNhanVien
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(buildHeader());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildStatsBar());
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(buildFilterCard());

        add(topPanel,        BorderLayout.NORTH);
        add(buildMainCard(), BorderLayout.CENTER);

        loadData();
    }

    // ─── HEADER ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        JLabel lbl = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lbl.setFont(F_TITLE);
        lbl.setForeground(ACCENT);
        pnl.add(lbl, BorderLayout.WEST);
        return pnl;
    }

    // ─── STATS BAR ────────────────────────────────────────────────────────────
    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 2, 12, 0));
        bar.setOpaque(false);
        bar.add(createStatCard("TỔNG KHÁCH HÀNG", lblTotal,   ACCENT));
        bar.add(createStatCard("CÓ EMAIL",         lblCoEmail, new Color(34, 197, 94)));
        return bar;
    }

    // ─── FILTER CARD ──────────────────────────────────────────────────────────
    private JPanel buildFilterCard() {
        JPanel card = buildCard(new FlowLayout(FlowLayout.LEFT, 12, 12));
        txtSearch = makeField("Tên / SĐT / Email...");
        JButton btnSearch  = makeBtn("Tìm kiếm", BtnStyle.PRIMARY);
        JButton btnRefresh = makeBtn("Làm mới",  BtnStyle.SECONDARY);

        card.add(makeLabel("Tìm:"));
        card.add(txtSearch);
        card.add(btnSearch);
        card.add(btnRefresh);

        btnSearch.addActionListener(e -> search());
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadData(); });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { search(); }
        });
        return card;
    }

    // ─── MAIN CARD ────────────────────────────────────────────────────────────
    private JPanel buildMainCard() {
        JPanel card = buildCard(new BorderLayout());
        card.add(buildActionBar(), BorderLayout.NORTH);
        card.add(buildTableBody(), BorderLayout.CENTER);
        return card;
    }

    // ─── ACTION BAR (label trái + nút phải) ───────────────────────────────────
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JLabel lbl = new JLabel("Danh sách khách hàng");
        lbl.setFont(F_LABEL);
        lbl.setForeground(TEXT_DARK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnAdd    = makeBtn("+ Thêm", BtnStyle.PRIMARY);
        JButton btnDelete = makeBtn("Xóa KH", BtnStyle.DANGER);

        btnAdd.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Mở form thêm khách hàng"));

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa khách hàng này?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                kh_dao.deleteKhachHang(table.getValueAt(row, 0).toString());
                loadData();
            }
        });

        right.add(btnAdd);
        right.add(btnDelete);
        bar.add(lbl,   BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─── TABLE BODY ───────────────────────────────────────────────────────────
    private JPanel buildTableBody() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 360));
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        JLabel lblEmpty = new JLabel(
                "Chưa có dữ liệu – nhấn \"+ Thêm\" hoặc tìm kiếm",
                SwingConstants.CENTER);
        lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEmpty.setForeground(TEXT_LIGHT);
        lblEmpty.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(sep,      BorderLayout.NORTH);
        body.add(scroll,   BorderLayout.CENTER);
        body.add(lblEmpty, BorderLayout.SOUTH);
        return body;
    }

    // ─── DATA ─────────────────────────────────────────────────────────────────
    private void loadData() {
        tableModel.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.getAllKhachHang();
        int coEmail = 0;
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
            // col 2 = email
            Object email = row.size() > 2 ? row.get(2) : null;
            if (email != null && !email.toString().trim().isEmpty()) coEmail++;
        }
        lblTotal.setText(String.valueOf(data.size()));
        lblCoEmail.setText(String.valueOf(coEmail));
    }

    private void search() {
        tableModel.setRowCount(0);
        Vector<Vector<Object>> data = kh_dao.searchKhachHang(txtSearch.getText().trim());
        for (Vector<Object> row : data) tableModel.addRow(row);
    }

    // ─── TABLE BUILD ──────────────────────────────────────────────────────────
    private JTable buildTable() {
        JTable t = new JTable(tableModel) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row))
                    c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setFont(F_CELL);
        t.setRowHeight(38);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(TEXT_DARK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer cellR = new DefaultTableCellRenderer();
        cellR.setFont(F_CELL);
        cellR.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
        for (int i = 0; i < COLS.length; i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cellR);

        return t;
    }

    // ─── UI HELPERS ───────────────────────────────────────────────────────────
    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(COLOR_TEXT_MUTED);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        p.add(lblT,     BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCard(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG_CARD);
        p.setBorder(new ShadowBorder());
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_MID);
        return l;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField(13);
        tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true));
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                switch (style) {
                    case PRIMARY:
                        g2.setColor(getModel().isPressed()  ? new Color(0x0F3F8C)
                                  : getModel().isRollover() ? ACCENT_HVR : ACCENT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    case DANGER:
                        g2.setColor(getModel().isPressed()  ? new Color(0x922B21)
                                  : getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        break;
                    default: break;
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(style == BtnStyle.SECONDARY ? new Color(0x3A5A8C) : Color.WHITE);
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = new Color(0xC0D4EE); }
        });
    }

    // ─── RENDERERS ────────────────────────────────────────────────────────────
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            l.setOpaque(true);
            l.setBackground(ACCENT);
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 6));
            return l;
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
        }
    }
}