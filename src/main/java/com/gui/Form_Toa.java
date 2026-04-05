package com.gui;

import com.dao.DAO_LoaiToa;
import com.dao.DAO_Toa;
import com.entities.LoaiToa;
import com.entities.Tau;
import com.entities.Toa;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class Form_Toa extends JDialog {
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

    private JTextField txtMaToa, txtTenToa, txtSoGhe, txtTauHienTai;
    private JLabel errMaToa, errTenToa, errSoGhe;
    private JComboBox<LoaiToa> cbLoaiToa;
    private JButton btnConfirm, btnCancel;
    
    private boolean confirmed = false;
    private Toa toaEntity;
    private Tau tauSelected;
    
    private DAO_LoaiToa loaiToaDAO = new DAO_LoaiToa();

    public Form_Toa(Frame parent, String title, Tau tau) {
        super(parent, title, true);
        this.tauSelected = tau;
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_PAGE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        initUI();
        loadLoaiToa();
        initEvents();

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // ==== HÀM MỚI: Chỉ gọi hàm này khi người dùng bấm "Thêm Thủ Công" ====
    public void setupForAdd() {
        txtMaToa.setText(new DAO_Toa().phatSinhMaToaTheoTau(tauSelected.getMaTau()));
        txtMaToa.setEditable(false);
        txtMaToa.setBackground(new Color(0xEEF2F8));
    }

    private void initUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 6, 5, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtTauHienTai = makeField();
        txtTauHienTai.setText(tauSelected.getTenTau() + " (Sức chứa: " + tauSelected.getSoToa() + " toa)");
        txtTauHienTai.setEditable(false);
        txtTauHienTai.setBackground(new Color(0xEEF2F8));
        txtTauHienTai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTauHienTai.setForeground(ACCENT);

        txtMaToa = makeField(); txtTenToa = makeField(); txtSoGhe = makeField();
        errMaToa = makeErrLbl(); errTenToa = makeErrLbl(); errSoGhe = makeErrLbl();
        cbLoaiToa = makeCombo();

        int r = 0;
        addSep(form, gc, r++, "Thông tin Toa", ACCENT);
        
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0; 
        JLabel l1 = new JLabel("Thuộc tàu:"); l1.setFont(F_LABEL); l1.setForeground(TEXT_MID); form.add(l1, gc);
        gc.gridx = 1; gc.weightx = 1; txtTauHienTai.setPreferredSize(new Dimension(260, 36)); form.add(txtTauHienTai, gc);
        r++;

        addRowWithErr(form, gc, r, "Mã toa:", txtMaToa, errMaToa); r += 2;
        addRowWithErr(form, gc, r, "Tên toa:", txtTenToa, errTenToa); r += 2;
        
        gc.gridx = 0; gc.gridy = r; gc.weightx = 0; 
        JLabel l2 = new JLabel("Loại toa:"); l2.setFont(F_LABEL); l2.setForeground(TEXT_MID); form.add(l2, gc);
        gc.gridx = 1; gc.weightx = 1; cbLoaiToa.setPreferredSize(new Dimension(260, 36)); form.add(cbLoaiToa, gc);
        r++;
        
        addRowWithErr(form, gc, r, "Số ghế:", txtSoGhe, errSoGhe); r += 2;

        add(form, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        bar.setOpaque(false);
        btnConfirm = makeBtn("Lưu", BtnStyle.PRIMARY);
        btnCancel = makeBtn("Hủy", BtnStyle.SECONDARY);
        bar.add(btnCancel); bar.add(btnConfirm);
        add(bar, BorderLayout.SOUTH);
    }

    private void loadLoaiToa() {
        List<LoaiToa> list = loaiToaDAO.getAllLoaiToa();
        for (LoaiToa lt : list) cbLoaiToa.addItem(lt);
        cbLoaiToa.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiToa) setText(((LoaiToa) value).getTenLoaiToa());
                return this;
            }
        });
    }

    private void initEvents() {
        txtTenToa.addActionListener(e -> cbLoaiToa.requestFocus());
        cbLoaiToa.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { if(e.getKeyCode() == KeyEvent.VK_ENTER) txtSoGhe.requestFocus(); }
        });
        txtSoGhe.addActionListener(e -> btnConfirm.doClick());

        txtTenToa.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { errTenToa.setText(" "); }});
        txtSoGhe.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { errSoGhe.setText(" "); }});

        btnConfirm.addActionListener(e -> {
            if (validateInput()) {
                toaEntity = new Toa();
                toaEntity.setMaToa(txtMaToa.getText().trim());
                toaEntity.setTenToa(txtTenToa.getText().trim());
                toaEntity.setSoGhe(Integer.parseInt(txtSoGhe.getText().trim()));
                toaEntity.setLoaiToa((LoaiToa) cbLoaiToa.getSelectedItem());
                toaEntity.setTau(tauSelected);
                confirmed = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> dispose());
    }

    private boolean validateInput() {
        boolean valid = true;
        errTenToa.setText(" "); errSoGhe.setText(" ");
        
        if (txtTenToa.getText().trim().isEmpty()) {
            errTenToa.setText("* Tên toa không được để trống!"); valid = false;
        }
        
        try {
            int ghe = Integer.parseInt(txtSoGhe.getText().trim());
            LoaiToa lt = (LoaiToa) cbLoaiToa.getSelectedItem();
            String tenLT = lt.getTenLoaiToa().toLowerCase();
            
            if (tenLT.contains("cứng") || tenLT.contains("mềm")) {
                if (ghe < 64 || ghe > 80) { 
                    errSoGhe.setText("* Ghế cứng/mềm phải từ 64-80 ghế!"); 
                    valid = false; 
                }
            } else if (tenLT.contains("nằm")) {
                if (ghe < 28 || ghe > 42) { 
                    errSoGhe.setText("* Giường nằm phải từ 28-42 ghế!"); 
                    valid = false; 
                }
            } else {
                if (ghe <= 0) { errSoGhe.setText("* Số ghế phải lớn hơn 0!"); valid = false; }
            }
        } catch (NumberFormatException e) {
            errSoGhe.setText("* Số ghế phải là số nguyên dương!"); valid = false;
        }
        return valid;
    }

    public void setEntity(Toa t) {
        this.toaEntity = t;
        txtMaToa.setText(t.getMaToa());
        txtMaToa.setEditable(false); 
        txtMaToa.setBackground(new Color(0xEEF2F8));
        txtTenToa.setText(t.getTenToa());
        txtSoGhe.setText(String.valueOf(t.getSoGhe()));
        
        for (int i = 0; i < cbLoaiToa.getItemCount(); i++) {
            if (cbLoaiToa.getItemAt(i).getMaLoaiToa().equals(t.getLoaiToa().getMaLoaiToa())) {
                cbLoaiToa.setSelectedIndex(i);
                break;
            }
        }
    }

    public Toa getEntity() { return toaEntity; }
    public boolean isConfirmed() { return confirmed; }

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