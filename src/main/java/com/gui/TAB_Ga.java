package com.gui;

import com.dao.DAO_Ga;
import com.entities.Ga;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class TAB_Ga extends JPanel {

    // =========================================================================
    // MÀU SẮC & FONT
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

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);

    private enum BtnStyle { PRIMARY, SECONDARY, DANGER }

    // =========================================================================
    // THÀNH PHẦN GIAO DIỆN
    // =========================================================================
    private DefaultTableModel dataModel;
    private JTable table;
    private JTextField txtSearch;
    private JLabel lblTotal, lblActive, lblInactive;
    private final DAO_Ga dsGa = new DAO_Ga();

    public TAB_Ga() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initUI();
        updateTableData();
    }

    private void initUI() {
        // ================= TOP PANEL (TITLE & DASHBOARD) =================
        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ GA");
        title.setFont(F_TITLE); title.setForeground(ACCENT);
        pnlTop.add(title, BorderLayout.NORTH);

        JPanel pnlDashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlDashboard.setOpaque(false);
        pnlDashboard.add(createStatCard("TỔNG SỐ GA", lblTotal = new JLabel("0"), ACCENT));
        pnlDashboard.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActive = new JLabel("0"), new Color(39, 174, 96)));
        pnlDashboard.add(createStatCard("NGƯNG HOẠT ĐỘNG", lblInactive = new JLabel("0"), new Color(192, 57, 43)));
        pnlTop.add(pnlDashboard, BorderLayout.CENTER);

        // ================= CENTER PANEL (TOOL BAR & TABLE) =================
        JPanel centerPnl = makeCard(new BorderLayout(0, 15));
        centerPnl.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(), new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setOpaque(false);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSearch.setOpaque(false);
        txtSearch = makeField("Nhập tên hoặc mã ga để tìm kiếm...");
        txtSearch.setPreferredSize(new Dimension(300, 36));
        pnlSearch.add(txtSearch);

        JButton btnXoa = makeBtn("- Xóa", BtnStyle.DANGER);
        JButton btnThem = makeBtn("+ Thêm Mới", BtnStyle.PRIMARY);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnThem);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlButtons, BorderLayout.EAST);

        String[] cols = {"Mã Ga", "Tên Ga", "Địa chỉ", "Tỉnh Thành"};
        dataModel = new DefaultTableModel(cols, 0);
        table = buildTable(dataModel);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER));
        scrollPane.getViewport().setBackground(BG_CARD);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        centerPnl.add(pnlToolbar, BorderLayout.NORTH);
        centerPnl.add(scrollPane, BorderLayout.CENTER);

        // XỬ LÝ SỰ KIỆN
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { thucHienTimKiem(); }
            private void thucHienTimKiem() {
                String tuKhoa = txtSearch.getText().trim();
                if(tuKhoa.equals("Nhập tên hoặc mã ga để tìm kiếm...")) return;

                List<Ga> listKetQua = dsGa.timKiemGa(tuKhoa);
                dataModel.setRowCount(0);
                for (Ga ga : listKetQua) {
                    dataModel.addRow(new Object[]{ga.getMaGa(), ga.getTenGa(), ga.getDiaChi(), ga.getTinhThanh()});
                }
            }
        });

        btnXoa.addActionListener(e -> xoaGa());
        btnThem.addActionListener(e -> hienThiDialogThemGa());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                        hienThiDialogSuaGa(row);
                    }
                }
            }
        });

        add(pnlTop, BorderLayout.NORTH);
        add(centerPnl, BorderLayout.CENTER);
    }

    private void updateTableData() {
        List<Ga> list = dsGa.getAllGa();
        dataModel.setRowCount(0);
        for (Ga ga : list) {
            dataModel.addRow(new Object[]{ga.getMaGa(), ga.getTenGa(), ga.getDiaChi(), ga.getTinhThanh()});
        }

        // Cập nhật thẻ Dashboard
        lblTotal.setText(String.valueOf(list.size()));
        lblActive.setText(String.valueOf(list.size()));
        lblInactive.setText("0"); // Do DAO hiện tại chỉ lấy Ga đang hoạt động
    }

    private void xoaGa() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn Ga cần xóa!"); return; }
        String maGa = table.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa Ga: " + maGa + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dsGa.deleteGa(maGa)) {
                JOptionPane.showMessageDialog(this, "Đã xóa Ga thành công!");
                updateTableData();
            } else {
                warn("Xóa Ga thất bại! Vui lòng thử lại.");
            }
        }
    }

    private void hienThiDialogThemGa() {
        JDialog dialog = makeDialog("Thêm Mới Ga");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(dsGa.phatSinhMaGa());
        JTextField txtTen = makeField("");
        JTextField txtDiaChi = makeField("");
        JTextField txtTinhThanh = makeField("");

        int r = 0;
        addRow(form, gc, r++, "Mã Ga:", txtMa);
        addRow(form, gc, r++, "Tên Ga:", txtTen);
        addRow(form, gc, r++, "Địa chỉ:", txtDiaChi);
        addRow(form, gc, r++, "Tỉnh thành:", txtTinhThanh);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnThem = makeBtn("Thêm", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnThem.addActionListener(e -> {
            String tenMoi = txtTen.getText().trim();
            if (tenMoi.isEmpty()) { warn("Tên ga không được để trống!"); return; }

            for (Ga existingGa : dsGa.getAllGa()) {
                if (existingGa.getTenGa().equalsIgnoreCase(tenMoi)) {
                    warn("Trùng ga! Tên ga này đã tồn tại trong CSDL.");
                    return;
                }
            }

            Ga gaMoi = new Ga(txtMa.getText(), tenMoi, txtDiaChi.getText().trim(), txtTinhThanh.getText().trim());
            if (dsGa.addGa(gaMoi)) {
                JOptionPane.showMessageDialog(dialog, "Thêm Ga thành công!");
                updateTableData();
                dialog.dispose();
            } else {
                warn("Thêm Ga thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnThem);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private void hienThiDialogSuaGa(int row) {
        String maGa = table.getValueAt(row, 0).toString();
        Ga gaHienTai = dsGa.getGaByMa(maGa);
        if (gaHienTai == null) { warn("Không tìm thấy dữ liệu ga này trong CSDL!"); return; }

        JDialog dialog = makeDialog("Cập nhật Thông tin Ga");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false); form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gc = defaultGC();

        JTextField txtMa = roField(gaHienTai.getMaGa());
        JTextField txtTen = makeField(""); txtTen.setText(gaHienTai.getTenGa());
        JTextField txtDiaChi = makeField(""); txtDiaChi.setText(gaHienTai.getDiaChi());
        JTextField txtTinhThanh = makeField(""); txtTinhThanh.setText(gaHienTai.getTinhThanh());

        int r = 0;
        addRow(form, gc, r++, "Mã Ga:", txtMa);
        addRow(form, gc, r++, "Tên Ga:", txtTen);
        addRow(form, gc, r++, "Địa chỉ:", txtDiaChi);
        addRow(form, gc, r++, "Tỉnh thành:", txtTinhThanh);

        JButton btnHuy = makeBtn("Hủy", BtnStyle.SECONDARY);
        JButton btnSua = makeBtn("Cập nhật", BtnStyle.PRIMARY);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnSua.addActionListener(e -> {
            String tenSua = txtTen.getText().trim();
            if (tenSua.isEmpty()) { warn("Tên ga không được để trống!"); return; }

            for (Ga existingGa : dsGa.getAllGa()) {
                if (!existingGa.getMaGa().equals(txtMa.getText()) && existingGa.getTenGa().equalsIgnoreCase(tenSua)) {
                    warn("Trùng ga! Tên ga này đã bị trùng lặp với một Ga khác.");
                    return;
                }
            }

            Ga gaCapNhat = new Ga(txtMa.getText(), tenSua, txtDiaChi.getText().trim(), txtTinhThanh.getText().trim());
            if (dsGa.updateGa(gaCapNhat)) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật Ga thành công!");
                updateTableData();
                dialog.dispose();
            } else {
                warn("Cập nhật Ga thất bại!");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false); btnPanel.add(btnHuy); btnPanel.add(btnSua);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    // =========================================================================
    // HELPER UI CHUNG
    // =========================================================================
    private JPanel createStatCard(String title, JLabel lblValue, Color accent) {
        JPanel p = makeCard(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(new ShadowBorder(), new EmptyBorder(15, 20, 15, 20)));
        JLabel lblT = new JLabel(title);
        lblT.setForeground(TEXT_MID);
        lblT.setFont(F_LABEL);
        lblValue.setForeground(accent);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(lblT, BorderLayout.NORTH); p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                return c;
            }
        };
        t.setRowHeight(38); t.setFont(F_CELL);
        t.setBackground(BG_CARD); t.setSelectionBackground(new Color(0xDDEEFF));
        t.setSelectionForeground(TEXT_DARK); t.setGridColor(BORDER);
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false); t.setFocusable(false);
        t.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(LEFT); }
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                l.setOpaque(true); l.setBackground(ACCENT); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI",Font.BOLD,13)); l.setBorder(BorderFactory.createEmptyBorder(0,12,0,6)); return l;
            }
        });
        h.setPreferredSize(new Dimension(0, 40)); h.setReorderingAllowed(false);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));
        for (int i = 0; i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        return t;
    }

    private JPanel makeCard(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG_CARD); p.setBorder(new ShadowBorder()); return p;
    }

    private JTextField makeField(String hint) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(TEXT_LIGHT);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(hint, getInsets().left + 4, getHeight() / 2 + 5); g2.dispose();
                }
            }
        };
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_FOC, 2, true), BorderFactory.createEmptyBorder(5, 9, 5, 9))); }
            public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10))); }
        });
        return tf;
    }

    private JTextField roField(String v) {
        JTextField tf = makeField(""); tf.setText(v); tf.setEditable(false); tf.setBackground(new Color(0xEEF2F8)); return tf;
    }

    private GridBagConstraints defaultGC() {
        GridBagConstraints gc = new GridBagConstraints(); gc.insets = new Insets(6, 8, 6, 8);
        gc.anchor = GridBagConstraints.WEST; gc.fill = GridBagConstraints.HORIZONTAL; return gc;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; JLabel l = new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID); form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1; field.setPreferredSize(new Dimension(260, 36)); form.add(field, gc);
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                switch(style){
                    case PRIMARY -> { g2.setColor(getModel().isRollover()?ACCENT_HVR:ACCENT); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    case DANGER  -> { g2.setColor(getModel().isRollover()?BTN_RED_HVR:BTN_RED); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    default      -> { g2.setColor(getModel().isRollover()?new Color(0xE0ECFF):BTN2_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); }
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(style == BtnStyle.DANGER ? 110 : 130, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JDialog makeDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog d = (owner instanceof Frame) ? new JDialog((Frame)owner, title, true) : new JDialog((Dialog)owner, title, true);
        d.setLayout(new BorderLayout()); d.getContentPane().setBackground(BG_PAGE);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); return d;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(0xC0D4EE);trackColor=BG_PAGE;}
            @Override protected JButton createDecreaseButton(int o){return zBtn();}
            @Override protected JButton createIncreaseButton(int o){return zBtn();}
            private JButton zBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        sb.putClientProperty(FlatClientProperties.SCROLL_BAR_SHOW_BUTTONS, false);
    }

    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE); }

    private static class ShadowBorder extends AbstractBorder {
        private static final int S = 4;
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int w,int h){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i=S;i>0;i--){g2.setColor(new Color(100,140,200,(int)(20.0*(S-i)/S)));g2.drawRoundRect(x+i,y+i,w-2*i-1,h-2*i-1,12,12);}
            g2.setColor(new Color(0xE2EAF4));g2.drawRoundRect(x,y,w-1,h-1,12,12);
            g2.setColor(BG_CARD);g2.setClip(new RoundRectangle2D.Float(x+1,y+1,w-2,h-2,12,12));g2.fillRect(x+1,y+1,w-2,h-2);g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(S,S,S,S);}
        @Override public Insets getBorderInsets(Component c,Insets ins){ins.set(S,S,S,S);return ins;}
    }
}