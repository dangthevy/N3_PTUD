package com.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import com.dao.DAO_Tau;
import com.entities.Tau;
import com.enums.TrangThaiTau;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Form_Tau extends JDialog {
    private static final Color BG_PAGE     = new Color(0xF4F7FB);
    private static final Color ACCENT      = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR  = new Color(0x2270CC);
    private static final Color ACCENT_FOC  = new Color(0x4D9DE0);
    private static final Color TEXT_DARK   = new Color(0x1E2B3C);
    private static final Color TEXT_MID    = new Color(0x5A6A7D);
    private static final Color BORDER      = new Color(0xE2EAF4);
    private static final Color BTN2_BG     = new Color(0xF0F4FA);
    private static final Color BTN2_FG     = new Color(0x3A5A8C);
    
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_ERR   = new Font("Segoe UI", Font.ITALIC, 11);
    
    private enum BtnStyle { PRIMARY, SECONDARY }

    private JTextField txtMa, txtTen, txtSoToa;
    private JLabel errMa, errTen, errSoToa;
    private JComboBox<TrangThaiTau> cbTrangThai;
    private JButton btnSave, btnCancel;
    
    private boolean isEditMode = false;
    private boolean confirmed = false;

    public Form_Tau(Frame parent, String title) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_PAGE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initUI();
        initEvents();
        
        // Tự động sinh mã nếu là Thêm Mới
        txtMa.setText(new DAO_Tau().phatSinhMaTau());
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(0xEEF2F8)); 

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtMa = makeField(); txtTen = makeField(); txtSoToa = makeField();
        errMa = makeErrLbl(); errTen = makeErrLbl(); errSoToa = makeErrLbl();
        
        cbTrangThai = makeCombo();
        for (TrangThaiTau tt : TrangThaiTau.values()) cbTrangThai.addItem(tt);
        cbTrangThai.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TrangThaiTau) setText(((TrangThaiTau) value).getMoTa());
                return this;
            }
        });

        int r = 0;
        addSep(form, gc, r++, "Thông tin Tàu", ACCENT);
        addRowWithErr(form, gc, r, "Mã tàu:", txtMa, errMa); r += 2;
        addRowWithErr(form, gc, r, "Tên tàu:", txtTen, errTen); r += 2;
        addRowWithErr(form, gc, r, "Số toa:", txtSoToa, errSoToa); r += 2;
        
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0; 
        JLabel l = new JLabel("Trạng thái:"); l.setFont(F_LABEL); l.setForeground(TEXT_MID); form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1; cbTrangThai.setPreferredSize(new Dimension(260, 36)); form.add(cbTrangThai, gc);

        add(form, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        bar.setOpaque(false);
        btnSave = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnCancel = makeBtn("Hủy", BtnStyle.SECONDARY);
        bar.add(btnCancel); bar.add(btnSave);
        add(bar, BorderLayout.SOUTH);
    }

    private void initEvents() {
        txtTen.addActionListener(e -> txtSoToa.requestFocus());
        txtSoToa.addActionListener(e -> cbTrangThai.requestFocus());
        cbTrangThai.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) btnSave.doClick(); }
        });

        txtTen.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { errTen.setText(" "); }});
        txtSoToa.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { errSoToa.setText(" "); }});

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
    }

    private boolean validateInput() {
        boolean valid = true;
        errTen.setText(" "); errSoToa.setText(" ");

        String ten = txtTen.getText().trim();
        if (!ten.matches("^(SE|TN|SN|SPT)\\d+$")) {
            errTen.setText("* Tên tàu bắt đầu bằng SE, TN, SN, SPT và số (VD: SE1)"); 
            valid = false;
        }
        
        try {
            int soToa = Integer.parseInt(txtSoToa.getText().trim());
            if (soToa < 10 || soToa > 15) { 
                errSoToa.setText("* Số toa quy định phải từ 10 đến 15 toa!"); 
                valid = false; 
            }
        } catch (NumberFormatException e) {
            errSoToa.setText("* Số toa phải là số nguyên hợp lệ!"); 
            valid = false;
        }
        return valid;
    }

    public void setEntity(Tau t) {
        isEditMode = true;
        txtMa.setText(t.getMaTau());
        txtTen.setText(t.getTenTau());
        txtSoToa.setText(String.valueOf(t.getSoToa()));
        cbTrangThai.setSelectedItem(t.getTrangThaiTau());
    }

    public Tau getEntity() {
        return new Tau(
            txtMa.getText().trim(),
            txtTen.getText().trim(),
            Integer.parseInt(txtSoToa.getText().trim()),
            (TrangThaiTau) cbTrangThai.getSelectedItem()
        );
    }

    public boolean isConfirmed() { return confirmed; }

    // ================= UI HELPERS =================
    private JTextField makeField() {
        JTextField tf = new JTextField();
        tf.setFont(F_CELL); tf.setForeground(TEXT_DARK); tf.setBackground(new Color(0xF8FAFD));
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { if(tf.isEditable()) tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT_FOC, 2, true), BorderFactory.createEmptyBorder(5, 9, 5, 9))); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { if(tf.isEditable()) tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(6, 10, 6, 10))); }
        });
        return tf;
    }

    private JLabel makeErrLbl() {
        JLabel l = new JLabel(" "); l.setFont(F_ERR); l.setForeground(new Color(231, 76, 60)); return l;
    }

    private <T> JComboBox<T> makeCombo() {
        JComboBox<T> cb = new JComboBox<>(); cb.setFont(F_CELL); 
        cb.setBackground(new Color(0xF8FAFD)); cb.setForeground(TEXT_DARK);
        cb.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), BorderFactory.createEmptyBorder(2, 4, 2, 4))); 
        return cb;
    }

    private void addSep(JPanel form, GridBagConstraints gc, int row, String txt, Color c) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1; 
        JLabel l = new JLabel(txt); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(c);
        form.add(l, gc); gc.gridwidth = 1;
    }

    private void addRowWithErr(JPanel form, GridBagConstraints gc, int row, String lbl, JComponent field, JLabel errLbl) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; 
        JLabel l = new JLabel(lbl); l.setFont(F_LABEL); l.setForeground(TEXT_MID); form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1; field.setPreferredSize(new Dimension(260, 36)); form.add(field, gc);
        
        gc.gridy = row + 1; gc.gridx = 1; gc.weightx = 1;
        gc.insets = new Insets(0, 6, 8, 6);
        form.add(errLbl, gc);
        gc.insets = new Insets(5, 6, 5, 6); 
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if(style == BtnStyle.PRIMARY) { g2.setColor(getModel().isRollover() ? ACCENT_HVR : ACCENT); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); }
                else { g2.setColor(getModel().isRollover() ? new Color(0xE0ECFF) : BTN2_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); g2.setColor(BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(F_LABEL); b.setForeground(style == BtnStyle.SECONDARY ? BTN2_FG : Color.WHITE);
        b.setPreferredSize(new Dimension(100, 36));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
}