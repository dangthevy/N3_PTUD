package com.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * UITheme - Bộ hằng số và helper dùng chung toàn app
 * Đồng bộ theo thiết kế: nền trắng/xám nhạt, PRIMARY xanh đậm, stat cards, header xanh
 */
public class UITheme {

    // ===================== MÀU SẮC =====================
    public static final Color PRIMARY       = new Color(41, 128, 185);   // #2980B9 - xanh chủ đạo
    public static final Color PRIMARY_DARK  = new Color(31, 97, 141);    // hover / header đậm hơn
    public static final Color PRIMARY_LIGHT = new Color(214, 234, 248);  // nền nhạt xanh
    public static final Color SUCCESS       = new Color(39, 174, 96);    // #27AE60
    public static final Color DANGER        = new Color(231, 76,  60);   // #E74C3C
    public static final Color WARNING       = new Color(243, 156, 18);   // #F39C12
    public static final Color TEXT_DARK     = new Color(44,  62,  80);   // tiêu đề
    public static final Color TEXT_MID      = new Color(127, 140, 141);  // phụ
    public static final Color BG_PAGE       = new Color(245, 247, 250);  // nền trang
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BORDER_COLOR  = new Color(220, 220, 220);
    public static final Color TABLE_HEADER  = PRIMARY;
    public static final Color ROW_ALT       = new Color(245, 249, 253);  // dòng chẵn

    // ===================== FONT =====================
    public static final Font FONT_PAGE_TITLE  = new Font("Segoe UI", Font.BOLD,   24);
    public static final Font FONT_SECTION     = new Font("Segoe UI", Font.BOLD,   14);
    public static final Font FONT_LABEL       = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_TABLE_HEAD  = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_STAT_NUM    = new Font("Segoe UI", Font.BOLD,   30);
    public static final Font FONT_STAT_LABEL  = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_BTN         = new Font("Segoe UI", Font.BOLD,   12);

    // ===================== KÍCH THƯỚC =====================
    public static final int  ROW_HEIGHT       = 36;
    public static final int  BTN_HEIGHT       = 36;
    public static final Dimension BTN_SIZE    = new Dimension(120, BTN_HEIGHT);
    public static final Dimension BTN_WIDE    = new Dimension(160, BTN_HEIGHT);

    // ===================== HELPER METHODS =====================

    /** Tạo tiêu đề trang: label lớn + gạch chân xanh */
    public static JPanel makePageHeader(String title) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_PAGE_TITLE);
        lbl.setForeground(PRIMARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(PRIMARY);
        sep.setPreferredSize(new Dimension(0, 2));

        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(sep, BorderLayout.SOUTH);
        return pnl;
    }

    /** Stat card: nền trắng, bo góc, đổ bóng nhẹ */
    public static JPanel makeStatCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(FONT_STAT_NUM);
        lblVal.setForeground(valueColor);

        JLabel lblName = new JLabel(label.toUpperCase());
        lblName.setFont(FONT_STAT_LABEL);
        lblName.setForeground(TEXT_MID);

        card.add(lblName, BorderLayout.NORTH);
        card.add(lblVal,  BorderLayout.CENTER);
        return card;
    }

    /** Panel bộ lọc: nền trắng, viền nhạt */
    public static JPanel makeFilterPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnl.setBackground(BG_CARD);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return pnl;
    }

    /** JLabel tiêu đề section trong card */
    public static JLabel makeSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }

    /** Nút PRIMARY */
    public static JButton makePrimaryBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(BTN_SIZE);
        return btn;
    }

    /** Nút màu bất kỳ */
    public static JButton makeBtn(String text, Color bg) {
        JButton btn = makePrimaryBtn(text);
        btn.setBackground(bg);
        return btn;
    }

    /** JTable đồng bộ style */
    public static void styleTable(JTable table) {
        table.setRowHeight(ROW_HEIGHT);
        table.setFont(FONT_LABEL);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(FONT_TABLE_HEAD);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /** JScrollPane không viền với header màu */
    public static JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getViewport().setBackground(BG_CARD);
        return sp;
    }

    /** Label + JTextField theo chiều dọc */
    public static JPanel makeLabeledField(String labelText, JComponent field) {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MID);
        pnl.add(lbl,   BorderLayout.NORTH);
        pnl.add(field, BorderLayout.CENTER);
        return pnl;
    }

    /** JTextField chuẩn */
    public static JTextField makeTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_LABEL);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, BTN_HEIGHT));
        return tf;
    }

    /** JComboBox chuẩn */
    public static JComboBox<Object> makeComboBox() {
        JComboBox<Object> cb = new JComboBox<>();
        cb.setFont(FONT_LABEL);
        cb.setBackground(BG_CARD);
        cb.setPreferredSize(new Dimension(160, BTN_HEIGHT));
        return cb;
    }

    /** Panel card trắng bo góc */
    public static JPanel makeCard(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return card;
    }

    // ===================== INNER CLASS =====================

    /** Renderer tô màu xen kẽ cho table */
    public static class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
            }
            setFont(FONT_LABEL);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return this;
        }
    }
}