package com.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import com.entities.NhanVien;
import com.dao.DAO_NhanVien;
import com.connectDB.ConnectDB;

public class GD_ThongTinCaNhan extends JDialog {

    // ================= COLOR (đồng bộ toàn hệ thống) =================
    private static final Color BG_PAGE      = new Color(0xF1F5FB);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1A5EAB);
    private static final Color ACCENT_HVR   = new Color(0x154D8F);
    private static final Color TEXT_DARK    = new Color(0x1A2B45);
    private static final Color TEXT_MID     = new Color(0x5A7499);
    private static final Color BORDER       = new Color(0xCBDCF0);
    private static final Color BTN_RED      = new Color(0xC0392B);
    private static final Color BTN_RED_HVR  = new Color(0xA93226);
    private static final Color BTN_GREEN    = new Color(0x16A34A);
    private static final Color BTN_GREEN_HVR= new Color(0x22C55E);

    // ================= FONT (đồng bộ toàn hệ thống) =================
    private static final Font F_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_MUTED = new Font("Segoe UI", Font.BOLD, 11);

    private enum BtnStyle { PRIMARY, SUCCESS, DANGER }

    private NhanVien nv;
    private DAO_NhanVien daoNV;

    private JTextField txtTen, txtSdt, txtEmail;
    private JLabel lblMa, lblTaiKhoan, lblChucVu, lblNgayVao, lblTrangThai, lblHeaderName;
    private JButton btnEditSave, btnLogout;
    private boolean isEditMode = false;

    public GD_ThongTinCaNhan(JFrame parent, NhanVien nv) {
        super(parent, "Thông tin cá nhân", true);
        this.nv    = nv;
        this.daoNV = new DAO_NhanVien(ConnectDB.getConnection());

        setSize(780, 560);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(BG_PAGE);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        setEditMode(false);
        initEvents(parent);
    }

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0D3570), getWidth(), 0, ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel lblAvatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.drawOval(cx - 8, cy - 12, 16, 16);
                g2.drawArc(cx - 13, cy + 5, 26, 16, 0, 180);
                g2.dispose();
            }
        };
        lblAvatar.setPreferredSize(new Dimension(46, 46));

        JPanel nameBlock = new JPanel();
        nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
        nameBlock.setOpaque(false);
        JLabel lblRole = new JLabel(nv.getChucVu().name());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(180, 210, 245));
        lblHeaderName = new JLabel(nv.getTenNV().toUpperCase());
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderName.setForeground(Color.WHITE);
        nameBlock.add(lblRole);
        nameBlock.add(lblHeaderName);

        left.add(lblAvatar);
        left.add(nameBlock);

        // Nút Đăng xuất màu đỏ
        btnLogout = new JButton("  Đăng xuất") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Màu nền đỏ khi hover/bình thường
                g2.setColor(getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Icon mũi tên thoát
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = 18, cy = getHeight()/2;
                g2.drawLine(cx-5, cy-4, cx-5, cy+4); // Thanh đứng
                g2.drawLine(cx-5, cy, cx+5, cy);     // Thanh ngang
                g2.drawLine(cx+2, cy-3, cx+5, cy);   // Mũi tên trên
                g2.drawLine(cx+2, cy+3, cx+5, cy);   // Mũi tên dưới
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnLogout.setFont(F_LABEL);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setOpaque(false); btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false); btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(130, 34));

        pnl.add(left,      BorderLayout.WEST);
        pnl.add(btnLogout, BorderLayout.EAST);
        return pnl;
    }

    private JPanel buildContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_PAGE);
        wrapper.setBorder(new EmptyBorder(20, 24, 10, 24));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                new EmptyBorder(24, 30, 24, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(10, 12, 10, 12);
        gbc.weightx = 0.5;

        lblMa        = makeReadOnly(nv.getMaNV());
        txtTen       = makeStyledField(nv.getTenNV());
        txtSdt       = makeStyledField(nv.getSdt());
        txtEmail     = makeStyledField(nv.getEmail()); 

        lblTaiKhoan  = makeReadOnly(nv.getTaiKhoan());
        lblChucVu    = makeReadOnly(nv.getChucVu().name());
        lblNgayVao   = makeReadOnly(nv.getNgayVaoLam().toString());

        lblTrangThai = new JLabel(nv.getTrangThai().name());
        lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrangThai.setForeground(new Color(0x16A34A));

        addCell(card, "Mã nhân viên",  lblMa,       0, 0, gbc);
        addCell(card, "Họ và tên",     txtTen,      0, 1, gbc);
        addCell(card, "Số điện thoại", txtSdt,      1, 0, gbc);
        addCell(card, "Email cá nhân", txtEmail,    1, 1, gbc);
        addCell(card, "Tài khoản",     lblTaiKhoan, 2, 0, gbc);
        addCell(card, "Chức vụ",       lblChucVu,   2, 1, gbc);
        addCell(card, "Ngày vào làm",  lblNgayVao,  3, 0, gbc);
        addCell(card, "Trạng thái",    lblTrangThai,3, 1, gbc);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private void addCell(JPanel panel, String title, JComponent comp,
                         int row, int col, GridBagConstraints gbc) {
        JPanel cell = new JPanel(new BorderLayout(0, 4));
        cell.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_MUTED);
        lbl.setForeground(new Color(100, 116, 139));
        cell.add(lbl,  BorderLayout.NORTH);
        cell.add(comp, BorderLayout.CENTER);
        gbc.gridx = col;
        gbc.gridy = row;
        panel.add(cell, gbc);
    }

    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 14));
        pnl.setBackground(BG_PAGE);
        pnl.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        
        btnEditSave = makeBtn("Chỉnh sửa thông tin", BtnStyle.PRIMARY);
        btnEditSave.setPreferredSize(new Dimension(210, 38));
        pnl.add(btnEditSave);
        return pnl;
    }

    private void initEvents(JFrame parent) {
        btnLogout.addActionListener(e -> {
            if (isEditMode) {
                JOptionPane.showMessageDialog(this, "Đang chỉnh sửa, hãy lưu trước!");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (parent != null) parent.dispose();
                JFrame loginFrame = new JFrame("Đăng nhập");
                GUI_Login loginPanel = new GUI_Login();
                loginPanel.setParentFrame(loginFrame);
                loginFrame.setContentPane(loginPanel);
                loginFrame.pack();
                loginFrame.setLocationRelativeTo(null);
                loginFrame.setVisible(true);
            }
        });

        btnEditSave.addActionListener(e -> {
            if (!isEditMode) {
                setEditMode(true);
                btnEditSave.setText("Lưu thay đổi");
            } else {
                handleSave();
            }
        });
    }

    private void handleSave() {
        String ten  = capitalizeWords(txtTen.getText().trim());
        String sdt  = txtSdt.getText().trim();
        String mail = txtEmail.getText().trim();

        if (ten.isEmpty() || !ten.matches("^[A-Za-zÀ-ỹ\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Tên không hợp lệ!"); return;
        }
        if (!sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "SĐT phải là 10 chữ số!"); return;
        }
        if (!mail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng!"); return;
        }

        nv.setTenNV(ten);
        nv.setSdt(sdt);
        nv.setEmail(mail);

        if (daoNV.updateProfile(nv)) {
            lblHeaderName.setText(ten.toUpperCase());
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            setEditMode(false);
            btnEditSave.setText("Chỉnh sửa thông tin");
        }
    }

    private void setEditMode(boolean editable) {
        this.isEditMode = editable;
        for (JTextField tf : new JTextField[]{txtTen, txtSdt, txtEmail}) {
            tf.setEditable(editable);
            tf.setBackground(editable ? Color.WHITE : new Color(0xF8FAFC));
            tf.setBorder(editable ? new LineBorder(ACCENT, 1, true) : new LineBorder(BORDER, 1, true));
        }
        btnLogout.setEnabled(!editable);
    }

    private JLabel makeReadOnly(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_CELL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JTextField makeStyledField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(F_CELL);
        tf.setBorder(new LineBorder(BORDER, 1, true));
        tf.setPreferredSize(new Dimension(220, 36));
        return tf;
    }

    private JButton makeBtn(String text, BtnStyle style) {
        JButton btn = new JButton("      " + text) { 
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (style == BtnStyle.PRIMARY) {
                    g2.setColor(getModel().isPressed() ? new Color(0x0F3F8C) : getModel().isRollover() ? ACCENT_HVR : ACCENT);
                } else if (style == BtnStyle.DANGER) {
                    g2.setColor(getModel().isPressed() ? new Color(0x922B21) : getModel().isRollover() ? BTN_RED_HVR : BTN_RED);
                } else {
                    g2.setColor(getModel().isPressed() ? new Color(0x166534) : getModel().isRollover() ? BTN_GREEN_HVR : BTN_GREEN);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Vẽ ICON CÂY BÚT
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int x = 15, y = getHeight() / 2 - 2;
                g2.rotate(Math.toRadians(45), x + 5, y + 5);
                g2.drawRect(x, y, 10, 4);
                g2.drawLine(x + 10, y, x + 13, y + 2);
                g2.drawLine(x + 10, y + 4, x + 13, y + 2);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_LABEL);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0)
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private static class ShadowBorder extends AbstractBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xE2EAF4));
            g2.drawRoundRect(x, y, w - 1, h - 1, 12, 12);
            g2.dispose();
        }
    }
}